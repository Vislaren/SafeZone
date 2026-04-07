package com.safezone.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "trigger_phrases")
data class TriggerPhrase(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val phrase: String,
    val isActive: Boolean = true,
    val slot: Int,           // 1–3 phrase slots
    val voicePrintPath: String? = null
)

@Dao
interface TriggerPhraseDao {

    @Query("SELECT * FROM trigger_phrases WHERE isActive = 1 ORDER BY slot ASC")
    fun observeActive(): Flow<List<TriggerPhrase>>

    @Query("SELECT * FROM trigger_phrases WHERE isActive = 1 ORDER BY slot ASC")
    suspend fun getActive(): List<TriggerPhrase>

    @Query("SELECT * FROM trigger_phrases ORDER BY slot ASC")
    suspend fun getAll(): List<TriggerPhrase>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(phrase: TriggerPhrase): Long

    @Update
    suspend fun update(phrase: TriggerPhrase)

    @Delete
    suspend fun delete(phrase: TriggerPhrase)

    @Query("SELECT COUNT(*) FROM trigger_phrases WHERE isActive = 1")
    suspend fun activeCount(): Int
}
