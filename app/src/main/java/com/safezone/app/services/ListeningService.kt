package com.safezone.app.services

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.safezone.app.MainActivity
import com.safezone.app.R
import com.safezone.app.SafeZoneApp
import com.safezone.app.domain.models.PhraseAction
import com.safezone.app.domain.models.TriggerSource
import com.safezone.app.domain.repository.AuthRepository
import com.safezone.app.domain.repository.PhraseRepository
import com.safezone.app.utils.PhraseMatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ListeningService : LifecycleService() {

    @Inject lateinit var phraseRepo: PhraseRepository
    @Inject lateinit var authRepo: AuthRepository

    private var recognizer: SpeechRecognizer? = null
    private var targetPhrases: List<Pair<String, PhraseAction>> = emptyList()

    // FIX Bug 3: flag to stop re-starting the recognizer once we're shutting down
    @Volatile private var destroyed = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (!hasMicPermission()) { stopSelf(); return START_NOT_STICKY }
        startForegroundWithNotif()
        lifecycleScope.launch { bootstrap() }
        return START_STICKY
    }

    private fun hasMicPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED

    private suspend fun bootstrap() {
        val userId = authRepo.currentUserId.firstOrNull() ?: run { stopSelf(); return }
        phraseRepo.observePhrases(userId).collectLatest { list ->
            val enabled = list.filter { it.enabled }
            targetPhrases = enabled.map { it.text to it.action }

            if (targetPhrases.isEmpty()) {
                // FIX Bug 4: properly destroy before nulling so the old callbacks are dead
                destroyRecognizer()
            } else {
                if (recognizer == null && !destroyed) {
                    startListening()
                }
            }
        }
    }

    // FIX Bug 4: centralise destroy so collectLatest re-runs always get a clean slate
    private fun destroyRecognizer() {
        runCatching { recognizer?.stopListening(); recognizer?.destroy() }
        recognizer = null
    }

    private fun startListening() {
        if (destroyed) return

        // FIX Bug 1: isRecognitionAvailable() only checks for ANY recognizer (including online).
        // createOnDeviceSpeechRecognizer() then fails silently on most devices because it needs
        // Android 13+ AND a separately-installed on-device model.
        // Use the standard factory instead — it resolves to the device's default engine (Google).
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            stopSelf(); return
        }

        destroyRecognizer() // FIX Bug 4: kill any previous instance before creating a new one

        // FIX Bug 1: createSpeechRecognizer() works on all API levels and all devices
        val r = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer = r

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            // FIX Bug 2: required when called from a background/foreground service;
            // without this many recognizer implementations fire onError(ERROR_CLIENT) immediately.
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }

        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partial: Bundle?) = checkResults(partial)

            override fun onResults(results: Bundle?) {
                checkResults(results)
                // FIX Bug 3: don't restart if a phrase was just detected and we called stopSelf(),
                // or if the service is already being torn down.
                if (!destroyed && recognizer != null) {
                    runCatching { r.startListening(intent) }
                }
            }

            override fun onError(error: Int) {
                // Back off briefly then retry — common in real-world usage.
                lifecycleScope.launch {
                    kotlinx.coroutines.delay(1500)
                    // FIX Bug 3: guard here too so the retry loop stops when we're destroying
                    if (!destroyed && recognizer != null) {
                        runCatching { r.startListening(intent) }
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        r.startListening(intent)
    }

    private fun checkResults(bundle: Bundle?) {
        val heard = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.joinToString(" ")?.ifBlank { null } ?: return
        val hit = targetPhrases.firstOrNull { PhraseMatcher.matches(heard, it.first) } ?: return
        onPhraseDetected(hit.second)
    }

    private fun onPhraseDetected(action: PhraseAction) {
        // FIX Bug 3: set destroyed BEFORE stopSelf() so the recognizer callbacks see it
        // immediately and don't attempt to restart listening.
        destroyed = true
        destroyRecognizer()

        when (action) {
            PhraseAction.FULL_SOS,
            PhraseAction.NOTIFY_CONTACTS,
            PhraseAction.SILENT_POLICE -> SOSService.start(this, TriggerSource.PHRASE)
        }

        stopSelf()
    }

    private fun startForegroundWithNotif() {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n = NotificationCompat.Builder(this, SafeZoneApp.CH_LISTENING)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle(getString(R.string.notif_listening_title))
            .setContentText(getString(R.string.notif_listening_text))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(open)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIF_ID, n)
        }
    }

    override fun onDestroy() {
        destroyed = true       // FIX Bug 3: ensure all callbacks stop retrying
        destroyRecognizer()    // FIX Bug 4: use the centralised helper
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? { super.onBind(intent); return null }

    companion object {
        const val NOTIF_ID = 43

        fun start(context: Context) {
            context.startForegroundService(Intent(context, ListeningService::class.java))
        }
        fun stop(context: Context) {
            context.stopService(Intent(context, ListeningService::class.java))
        }
    }
}