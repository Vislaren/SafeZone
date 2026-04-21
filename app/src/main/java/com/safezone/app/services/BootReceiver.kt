package com.safezone.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.safezone.app.data.local.preferences.SafeZonePrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var prefs: SafeZonePrefs

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (prefs.listeningEnabled.firstOrNull() == true) {
                    ListeningService.start(context)
                }
                if (prefs.bleFallbackEnabled.firstOrNull() == true) {
                    BLEService.scan(context)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
