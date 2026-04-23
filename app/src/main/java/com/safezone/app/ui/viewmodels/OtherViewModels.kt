package com.safezone.app.ui.viewmodels

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safezone.app.data.local.preferences.SafeZonePrefs
import com.safezone.app.domain.models.AppResult
import com.safezone.app.domain.models.IncomingAlert
import com.safezone.app.domain.models.NearbyUser
import com.safezone.app.domain.models.PhraseAction
import com.safezone.app.domain.models.SecurityPhrase
import com.safezone.app.domain.models.SosEvent
import com.safezone.app.domain.models.SosStatus
import com.safezone.app.domain.repository.AlertRepository
import com.safezone.app.domain.repository.AuthRepository
import com.safezone.app.domain.repository.LocationPoint
import com.safezone.app.domain.repository.LocationRepository
import com.safezone.app.domain.repository.PhraseRepository
import com.safezone.app.domain.repository.SosRepository
import com.safezone.app.domain.usecases.EndSosUseCase
import com.safezone.app.domain.usecases.GetNearbyUsersUseCase
import com.safezone.app.domain.usecases.ObserveSosHistoryUseCase
import com.safezone.app.domain.usecases.SignOutUseCase
import com.safezone.app.services.BLEService
import com.safezone.app.services.ListeningService
import com.safezone.app.services.SOSService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// ---- MAP ----
data class MapUiState(
    val myLocation: LocationPoint? = null,
    val nearby: List<NearbyUser> = emptyList(),
    val focusedAlert: IncomingAlert? = null,
    val loading: Boolean = false
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val location: LocationRepository,
    private val getNearby: GetNearbyUsersUseCase,
    private val alerts: AlertRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                location.locationUpdates().collectLatest { p ->
                    _state.value = _state.value.copy(myLocation = p)
                    val r = getNearby(p.lat, p.lng, 500)
                    if (r is AppResult.Success) _state.value = _state.value.copy(nearby = r.data)
                }
            }
        }
        viewModelScope.launch {
            val uid = auth.currentUserId.firstOrNull() ?: return@launch
            runCatching {
                alerts.observeIncomingAlerts(uid).collectLatest { a ->
                    _state.value = _state.value.copy(focusedAlert = a)
                }
            }
        }
    }

    fun clearFocus() { _state.value = _state.value.copy(focusedAlert = null) }
}

// ---- HISTORY ----
data class HistoryUiState(
    val events: List<SosEvent> = emptyList(),
    val totalAlerts: Int = 0,
    val avgResponseMinutes: Int = 0
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val observeHistory: ObserveSosHistoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = auth.currentUserId.firstOrNull() ?: return@launch
            observeHistory(uid).collectLatest { list ->
                val avg = list.filter { it.durationSeconds > 0 }
                    .map { it.durationSeconds }.average().takeIf { !it.isNaN() } ?: 0.0
                _state.value = HistoryUiState(
                    events = list,
                    totalAlerts = list.size,
                    avgResponseMinutes = (avg / 60).toInt()
                )
            }
        }
    }
}

// ---- SETTINGS ----
data class SettingsUiState(
    val biometricEnabled: Boolean = false,
    val listeningEnabled: Boolean = false,
    val bleFallbackEnabled: Boolean = true,
    val phrases: List<SecurityPhrase> = emptyList(),
    val avatarUrl: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val prefs: SafeZonePrefs,
    private val phraseRepo: PhraseRepository,
    private val signOut: SignOutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { prefs.biometricEnabled.collectLatest {
            _state.value = _state.value.copy(biometricEnabled = it)
        } }
        viewModelScope.launch { prefs.listeningEnabled.collectLatest {
            _state.value = _state.value.copy(listeningEnabled = it)
        } }
        viewModelScope.launch { prefs.bleFallbackEnabled.collectLatest {
            _state.value = _state.value.copy(bleFallbackEnabled = it)
        } }
        viewModelScope.launch {
            val uid = auth.currentUserId.firstOrNull() ?: return@launch
            phraseRepo.observePhrases(uid).collectLatest {
                _state.value = _state.value.copy(phrases = it)
            }
        }
    }

    fun toggleBiometric(on: Boolean) = viewModelScope.launch { prefs.setBiometricEnabled(on) }
    fun toggleListening(context: Context, on: Boolean) = viewModelScope.launch {
        prefs.setListeningEnabled(on)
        if (on) ListeningService.start(context) else ListeningService.stop(context)
    }
    fun toggleBle(context: Context, on: Boolean) = viewModelScope.launch {
        prefs.setBleFallbackEnabled(on)
        if (on) BLEService.scan(context) else BLEService.stop(context)
    }
    fun deletePhrase(id: String) = viewModelScope.launch { phraseRepo.deletePhrase(id) }

    fun signOut(onDone: () -> Unit) = viewModelScope.launch {
        signOut.invoke()
        onDone()
    }
}

// ---- PHRASE SETUP ----
data class PhraseSetupUiState(
    val text: String = "",
    val action: PhraseAction = PhraseAction.FULL_SOS,
    val recording: Boolean = false,
    val preparingMic: Boolean = false,   // true while we're stopping ListeningService before recording
    val recordedFilePath: String? = null,
    val recordedSeconds: Int = 0,
    val phrases: List<SecurityPhrase> = emptyList(),
    val saving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PhraseSetupViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val phraseRepo: PhraseRepository,
    private val recorder: com.safezone.app.utils.ChunkedAudioRecorder,
    // FIX Bug 1: inject prefs so we know whether to restart ListeningService after recording
    private val prefs: SafeZonePrefs
) : ViewModel() {
    private val _state = MutableStateFlow(PhraseSetupUiState())
    val state: StateFlow<PhraseSetupUiState> = _state.asStateFlow()

    private var currentRecordingId: String? = null
    private var recordingStartedAtMs: Long = 0
    // FIX Bug 1: remember whether the listening service was running so we can restore it
    private var wasListening = false

    init {
        viewModelScope.launch {
            val uid = auth.currentUserId.firstOrNull() ?: return@launch
            phraseRepo.observePhrases(uid).collectLatest {
                _state.value = _state.value.copy(phrases = it)
            }
        }
    }

    fun onTextChange(v: String) { _state.value = _state.value.copy(text = v, error = null) }
    fun onAction(a: PhraseAction) { _state.value = _state.value.copy(action = a) }

    fun startRecording(context: Context) {
        if (_state.value.recording || _state.value.preparingMic) return
        viewModelScope.launch {
            // FIX Bug 1: SpeechRecognizer (ListeningService) and MediaRecorder both need the
            // microphone exclusively. Stop the service first, wait for the OS to release the
            // audio session, then start recording — otherwise setAudioSource() fails immediately.
            wasListening = prefs.listeningEnabled.firstOrNull() == true
            if (wasListening) {
                _state.value = _state.value.copy(preparingMic = true, error = null)
                ListeningService.stop(context)
                delay(500L) // give the OS time to release the audio focus
            }

            try {
                val id = UUID.randomUUID().toString()
                currentRecordingId = id
                recordingStartedAtMs = System.currentTimeMillis()
                recorder.startChunk(context, id, 0)
                _state.value = _state.value.copy(recording = true, preparingMic = false, error = null)
            } catch (ex: Exception) {
                _state.value = _state.value.copy(
                    recording = false,
                    preparingMic = false,
                    error = "Microphone unavailable: ${ex.message}"
                )
                // Restore listening service if we stopped it but recording still failed
                if (wasListening) ListeningService.start(context)
                wasListening = false
            }
        }
    }

    fun stopRecording(context: Context) {
        // FIX Bug 1: stopRecording now accepts context so it can restart ListeningService.
        // It runs in a coroutine to handle the case where startRecording's delay is still
        // in-flight (preparingMic == true) — we wait briefly before giving up.
        viewModelScope.launch {
            // If the mic was still being prepared when the user released, wait for it.
            var waited = 0
            while (_state.value.preparingMic && waited < 2_000) {
                delay(50); waited += 50
            }

            if (!_state.value.recording) {
                // Recording never actually started — nothing to stop, but still restore service.
                if (wasListening) { ListeningService.start(context); wasListening = false }
                return@launch
            }

            try {
                val f = recorder.stopChunk()
                val secs = if (recordingStartedAtMs > 0)
                    ((System.currentTimeMillis() - recordingStartedAtMs) / 1000).toInt() else 0
                _state.value = _state.value.copy(
                    recording = false,
                    recordedFilePath = f?.absolutePath,
                    recordedSeconds = secs
                )
            } catch (ex: Exception) {
                _state.value = _state.value.copy(
                    recording = false,
                    error = "Record stop failed: ${ex.message}"
                )
            } finally {
                currentRecordingId = null
                recordingStartedAtMs = 0
                // FIX Bug 1: restore the listening service now that the mic is free
                if (wasListening) ListeningService.start(context)
                wasListening = false
            }
        }
    }

    fun clearRecording() {
        _state.value = _state.value.copy(recordedFilePath = null, recordedSeconds = 0)
    }

    fun playRecording(context: Context) = viewModelScope.launch {
        val path = _state.value.recordedFilePath ?: return@launch
        try {
            val mp = MediaPlayer()
            mp.setDataSource(path)
            mp.prepare()
            mp.start()
            mp.setOnCompletionListener { it.release() }
        } catch (ex: Exception) {
            _state.value = _state.value.copy(error = "Playback failed: ${ex.message}")
        }
    }

    fun save() = viewModelScope.launch {
        val s = _state.value
        if (s.text.isBlank()) {
            _state.value = s.copy(error = "Phrase text is required")
            return@launch
        }

        // FIX Bug 2: previously this returned silently with no error message if the user
        // wasn't signed in yet. The form would clear, the phrase was never saved, and there
        // was no feedback — so the phrase appeared to vanish.
        val uid = auth.currentUserId.firstOrNull()
        if (uid == null) {
            _state.value = s.copy(error = "Not signed in — please restart the app and try again")
            return@launch
        }

        _state.value = s.copy(saving = true, error = null)
        val phrase = SecurityPhrase(
            id = UUID.randomUUID().toString(),
            userId = uid,
            text = s.text.trim(),
            action = s.action,
            enabled = true
        )
        when (val r = phraseRepo.savePhrase(phrase)) {
            is AppResult.Success -> {
                _state.value = _state.value.copy(
                    saving = false,
                    text = "",
                    recordedFilePath = null,
                    recordedSeconds = 0,
                    error = null
                )
            }
            is AppResult.Error -> _state.value = _state.value.copy(
                saving = false,
                error = r.message ?: "Failed to save phrase — please try again"
            )
            else -> _state.value = _state.value.copy(saving = false)
        }
    }

    fun delete(id: String) = viewModelScope.launch { phraseRepo.deletePhrase(id) }
}

// ---- SOS ACTIVE ----
data class SosActiveUiState(
    val active: Boolean = false,
    val eventId: String? = null,
    val elapsedSeconds: Long = 0,
    val audioRecording: Boolean = true,
    val locationSharing: Boolean = true,
    val authoritiesNotified: Boolean = true,
    val etaMinutes: Int = 3
)

@HiltViewModel
class SosActiveViewModel @Inject constructor(
    private val endSos: EndSosUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SosActiveUiState())
    val state: StateFlow<SosActiveUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            SOSService.state.collectLatest { s ->
                val elapsed = if (s.active && s.startedAtMs > 0)
                    (System.currentTimeMillis() - s.startedAtMs) / 1000 else 0L
                _state.value = _state.value.copy(
                    active = s.active, eventId = s.eventId, elapsedSeconds = elapsed
                )
            }
        }
        // Tick the timer
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val s = SOSService.state.value
                if (s.active && s.startedAtMs > 0) {
                    _state.value = _state.value.copy(
                        elapsedSeconds = (System.currentTimeMillis() - s.startedAtMs) / 1000
                    )
                }
            }
        }
    }

    fun endSos(context: Context, onEnded: () -> Unit) = viewModelScope.launch {
        val id = _state.value.eventId
        if (id != null) endSos.invoke(id, SosStatus.RESOLVED)
        SOSService.stop(context)
        onEnded()
    }
}

// ---- ALERT (incoming) ----
data class AlertUiState(
    val event: SosEvent? = null,
    val userName: String = "Unknown",
    val avatarUrl: String? = null,
    val distanceMeters: Double = 0.0,
    val etaMinutes: Double = 0.0,
    val loading: Boolean = true
)

@HiltViewModel
class AlertViewModel @Inject constructor(
    private val sos: SosRepository,
    private val auth: AuthRepository,
    private val location: LocationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AlertUiState())
    val state: StateFlow<AlertUiState> = _state.asStateFlow()

    fun load(eventId: String) = viewModelScope.launch {
        val uid = auth.currentUserId.firstOrNull() ?: return@launch
        val here = location.currentLocation()
        sos.observeHistory(uid).collectLatest { list ->
            val ev = list.firstOrNull { it.id == eventId }
            if (ev != null && here != null) {
                val dist = com.safezone.app.utils.haversineMeters(
                    here.lat, here.lng, ev.lat, ev.lng
                )
                _state.value = AlertUiState(
                    event = ev,
                    userName = ev.userName.ifBlank { "Unknown" },
                    avatarUrl = ev.userPhotoUrl,
                    distanceMeters = dist,
                    etaMinutes = (dist / 80).coerceAtLeast(1.0),
                    loading = false
                )
            } else {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }
}