package com.safezone.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "safezone_prefs")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_IS_AUTHENTICATED  = booleanPreferencesKey("is_authenticated")
        val KEY_SESSION_TOKEN     = stringPreferencesKey("session_token")
        val KEY_PHONE_NUMBER      = stringPreferencesKey("phone_number")
        val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val KEY_IMPACT_THRESHOLD  = floatPreferencesKey("impact_threshold")
        val KEY_ACCEL_SENSITIVITY = floatPreferencesKey("accel_sensitivity")
        val KEY_SERVICE_RUNNING   = booleanPreferencesKey("service_running")
        val KEY_ACTIVE_PHRASE     = stringPreferencesKey("active_phrase")
        val KEY_ONBOARDING_DONE   = booleanPreferencesKey("onboarding_done")
    }

    /** Safe dataStore.data — emits emptyPreferences() on IOException */
    private val safeData: Flow<Preferences> = context.dataStore.data
        .catch { emit(emptyPreferences()) }

    val isAuthenticated: Flow<Boolean> = safeData.map { it[KEY_IS_AUTHENTICATED] ?: false }
    val sessionToken:    Flow<String?> = safeData.map { it[KEY_SESSION_TOKEN] }
    val phoneNumber:     Flow<String?> = safeData.map { it[KEY_PHONE_NUMBER] }
    val biometricEnabled: Flow<Boolean> = safeData.map { it[KEY_BIOMETRIC_ENABLED] ?: false }
    val impactThreshold:  Flow<Float>   = safeData.map { it[KEY_IMPACT_THRESHOLD]  ?: 0.82f }
    val accelSensitivity: Flow<Float>   = safeData.map { it[KEY_ACCEL_SENSITIVITY] ?: 0.45f }
    val isServiceRunning: Flow<Boolean> = safeData.map { it[KEY_SERVICE_RUNNING]   ?: false }
    val activePhrase:     Flow<String>  = safeData.map { it[KEY_ACTIVE_PHRASE] ?: "Protocol Omega Delta" }
    val onboardingDone:   Flow<Boolean> = safeData.map { it[KEY_ONBOARDING_DONE]   ?: false }

    suspend fun setAuthenticated(v: Boolean)  = context.dataStore.edit { it[KEY_IS_AUTHENTICATED] = v }
    suspend fun setSessionToken(token: String)= context.dataStore.edit { it[KEY_SESSION_TOKEN]    = token }
    suspend fun setPhoneNumber(phone: String) = context.dataStore.edit { it[KEY_PHONE_NUMBER]     = phone }
    suspend fun setBiometricEnabled(v: Boolean)= context.dataStore.edit { it[KEY_BIOMETRIC_ENABLED]= v }
    suspend fun setImpactThreshold(v: Float)  = context.dataStore.edit { it[KEY_IMPACT_THRESHOLD] = v }
    suspend fun setAccelSensitivity(v: Float) = context.dataStore.edit { it[KEY_ACCEL_SENSITIVITY]= v }
    suspend fun setServiceRunning(v: Boolean) = context.dataStore.edit { it[KEY_SERVICE_RUNNING]  = v }
    suspend fun setActivePhrase(phrase: String)= context.dataStore.edit { it[KEY_ACTIVE_PHRASE]   = phrase }
    suspend fun setOnboardingDone(v: Boolean) = context.dataStore.edit { it[KEY_ONBOARDING_DONE]  = v }

    suspend fun clearSession() {
        context.dataStore.edit {
            it[KEY_IS_AUTHENTICATED] = false
            it.remove(KEY_SESSION_TOKEN)
        }
    }
}
