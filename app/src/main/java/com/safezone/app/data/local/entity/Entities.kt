package com.safezone.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sos_event_cache")
data class SosEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val lat: Double,
    val lng: Double,
    val startedAt: String,
    val endedAt: String?,
    val durationSeconds: Long,
    val status: String,
    val triggerSource: String,
    val audioUrl: String?,
    val imageUrl: String?,
    val note: String?
)

@Entity(tableName = "incoming_alerts")
data class IncomingAlertEntity(
    @PrimaryKey val eventId: String,
    val userId: String,
    val userName: String,
    val userPhotoUrl: String?,
    val lat: Double,
    val lng: Double,
    val distanceMeters: Double,
    val triggerSource: String,
    val receivedAt: Long,
    val resolved: Boolean = false
)

@Entity(tableName = "pending_audio_chunks")
data class PendingAudioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: String,
    val filePath: String,
    val createdAt: Long = System.currentTimeMillis()
)
