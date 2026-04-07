package com.safezone

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SafeZoneApplication : Application() {

    companion object {
        const val SERVICE_CHANNEL_ID  = "safezone_service_channel"
        const val ALERT_CHANNEL_ID    = "safezone_alert_channel"
        const val SERVICE_NOTIF_ID    = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            NotificationChannel(
                SERVICE_CHANNEL_ID,
                "Safe Zone Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps SafeZone monitoring active in background"
                setShowBadge(false)
                manager.createNotificationChannel(this)
            }

            NotificationChannel(
                ALERT_CHANNEL_ID,
                "Safe Zone Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency alert notifications"
                enableVibration(true)
                manager.createNotificationChannel(this)
            }
        }
    }
}
