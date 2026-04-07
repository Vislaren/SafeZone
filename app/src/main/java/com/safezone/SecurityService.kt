package com.safezone

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaRecorder
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.safezone.SafeZoneApplication.Companion.SERVICE_CHANNEL_ID
import com.safezone.SafeZoneApplication.Companion.SERVICE_NOTIF_ID
import com.safezone.data.db.FileTag
import com.safezone.data.db.FileType
import com.safezone.data.db.VaultFile
import com.safezone.data.preferences.SessionManager
import com.safezone.data.repository.ContactRepository
import com.safezone.data.repository.SecurityRepository
import com.safezone.data.repository.VaultRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class SecurityService : LifecycleService(), SensorEventListener {

    companion object {
        private const val TAG = "SafeZone::Service"

        const val ACTION_START      = "com.safezone.action.START"
        const val ACTION_STOP       = "com.safezone.action.STOP"
        const val ACTION_TRIGGER    = "com.safezone.action.TRIGGER"
        const val ACTION_SOS        = "com.safezone.action.SOS"
        const val ACTION_STATUS     = "com.safezone.action.STATUS"

        const val EXTRA_STATUS      = "extra_status"

        // Thresholds
        private const val G_FORCE_THRESHOLD      = 12.0f   // m/s² – pickup motion
        private const val HIGH_GFORCE_THRESHOLD   = 20.0f  // m/s² – impact
        private const val AUDIO_MAX_DURATION_MS   = 45 * 60 * 1000L
        private const val VIDEO_MAX_DURATION_MS   = 30 * 60 * 1000L
        private const val GPS_INTERVAL_MS         = 5 * 60 * 1000L  // 5 min
        private const val GPS_EXPIRE_MS           = 45 * 60 * 1000L // 45 min

        val isRunning = AtomicBoolean(false)
    }

    // ─── Injected Dependencies ────────────────────────────────────────────────
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var contactRepository: ContactRepository
    @Inject lateinit var securityRepository: SecurityRepository
    @Inject lateinit var vaultRepository: VaultRepository

    // ─── System Services ──────────────────────────────────────────────────────
    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var speechRecognizer: SpeechRecognizer? = null
    private var mediaRecorder: MediaRecorder? = null

    // ─── State ────────────────────────────────────────────────────────────────
    private var isTriggered        = false
    private var isRecordingAudio   = false
    private var isRecordingVideo   = false
    private var triggerStartTime   = 0L
    private var gpsTrackingExpiry  = 0L
    private var currentLat         = 0.0
    private var currentLng         = 0.0
    private var activePhrases      = listOf<String>()

    private val handler = Handler(Looper.getMainLooper())
    private var audioStopRunnable: Runnable? = null
    private var videoStopRunnable: Runnable? = null

    // ─── Lifecycle ────────────────────────────────────────────────────────────
    override fun onCreate() {
        super.onCreate()
        sensorManager       = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        isRunning.set(true)
        Log.i(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START  -> startSafeZone()
            ACTION_STOP   -> stopSafeZone()
            ACTION_SOS    -> lifecycleScope.launch { triggerEmergencySequence(isSos = true) }
            else          -> startSafeZone()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
        isRunning.set(false)
        Log.i(TAG, "Service destroyed – will restart via sticky intent")
    }

    // ─── Core Startup ─────────────────────────────────────────────────────────
    private fun startSafeZone() {
        promoteToForeground()
        registerAccelerometer()
        startLocationUpdates()
        lifecycleScope.launch {
            activePhrases = securityRepository.getPhrases().map { it.phrase }
            sessionManager.setServiceRunning(true)
            startSpeechRecognitionLoop()
        }
        Log.i(TAG, "SafeZone monitoring active — phrases: $activePhrases")
    }

    private fun stopSafeZone() {
        lifecycleScope.launch { sessionManager.setServiceRunning(false) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ─── Foreground Notification ───────────────────────────────────────────────
    private fun promoteToForeground() {
    val notif = buildServiceNotification("SYSTEM ARMED — Monitoring Active")

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Build the foreground service type flags only for permissions
        // that are actually granted right now — avoids SecurityException on API 34+
        var serviceType = 0

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED) {
            serviceType = serviceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            serviceType = serviceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            serviceType = serviceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        }

        try {
            if (serviceType != 0) {
                startForeground(SERVICE_NOTIF_ID, notif, serviceType)
            } else {
                // No typed permissions granted yet — start without a type
                startForeground(SERVICE_NOTIF_ID, notif)
            }
        } catch (e: Exception) {
            Log.e(TAG, "startForeground failed: ${e.message} — falling back")
            startForeground(SERVICE_NOTIF_ID, notif)
        }
    } else {
        startForeground(SERVICE_NOTIF_ID, notif)
    }
}

    private fun buildServiceNotification(status: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setContentTitle("SAFEZONE")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    // ─── Speech Recognition Loop ───────────────────────────────────────────────
    private fun startSpeechRecognitionLoop() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.w(TAG, "SpeechRecognizer not available on this device")
            return
        }
        scheduleSpeechRecognition()
    }

    private fun scheduleSpeechRecognition() {
        handler.post { initAndStartRecognizer() }
    }

    private fun initAndStartRecognizer() {
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results
                    ?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                    ?: return
                matches.forEach { heard ->
                    activePhrases.forEach { phrase ->
                        if (heard.contains(phrase, ignoreCase = true)) {
                            Log.i(TAG, "Trigger phrase detected: '$phrase'")
                            lifecycleScope.launch { triggerEmergencySequence(isSos = false) }
                            return
                        }
                    }
                }
                // Restart loop unless triggered
                if (!isTriggered) handler.postDelayed({ initAndStartRecognizer() }, 500)
            }

            override fun onError(error: Int) {
                Log.w(TAG, "SpeechRecognizer error: $error — restarting in 2s")
                if (!isTriggered) handler.postDelayed({ initAndStartRecognizer() }, 2000)
            }

            override fun onReadyForSpeech(params: Bundle?)      {}
            override fun onBeginningOfSpeech()                  {}
            override fun onRmsChanged(rmsdB: Float)             {}
            override fun onBufferReceived(buffer: ByteArray?)   {}
            override fun onEndOfSpeech()                        {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
        }
        try { speechRecognizer?.startListening(intent) }
        catch (e: Exception) { Log.e(TAG, "Error starting recognizer: ${e.message}") }
    }

    // ─── Emergency Trigger Sequence ────────────────────────────────────────────
    private suspend fun triggerEmergencySequence(isSos: Boolean) {
        if (isTriggered) return
        isTriggered    = true
        triggerStartTime = System.currentTimeMillis()

        Log.i(TAG, "🚨 EMERGENCY SEQUENCE INITIATED (SOS=$isSos)")
        updateNotification("🚨 EMERGENCY — Recording & Dispatching...")

        val contacts = contactRepository.getContacts()

        // Parallel execution of emergency actions
        withContext(Dispatchers.IO) {
            launch { startAudioRecording() }
            launch { fetchLocationAndDispatch(contacts, isSos) }
            if (isSos) {
                launch { dispatchSosCalls(contacts) }
            }
        }

        // Schedule GPS loop for 45 min
        gpsTrackingExpiry = System.currentTimeMillis() + GPS_EXPIRE_MS
        schedulePeriodicGpsUpdate()
    }

    // ─── Audio Recording ──────────────────────────────────────────────────────
    private suspend fun startAudioRecording() {
        if (isRecordingAudio) return
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) return

        val outFile = createVaultFile("AUDIO_LOG", "m4a")
        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION") MediaRecorder()
            }
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outFile.absolutePath)
                setMaxDuration(AUDIO_MAX_DURATION_MS.toInt())
                prepare()
                start()
            }
            isRecordingAudio = true
            Log.i(TAG, "Audio recording started → ${outFile.name}")

            // Persist record to vault DB
            vaultRepository.insert(
                VaultFile(
                    fileName  = outFile.name,
                    filePath  = outFile.absolutePath,
                    fileType  = FileType.AUDIO,
                    tag       = FileTag.VOICE_LOG,
                    createdAt = System.currentTimeMillis()
                )
            )

            // Auto-stop after 45 min
            audioStopRunnable = Runnable { stopAudioRecording() }
            handler.postDelayed(audioStopRunnable!!, AUDIO_MAX_DURATION_MS)

        } catch (e: Exception) {
            Log.e(TAG, "Audio recording failed: ${e.message}")
        }
    }

    private fun stopAudioRecording() {
        try {
            mediaRecorder?.apply { stop(); release() }
            mediaRecorder    = null
            isRecordingAudio = false
            Log.i(TAG, "Audio recording stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio: ${e.message}")
        }
    }

    // ─── Silent Video Recording ────────────────────────────────────────────────
    private fun startSilentVideoRecording() {
        if (isRecordingVideo) return
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) return

        isRecordingVideo = true
        Log.i(TAG, "Silent video recording initiated (SurfaceTexture, no preview)")
        // CameraX recording via SurfaceTexture is handled in the ViewModel/CameraHelper;
        // here we set the flag and notify the service state.
        // Full CameraX headless recording is implemented in CameraHelper.kt
        lifecycleScope.launch {
            vaultRepository.insert(
                VaultFile(
                    fileName  = "SEC_FEED_${System.currentTimeMillis()}.mp4",
                    filePath  = createVaultFile("SEC_FEED", "mp4").absolutePath,
                    fileType  = FileType.VIDEO,
                    tag       = FileTag.ENCRYPTED,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        videoStopRunnable = Runnable {
            isRecordingVideo = false
            Log.i(TAG, "Silent video recording stopped")
        }
        handler.postDelayed(videoStopRunnable!!, VIDEO_MAX_DURATION_MS)
    }

    // ─── Location & SMS Dispatch ───────────────────────────────────────────────
    private suspend fun fetchLocationAndDispatch(
        contacts: List<com.safezone.data.db.Contact>,
        isSos: Boolean
    ) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                currentLat = it.latitude
                currentLng = it.longitude
                val coordMsg = buildSmsMessage(it.latitude, it.longitude, isSos)
                dispatchSms(contacts, coordMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Location fetch failed: ${e.message}")
            // Send SMS without coordinates if location unavailable
            dispatchSms(contacts, "🚨 SafeZone ALERT — Location unavailable. User may need assistance.")
        }
    }

    private fun buildSmsMessage(lat: Double, lng: Double, isSos: Boolean): String {
        val ts   = SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault()).format(Date())
        val type = if (isSos) "SOS" else "ALERT"
        return "🚨 SAFEZONE $type — $ts\n" +
               "GPS: https://maps.google.com/?q=$lat,$lng\n" +
               "Coordinates: $lat, $lng\n" +
               "Immediate attention may be required."
    }

    @Suppress("DEPRECATION")
    private fun dispatchSms(contacts: List<com.safezone.data.db.Contact>, message: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) return

        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }

        contacts.forEach { contact ->
            try {
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(contact.phone, null, parts, null, null)
                Log.i(TAG, "SMS dispatched to ${contact.name} (${contact.phone})")
            } catch (e: Exception) {
                Log.e(TAG, "SMS failed to ${contact.phone}: ${e.message}")
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun dispatchSosCalls(contacts: List<com.safezone.data.db.Contact>) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED) return

        contacts.take(1).forEach { contact ->
            try {
                val callIntent = Intent(Intent.ACTION_CALL).apply {
                    data  = android.net.Uri.parse("tel:${contact.phone}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(callIntent)
                Log.i(TAG, "SOS call initiated to ${contact.name}")
            } catch (e: Exception) {
                Log.e(TAG, "SOS call failed: ${e.message}")
            }
        }
    }

    // ─── GPS Periodic Update (5-min intervals, 45-min expiry) ─────────────────
    private fun schedulePeriodicGpsUpdate() {
        if (System.currentTimeMillis() >= gpsTrackingExpiry) {
            Log.i(TAG, "GPS tracking session expired")
            return
        }
        handler.postDelayed({
            lifecycleScope.launch {
                sendGpsUpdate()
                schedulePeriodicGpsUpdate()
            }
        }, GPS_INTERVAL_MS)
    }

    private suspend fun sendGpsUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return
        try {
            val location = fusedLocationClient.lastLocation.await() ?: return
            val contacts = contactRepository.getContacts()
            val msg      = "📍 SAFEZONE UPDATE — ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}\n" +
                           "GPS: https://maps.google.com/?q=${location.latitude},${location.longitude}"
            dispatchSms(contacts, msg)
        } catch (e: Exception) {
            Log.e(TAG, "GPS update failed: ${e.message}")
        }
    }

    // ─── Accelerometer (SensorEventListener) ──────────────────────────────────
    private fun registerAccelerometer() {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val gForce = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        when {
            // High-impact (drop / hard motion) — start GPS tracking
            gForce > HIGH_GFORCE_THRESHOLD && !isTriggered -> {
                Log.i(TAG, "High G-force detected: $gForce m/s²")
                gpsTrackingExpiry = System.currentTimeMillis() + GPS_EXPIRE_MS
                schedulePeriodicGpsUpdate()
            }
            // Pick-up motion after trigger — start silent video
            gForce > G_FORCE_THRESHOLD && isTriggered && !isRecordingVideo -> {
                Log.i(TAG, "Pick-up motion detected post-trigger — starting video")
                startSilentVideoRecording()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ─── Location Updates for Dashboard ───────────────────────────────────────
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        val req = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 30_000L)
            .setMinUpdateIntervalMillis(15_000L)
            .build()

        fusedLocationClient.requestLocationUpdates(req, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    currentLat = it.latitude
                    currentLng = it.longitude
                }
            }
        }, Looper.getMainLooper())
    }

    // ─── Utility ──────────────────────────────────────────────────────────────
    private suspend fun createVaultFile(prefix: String, ext: String): File {
        val dir = securityRepository.getStorageDir()
        val ts  = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(dir, "${prefix}_${ts}.$ext")
    }

    private fun updateNotification(status: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(SERVICE_NOTIF_ID, buildServiceNotification(status))
    }

    private fun cleanup() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            sensorManager.unregisterListener(this)
            stopAudioRecording()
            audioStopRunnable?.let { handler.removeCallbacks(it) }
            videoStopRunnable?.let { handler.removeCallbacks(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup error: ${e.message}")
        }
    }

}
