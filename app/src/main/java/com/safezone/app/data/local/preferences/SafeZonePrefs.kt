package com.safezone.app.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "safezone_prefs")

@Singleton
class SafeZonePrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val LISTENING_ENABLED = booleanPreferencesKey("listening_enabled")
        val BLE_FALLBACK_ENABLED = booleanPreferencesKey("ble_fallback_enabled")
        val LAST_FCM_TOKEN = stringPreferencesKey("last_fcm_token")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BIOMETRIC_ENABLED] ?: false }
    val listeningEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.LISTENING_ENABLED] ?: false }
    val bleFallbackEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BLE_FALLBACK_ENABLED] ?: true }
    val fcmToken: Flow<String?> = context.dataStore.data.map { it[Keys.LAST_FCM_TOKEN] }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    suspend fun setBiometricEnabled(v: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = v }
    }
    suspend fun setListeningEnabled(v: Boolean) {
        context.dataStore.edit { it[Keys.LISTENING_ENABLED] = v }
    }
    suspend fun setBleFallbackEnabled(v: Boolean) {
        context.dataStore.edit { it[Keys.BLE_FALLBACK_ENABLED] = v }
    }
    suspend fun setFcmToken(v: String) {
        context.dataStore.edit { it[Keys.LAST_FCM_TOKEN] = v }
    }
    suspend fun setOnboardingDone(v: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = v }
    }
}
