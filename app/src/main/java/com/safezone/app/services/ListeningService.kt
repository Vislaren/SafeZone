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
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Background wake-word service. Uses Android's on-device SpeechRecognizer as a fallback
 * (which is battery-heavy in continuous mode). For production you should swap
 * [createRecognizer] / [startListening] with Porcupine or Vosk.
 */
@AndroidEntryPoint
class ListeningService : LifecycleService() {

    @Inject lateinit var phraseRepo: PhraseRepository
    @Inject lateinit var authRepo: AuthRepository

    private var recognizer: SpeechRecognizer? = null
    private var targetPhrases: List<Pair<String, PhraseAction>> = emptyList()

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
        targetPhrases = phraseRepo.observePhrases(userId).firstOrNull()
            ?.filter { it.enabled }
            ?.map { it.text to it.action }
            ?: emptyList()
        if (targetPhrases.isEmpty()) { stopSelf(); return }
        startListening()
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            // Device has no on-device recognizer. Stop quietly; the app should warn the user.
            stopSelf(); return
        }
        recognizer?.destroy()
        val r = SpeechRecognizer.createOnDeviceSpeechRecognizer(this)
        recognizer = r

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
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
                // Restart; SpeechRecognizer doesn't loop natively.
                r.startListening(intent)
            }
            override fun onError(error: Int) {
                // Back off briefly then retry — common in real-world usage.
                lifecycleScope.launch {
                    kotlinx.coroutines.delay(1500)
                    runCatching { r.startListening(intent) }
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
        // All actions currently route into full SOS — tune these for silent variants later.
        when (action) {
            PhraseAction.FULL_SOS,
            PhraseAction.NOTIFY_CONTACTS,
            PhraseAction.SILENT_POLICE -> SOSService.start(this, TriggerSource.PHRASE)
        }
        // Stop listening while SOS is running; the SOSService will own the mic.
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
        runCatching { recognizer?.stopListening(); recognizer?.destroy() }
        recognizer = null
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
