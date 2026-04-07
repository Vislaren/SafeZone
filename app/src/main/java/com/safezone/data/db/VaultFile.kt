package com.safezone.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

enum class FileType { VIDEO, AUDIO }
enum class FileTag  { ENCRYPTED, VOICE_LOG, HD4K, SD720 }

@Entity(tableName = "vault_files")
data class VaultFile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fileName: String,
    val filePath: String,
    val fileType: FileType,
    val tag: FileTag,
    val sizeMb: Double = 0.0,
    val durationSeconds: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val isEncrypted: Boolean = true
)

@Dao
interface VaultFileDao {

    @Query("SELECT * FROM vault_files ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<VaultFile>>

    @Query("SELECT * FROM vault_files WHERE fileType = :type ORDER BY createdAt DESC")
    fun observeByType(type: FileType): Flow<List<VaultFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: VaultFile): Long

    @Delete
    suspend fun delete(file: VaultFile)

    @Query("DELETE FROM vault_files WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT SUM(sizeMb) FROM vault_files")
    suspend fun totalStorageMb(): Double?

    @Query("SELECT COUNT(*) FROM vault_files")
    suspend fun count(): Int
}
