package com.safezone.app.data.repository

import com.safezone.app.data.remote.supabase.SupabaseClientProvider
import com.safezone.app.data.remote.supabase.SupabaseSchema
import com.safezone.app.domain.models.AppResult
import com.safezone.app.domain.models.NearbyUser
import com.safezone.app.domain.models.SosEvent
import com.safezone.app.domain.models.SosStatus
import com.safezone.app.domain.models.TriggerSource
import com.safezone.app.domain.repository.SosRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SosRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClientProvider
) : SosRepository {

    override fun observeHistory(userId: String): Flow<List<SosEvent>> = flow {
        val rows = runCatching {
            supabase.client.postgrest[SupabaseSchema.T_SOS_EVENTS]
                .select {
                    filter { eq("user_id", userId) }
                    order("started_at", Order.DESCENDING)
                    limit(100)
                }
                .decodeList<SosEvent>()
        }.getOrDefault(emptyList())
        emit(rows)
    }

    override fun observeActiveEvent(userId: String): Flow<SosEvent?> = flow {
        val row = runCatching {
            supabase.client.postgrest[SupabaseSchema.T_SOS_EVENTS]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("status", "active")
                    }
                    order("started_at", Order.DESCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<SosEvent>()
        }.getOrNull()
        emit(row)
    }

    override suspend fun createEvent(
        userId: String,
        lat: Double,
        lng: Double,
        triggerSource: TriggerSource
    ): AppResult<SosEvent> = runCatching {
        val event = SosEvent(
            id = UUID.randomUUID().toString(),
            userId = userId,
            lat = lat,
            lng = lng,
            startedAt = Instant.now().toString(),
            triggerSource = triggerSource,
            status = SosStatus.ACTIVE
        )
        supabase.client.postgrest[SupabaseSchema.T_SOS_EVENTS].insert(event)
        // Ask the backend to fan out the alert to nearby users & push FCM.
        supabase.client.postgrest.rpc(
            SupabaseSchema.RPC_BROADCAST_SOS,
            buildJsonObject {
                put("event_id", event.id)
                put("radius_meters", 50)
            }
        )
        AppResult.Success(event)
    }.getOrElse { AppResult.Error(it.message ?: "SOS create failed", it) }

    override suspend fun updateEventLocation(eventId: String, lat: Double, lng: Double): AppResult<Unit> =
        runCatching {
            supabase.client.postgrest[SupabaseSchema.T_SOS_EVENTS].update(
                mapOf("lat" to lat, "lng" to lng)
            ) { filter { eq("id", eventId) } }
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Error(it.message ?: "Location update failed", it) }

    override suspend fun endEvent(eventId: String, status: SosStatus): AppResult<Unit> = runCatching {
        supabase.client.postgrest[SupabaseSchema.T_SOS_EVENTS].update(
            mapOf(
                "status" to status.name.lowercase(),
                "ended_at" to Instant.now().toString()
            )
        ) { filter { eq("id", eventId) } }
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error(it.message ?: "End failed", it) }

    override suspend fun attachAudio(eventId: String, audioUrl: String): AppResult<Unit> = runCatching {
        supabase.client.postgrest[SupabaseSchema.T_SOS_EVENTS].update(
            mapOf("audio_url" to audioUrl)
        ) { filter { eq("id", eventId) } }
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error(it.message ?: "Attach failed", it) }

    override suspend fun attachImage(eventId: String, imageUrl: String): AppResult<Unit> = runCatching {
        supabase.client.postgrest[SupabaseSchema.T_SOS_EVENTS].update(
            mapOf("image_url" to imageUrl)
        ) { filter { eq("id", eventId) } }
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error(it.message ?: "Attach failed", it) }

    override suspend fun uploadAudioChunk(eventId: String, bytes: ByteArray): AppResult<String> =
        runCatching {
            val path = "$eventId/${System.currentTimeMillis()}.m4a"
            supabase.client.storage.from(SupabaseSchema.BUCKET_SOS_AUDIO)
                .upload(path, bytes) { upsert = false }
            AppResult.Success(
                supabase.client.storage.from(SupabaseSchema.BUCKET_SOS_AUDIO).publicUrl(path)
            )
        }.getOrElse { AppResult.Error(it.message ?: "Upload failed", it) }

    override suspend fun uploadImage(eventId: String, bytes: ByteArray): AppResult<String> =
        runCatching {
            val path = "$eventId/snapshot.jpg"
            supabase.client.storage.from(SupabaseSchema.BUCKET_SOS_IMAGES)
                .upload(path, bytes) { upsert = true }
            AppResult.Success(
                supabase.client.storage.from(SupabaseSchema.BUCKET_SOS_IMAGES).publicUrl(path)
            )
        }.getOrElse { AppResult.Error(it.message ?: "Upload failed", it) }

    @Serializable
    private data class NearbyParams(val lat: Double, val lng: Double, val radius_meters: Int)

    override suspend fun getNearbyUsers(
        lat: Double,
        lng: Double,
        radiusMeters: Int
    ): AppResult<List<NearbyUser>> = runCatching {
        val resp = supabase.client.postgrest.rpc(
            SupabaseSchema.RPC_NEARBY_USERS,
            buildJsonObject {
                put("lat", lat)
                put("lng", lng)
                put("radius_meters", radiusMeters)
            }
        )
        val list = resp.decodeList<NearbyUser>()
        AppResult.Success(list)
    }.getOrElse { AppResult.Error(it.message ?: "Nearby query failed", it) }
}
