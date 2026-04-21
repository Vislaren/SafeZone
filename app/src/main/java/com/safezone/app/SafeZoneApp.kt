package com.safezone.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SafeZoneApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java)

        nm.createNotificationChannel(
            NotificationChannel(
                CH_SOS_INCOMING, getString(R.string.channel_sos_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_sos_desc)
                enableVibration(true)
                setBypassDnd(true)
            }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CH_SOS_ACTIVE, getString(R.string.channel_sos_active_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = getString(R.string.channel_sos_active_desc) }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CH_LISTENING, getString(R.string.channel_listening_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = getString(R.string.channel_listening_desc) }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CH_BLE, getString(R.string.channel_ble_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = getString(R.string.channel_ble_desc) }
        )
    }

    companion object {
        const val CH_SOS_INCOMING = "safezone_alerts"
        const val CH_SOS_ACTIVE = "safezone_sos_active"
        const val CH_LISTENING = "safezone_listening"
        const val CH_BLE = "safezone_ble"
    }
}
