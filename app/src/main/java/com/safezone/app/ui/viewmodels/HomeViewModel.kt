package com.safezone.app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safezone.app.data.local.preferences.SafeZonePrefs
import com.safezone.app.domain.models.TriggerSource
import com.safezone.app.domain.repository.AuthRepository
import com.safezone.app.domain.repository.ProfileRepository
import com.safezone.app.services.ListeningService
import com.safezone.app.services.SOSService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val statusLabel: String = "Safe",
    val listening: Boolean = false,
    val avatarUrl: String? = null,
    val sosActive: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val profile: ProfileRepository,
    private val prefs: SafeZonePrefs
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        // Profile avatar
        viewModelScope.launch {
            val uid = auth.currentUserId.firstOrNull() ?: return@launch
            profile.observeProfile(uid).collectLatest { p ->
                _state.value = _state.value.copy(avatarUrl = p?.photoUrl)
            }
        }
        // Listening toggle
        viewModelScope.launch {
            prefs.listeningEnabled.collectLatest {
                _state.value = _state.value.copy(listening = it)
            }
        }
        // Active SOS state
        viewModelScope.launch {
            SOSService.state.collectLatest {
                _state.value = _state.value.copy(
                    sosActive = it.active,
                    statusLabel = if (it.active) "Active SOS" else "Safe"
                )
            }
        }
    }

    fun triggerSos(context: Context) {
        SOSService.start(context, TriggerSource.MANUAL)
    }

    fun toggleListening(context: Context) = viewModelScope.launch {
        val on = !_state.value.listening
        prefs.setListeningEnabled(on)
        if (on) ListeningService.start(context) else ListeningService.stop(context)
    }
}
