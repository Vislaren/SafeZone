package com.safezone.app.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.safezone.app.MainActivity
import com.safezone.app.R
import com.safezone.app.SafeZoneApp
import com.safezone.app.domain.models.AppResult
import com.safezone.app.domain.models.SosEvent
import com.safezone.app.domain.models.SosStatus
import com.safezone.app.domain.models.TriggerSource
import com.safezone.app.domain.repository.AuthRepository
import com.safezone.app.domain.repository.LocationRepository
import com.safezone.app.domain.repository.SosRepository
import com.safezone.app.utils.ChunkedAudioRecorder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SOSService : Service() {

    @Inject lateinit var authRepo: AuthRepository
    @Inject lateinit var sosRepo: SosRepository
    @Inject lateinit var locationRepo: LocationRepository
    @Inject lateinit var recorder: ChunkedAudioRecorder

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentEvent: SosEvent? = null
    private var recordLoop: Job? = null
    private var locationLoop: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startSos(
                source = intent.getStringExtra(EXTRA_SOURCE)
                    ?.let { runCatching { TriggerSource.valueOf(it) }.getOrNull() }
                    ?: TriggerSource.MANUAL
            )
            ACTION_STOP -> stopSos()
            else -> stopSelf()
        }
        return START_STICKY
    }

    private fun startSos(source: TriggerSource) {
        if (_state.value.active) return
        startForegroundWithNotif()
        acquireWakeLock()

        scope.launch {
            val userId = authRepo.currentUserId.firstOrNull()
                ?: return@launch stopSelf()
            val loc = locationRepo.currentLocation() ?: run {
                _state.value = _state.value.copy(error = "Location unavailable")
                return@launch stopSelf()
            }

            when (val res = sosRepo.createEvent(userId, loc.lat, loc.lng, source)) {
                is AppResult.Success -> {
                    currentEvent = res.data
                    _state.value = SosState(active = true, eventId = res.data.id, startedAtMs = System.currentTimeMillis())
                    startRecordingLoop(res.data.id)
                    startLocationPush(res.data.id, userId)
                }
                is AppResult.Error -> {
                    _state.value = SosState(active = false, error = res.message)
                    stopSelf()
                }
                else -> Unit
            }
        }
    }

    private fun startRecordingLoop(eventId: String) {
        recordLoop?.cancel()
        recordLoop = scope.launch {
            var i = 0
            while (isActive()) {
                runCatching {
                    recorder.startChunk(this@SOSService, eventId, i)
                    delay(CHUNK_MS)
                    val file = recorder.stopChunk() ?: return@runCatching
                    val bytes = file.readBytes()
                    sosRepo.uploadAudioChunk(eventId, bytes)
                    runCatching { file.delete() }
                }.onFailure {
                    // Swallow & continue — the event is more important than a single chunk.
                }
                i++
            }
        }
    }

    private fun startLocationPush(eventId: String, userId: String) {
        locationLoop?.cancel()
        locationLoop = scope.launch {
            locationRepo.locationUpdates().collect { p ->
                sosRepo.updateEventLocation(eventId, p.lat, p.lng)
                locationRepo.publishLocation(userId, p.lat, p.lng)
            }
        }
    }

    private fun stopSos() {
        val id = currentEvent?.id
        scope.launch {
            recorder.stopChunk()
            recordLoop?.cancel(); locationLoop?.cancel()
            if (id != null) sosRepo.endEvent(id, SosStatus.RESOLVED)
            _state.value = SosState(active = false)
            releaseWakeLock()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun isActive(): Boolean = _state.value.active && scope.coroutineContext[Job]?.isActive == true

    private fun startForegroundWithNotif() {
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this, 1, Intent(this, SOSService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n: Notification = NotificationCompat.Builder(this, SafeZoneApp.CH_SOS_ACTIVE)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle(getString(R.string.notif_sos_active_title))
            .setContentText(getString(R.string.notif_sos_active_text))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openIntent)
            .addAction(0, "End SOS", stopIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIF_ID, n,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION or
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIF_ID, n)
        }
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SafeZone::SOS")
            .apply { acquire(30 * 60 * 1000L) }
    }

    private fun releaseWakeLock() {
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }

    override fun onDestroy() {
        recordLoop?.cancel(); locationLoop?.cancel(); scope.cancel()
        releaseWakeLock()
        super.onDestroy()
    }

    companion object {
        const val NOTIF_ID = 42
        const val ACTION_START = "com.safezone.app.action.SOS_START"
        const val ACTION_STOP = "com.safezone.app.action.SOS_STOP"
        const val EXTRA_SOURCE = "source"
        private const val CHUNK_MS = 15_000L

        /** Observable state shared with the UI (bound via a singleton shim). */
        private val _state = MutableStateFlow(SosState())
        val state: StateFlow<SosState> = _state.asStateFlow()

        fun start(context: Context, source: TriggerSource = TriggerSource.MANUAL) {
            val i = Intent(context, SOSService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_SOURCE, source.name)
            context.startForegroundService(i)
        }

        fun stop(context: Context) {
            val i = Intent(context, SOSService::class.java).setAction(ACTION_STOP)
            context.startService(i)
        }
    }
}

data class SosState(
    val active: Boolean = false,
    val eventId: String? = null,
    val startedAtMs: Long = 0L,
    val error: String? = null
)
