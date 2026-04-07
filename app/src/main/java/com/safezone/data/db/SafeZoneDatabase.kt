package com.safezone.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(
    entities = [Contact::class, TriggerPhrase::class, VaultFile::class],
    version = 1,
    exportSchema = false
)
abstract class SafeZoneDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun triggerPhraseDao(): TriggerPhraseDao
    abstract fun vaultFileDao(): VaultFileDao

    companion object {
        @Volatile private var INSTANCE: SafeZoneDatabase? = null

        fun getInstance(context: Context): SafeZoneDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): SafeZoneDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                SafeZoneDatabase::class.java,
                "safezone_secure.db"
            )
            .fallbackToDestructiveMigration()
            .build()
    }
}
