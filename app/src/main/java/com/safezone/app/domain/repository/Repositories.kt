package com.safezone.app.domain.repository

import com.safezone.app.domain.models.AppResult
import com.safezone.app.domain.models.IncomingAlert
import com.safezone.app.domain.models.NearbyUser
import com.safezone.app.domain.models.SecurityPhrase
import com.safezone.app.domain.models.SosEvent
import com.safezone.app.domain.models.SosStatus
import com.safezone.app.domain.models.TriggerSource
import com.safezone.app.domain.models.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: Flow<String?>
    suspend fun signUp(email: String, password: String): AppResult<String>
    suspend fun signIn(email: String, password: String): AppResult<String>
    suspend fun signOut(): AppResult<Unit>
    suspend fun refreshSession(): AppResult<Unit>
}

interface ProfileRepository {
    fun observeProfile(userId: String): Flow<UserProfile?>
    suspend fun getProfile(userId: String): AppResult<UserProfile>
    suspend fun upsertProfile(profile: UserProfile): AppResult<Unit>
    suspend fun uploadAvatar(userId: String, bytes: ByteArray): AppResult<String>
    suspend fun updateFcmToken(userId: String, token: String): AppResult<Unit>
}

interface PhraseRepository {
    fun observePhrases(userId: String): Flow<List<SecurityPhrase>>
    suspend fun savePhrase(phrase: SecurityPhrase): AppResult<Unit>
    suspend fun deletePhrase(id: String): AppResult<Unit>
    suspend fun uploadVoiceSample(phraseId: String, bytes: ByteArray): AppResult<String>
}

interface SosRepository {
    fun observeHistory(userId: String): Flow<List<SosEvent>>
    fun observeActiveEvent(userId: String): Flow<SosEvent?>
    suspend fun createEvent(
        userId: String,
        lat: Double,
        lng: Double,
        triggerSource: TriggerSource
    ): AppResult<SosEvent>
    suspend fun updateEventLocation(eventId: String, lat: Double, lng: Double): AppResult<Unit>
    suspend fun endEvent(eventId: String, status: SosStatus): AppResult<Unit>
    suspend fun attachAudio(eventId: String, audioUrl: String): AppResult<Unit>
    suspend fun attachImage(eventId: String, imageUrl: String): AppResult<Unit>
    suspend fun uploadAudioChunk(eventId: String, bytes: ByteArray): AppResult<String>
    suspend fun uploadImage(eventId: String, bytes: ByteArray): AppResult<String>
    suspend fun getNearbyUsers(lat: Double, lng: Double, radiusMeters: Int = 50): AppResult<List<NearbyUser>>
}

interface AlertRepository {
    /** Realtime stream of incoming SOS alerts targeted at current user. */
    fun observeIncomingAlerts(userId: String): Flow<IncomingAlert>
    suspend fun acknowledgeAlert(alertId: String): AppResult<Unit>
}

interface LocationRepository {
    fun locationUpdates(): Flow<LocationPoint>
    suspend fun currentLocation(): LocationPoint?
    suspend fun publishLocation(userId: String, lat: Double, lng: Double): AppResult<Unit>
}

data class LocationPoint(val lat: Double, val lng: Double, val accuracyM: Float, val timestamp: Long)
