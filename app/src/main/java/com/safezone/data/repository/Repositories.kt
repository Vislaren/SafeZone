package com.safezone.data.repository

import android.content.Context
import com.safezone.data.db.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// ─── ContactRepository ────────────────────────────────────────────────────────
class ContactRepository @Inject constructor(private val dao: ContactDao) {

    fun observeContacts(): Flow<List<Contact>> = dao.observeAll()

    suspend fun getContacts(): List<Contact> = dao.getAll()

    suspend fun saveContact(name: String, phone: String, slot: Int) {
        dao.insert(Contact(name = name, phone = phone, slot = slot))
    }

    suspend fun deleteContact(contact: Contact) = dao.delete(contact)

    suspend fun deleteSlot(slot: Int) = dao.deleteBySlot(slot)

    suspend fun count(): Int = dao.count()
}

// ─── VaultRepository ─────────────────────────────────────────────────────────
class VaultRepository @Inject constructor(private val dao: VaultFileDao) {

    fun observeAll(): Flow<List<VaultFile>> = dao.observeAll()

    fun observeByType(type: FileType): Flow<List<VaultFile>> = dao.observeByType(type)

    suspend fun insert(file: VaultFile): Long = dao.insert(file)

    suspend fun delete(file: VaultFile) = dao.delete(file)

    suspend fun deleteById(id: Int) = dao.deleteById(id)

    suspend fun totalStorageGb(): Double = (dao.totalStorageMb() ?: 0.0) / 1024.0

    suspend fun count(): Int = dao.count()
}

// ─── SecurityRepository ───────────────────────────────────────────────────────
class SecurityRepository @Inject constructor(
    private val context: Context,
    private val dao: TriggerPhraseDao
) {
    fun observePhrases(): Flow<List<TriggerPhrase>> = dao.observeActive()

    suspend fun getPhrases(): List<TriggerPhrase> = dao.getActive()

    suspend fun savePhrase(phrase: String, slot: Int) {
        dao.insert(TriggerPhrase(phrase = phrase, slot = slot))
    }

    suspend fun deletePhrase(phrase: TriggerPhrase) = dao.delete(phrase)

    suspend fun getStorageDir(): java.io.File {
        val dir = java.io.File(context.filesDir, "safezone_vault")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}
