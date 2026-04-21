package com.safezone.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.safezone.app.data.local.entity.IncomingAlertEntity
import com.safezone.app.data.local.entity.PendingAudioEntity
import com.safezone.app.data.local.entity.SosEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SosEventDao {
    @Query("SELECT * FROM sos_event_cache WHERE userId = :userId ORDER BY startedAt DESC")
    fun observeForUser(userId: String): Flow<List<SosEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(events: List<SosEventEntity>)

    @Query("DELETE FROM sos_event_cache WHERE userId = :userId")
    suspend fun clearForUser(userId: String)
}

@Dao
interface IncomingAlertDao {
    @Query("SELECT * FROM incoming_alerts ORDER BY receivedAt DESC LIMIT 100")
    fun observeAll(): Flow<List<IncomingAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(alert: IncomingAlertEntity)

    @Query("UPDATE incoming_alerts SET resolved = 1 WHERE eventId = :eventId")
    suspend fun resolve(eventId: String)

    @Query("DELETE FROM incoming_alerts WHERE receivedAt < :before")
    suspend fun pruneOlderThan(before: Long)
}

@Dao
interface PendingAudioDao {
    @Insert
    suspend fun enqueue(chunk: PendingAudioEntity): Long

    @Query("SELECT * FROM pending_audio_chunks WHERE eventId = :eventId ORDER BY createdAt ASC")
    suspend fun forEvent(eventId: String): List<PendingAudioEntity>

    @Query("DELETE FROM pending_audio_chunks WHERE id = :id")
    suspend fun remove(id: Long)
}
