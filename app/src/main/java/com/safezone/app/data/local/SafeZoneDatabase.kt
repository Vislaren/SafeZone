package com.safezone.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.safezone.app.data.local.dao.IncomingAlertDao
import com.safezone.app.data.local.dao.PendingAudioDao
import com.safezone.app.data.local.dao.SosEventDao
import com.safezone.app.data.local.entity.IncomingAlertEntity
import com.safezone.app.data.local.entity.PendingAudioEntity
import com.safezone.app.data.local.entity.SosEventEntity

@Database(
    entities = [SosEventEntity::class, IncomingAlertEntity::class, PendingAudioEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SafeZoneDatabase : RoomDatabase() {
    abstract fun sosEventDao(): SosEventDao
    abstract fun incomingAlertDao(): IncomingAlertDao
    abstract fun pendingAudioDao(): PendingAudioDao
}
