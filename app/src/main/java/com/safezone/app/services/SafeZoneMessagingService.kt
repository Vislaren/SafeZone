package com.safezone.app.services

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.safezone.app.MainActivity
import com.safezone.app.R
import com.safezone.app.SafeZoneApp
import com.safezone.app.data.local.preferences.SafeZonePrefs
import com.safezone.app.domain.repository.AuthRepository
import com.safezone.app.domain.repository.ProfileRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SafeZoneMessagingService : FirebaseMessagingService() {

    @Inject lateinit var authRepo: AuthRepository
    @Inject lateinit var profileRepo: ProfileRepository
    @Inject lateinit var prefs: SafeZonePrefs

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch {
            prefs.setFcmToken(token)
            authRepo.currentUserId.firstOrNull()?.let { uid ->
                profileRepo.updateFcmToken(uid, token)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = data["type"] ?: "generic"
        val eventId = data["event_id"] ?: ""
        val userName = data["user_name"] ?: "Unknown"
        val distance = data["distance_meters"] ?: "0"

        val title = when (type) {
            "sos_alert" -> "DISTRESS SIGNAL"
            else -> message.notification?.title ?: "SafeZone"
        }
        val body = when (type) {
            "sos_alert" -> "$userName — ${distance}m away. Tap to respond."
            else -> message.notification?.body.orEmpty()
        }

        val deepLink = "safezone://alert?id=$eventId".toUri()
        val intent = android.content.Intent(this, MainActivity::class.java)
            .setData(deepLink)
            .setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pending = PendingIntent.getActivity(
            this, eventId.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(this, SafeZoneApp.CH_SOS_INCOMING)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        val canNotify = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

        if (canNotify) {
            androidx.core.app.NotificationManagerCompat.from(this)
                .notify(eventId.hashCode(), notif)
        }
    }
}
