package com.safezone.app.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    @SerialName("full_name") val fullName: String = "",
    val phone: String = "",
    val address: String = "",
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("emergency_contacts") val emergencyContacts: List<EmergencyContact> = emptyList(),
    @SerialName("fcm_token") val fcmToken: String? = null,
    @SerialName("last_lat") val lastLat: Double? = null,
    @SerialName("last_lng") val lastLng: Double? = null,
    @SerialName("last_seen_at") val lastSeenAt: String? = null
)

@Serializable
data class EmergencyContact(
    val name: String,
    val phone: String
)

@Serializable
data class SecurityPhrase(
    val id: String,
    @SerialName("user_id") val userId: String,
    val text: String,
    val action: PhraseAction,
    @SerialName("voice_url") val voiceUrl: String? = null,
    val enabled: Boolean = true
)

@Serializable
enum class PhraseAction {
    @SerialName("notify_contacts") NOTIFY_CONTACTS,
    @SerialName("silent_police") SILENT_POLICE,
    @SerialName("full_sos") FULL_SOS
}

@Serializable
data class SosEvent(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String = "",
    @SerialName("user_photo_url") val userPhotoUrl: String? = null,
    val lat: Double,
    val lng: Double,
    @SerialName("started_at") val startedAt: String,
    @SerialName("ended_at") val endedAt: String? = null,
    @SerialName("duration_seconds") val durationSeconds: Long = 0,
    val status: SosStatus = SosStatus.ACTIVE,
    @SerialName("trigger_source") val triggerSource: TriggerSource = TriggerSource.MANUAL,
    @SerialName("audio_url") val audioUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val note: String? = null
)

@Serializable
enum class SosStatus {
    @SerialName("active") ACTIVE,
    @SerialName("resolved") RESOLVED,
    @SerialName("canceled") CANCELED,
    @SerialName("high_alert") HIGH_ALERT
}

@Serializable
enum class TriggerSource {
    @SerialName("manual") MANUAL,
    @SerialName("phrase") PHRASE,
    @SerialName("geofence") GEOFENCE,
    @SerialName("ble") BLE
}

@Serializable
data class NearbyUser(
    val id: String,
    val name: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    val lat: Double,
    val lng: Double,
    @SerialName("distance_meters") val distanceMeters: Double
)

@Serializable
data class IncomingAlert(
    val eventId: String,
    val userId: String,
    val userName: String,
    val userPhotoUrl: String? = null,
    val lat: Double,
    val lng: Double,
    val distanceMeters: Double,
    val triggerSource: TriggerSource,
    val receivedAt: Long = System.currentTimeMillis()
)
