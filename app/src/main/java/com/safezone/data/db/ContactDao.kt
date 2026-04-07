package com.safezone.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY slot ASC")
    fun observeAll(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts ORDER BY slot ASC")
    suspend fun getAll(): List<Contact>

    @Query("SELECT * FROM contacts WHERE slot = :slot LIMIT 1")
    suspend fun getBySlot(slot: Int): Contact?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact): Long

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("DELETE FROM contacts WHERE slot = :slot")
    suspend fun deleteBySlot(slot: Int)

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun count(): Int
}
