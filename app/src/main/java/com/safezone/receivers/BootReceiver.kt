package com.safezone.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.safezone.SecurityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.bootDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "safezone_prefs")

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val validActions = listOf(
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_MY_PACKAGE_REPLACED
        )
        if (intent.action !in validActions) return

        Log.i("SafeZone::Boot", "Boot event — checking session state")

        val authKey = booleanPreferencesKey("is_authenticated")
        CoroutineScope(Dispatchers.IO).launch {
            val isAuthenticated = context.bootDataStore.data
                .catch { emit(emptyPreferences()) }
                .map { it[authKey] ?: false }
                .first()

            if (isAuthenticated) {
                Log.i("SafeZone::Boot", "Session valid — restarting SecurityService")
                val svcIntent = Intent(context, SecurityService::class.java).apply {
                    action = SecurityService.ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(svcIntent)
                } else {
                    context.startService(svcIntent)
                }
            }
        }
    }
}
