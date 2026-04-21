package com.safezone.app.data.repository

import com.safezone.app.data.remote.supabase.SupabaseClientProvider
import com.safezone.app.data.remote.supabase.SupabaseSchema
import com.safezone.app.domain.models.AppResult
import com.safezone.app.domain.models.IncomingAlert
import com.safezone.app.domain.models.TriggerSource
import com.safezone.app.domain.repository.AlertRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClientProvider
) : AlertRepository {

    override fun observeIncomingAlerts(userId: String): Flow<IncomingAlert> {
        val channel = supabase.client.channel("alerts:$userId")
        val flow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = SupabaseSchema.T_ALERT_DELIVERIES
        }
        return flow
            .filter { change ->
                change.record["recipient_id"]?.jsonPrimitive?.content == userId
            }
            .map { change -> change.record.toIncomingAlert() }
    }

    override suspend fun acknowledgeAlert(alertId: String): AppResult<Unit> = runCatching {
        supabase.client.postgrest[SupabaseSchema.T_ALERT_DELIVERIES].update(
            mapOf("acknowledged_at" to java.time.Instant.now().toString())
        ) { filter { eq("id", alertId) } }
        AppResult.Success(Unit)
    }.getOrElse { AppResult.Error(it.message ?: "Ack failed", it) }

    private fun JsonObject.toIncomingAlert(): IncomingAlert = IncomingAlert(
        eventId = this["event_id"]!!.jsonPrimitive.content,
        userId = this["user_id"]!!.jsonPrimitive.content,
        userName = this["user_name"]?.jsonPrimitive?.content ?: "Unknown",
        userPhotoUrl = this["user_photo_url"]?.jsonPrimitive?.content,
        lat = this["lat"]!!.jsonPrimitive.content.toDouble(),
        lng = this["lng"]!!.jsonPrimitive.content.toDouble(),
        distanceMeters = this["distance_meters"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0,
        triggerSource = runCatching {
            TriggerSource.valueOf(
                (this["trigger_source"]?.jsonPrimitive?.content ?: "manual").uppercase()
            )
        }.getOrDefault(TriggerSource.MANUAL)
    )
}
