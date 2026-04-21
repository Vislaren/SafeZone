package com.safezone.app.di

import android.content.Context
import androidx.room.Room
import com.safezone.app.data.local.SafeZoneDatabase
import com.safezone.app.data.local.dao.IncomingAlertDao
import com.safezone.app.data.local.dao.PendingAudioDao
import com.safezone.app.data.local.dao.SosEventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class AppScope
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class IoDispatcher
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class DefaultDispatcher
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): SafeZoneDatabase =
        Room.databaseBuilder(ctx, SafeZoneDatabase::class.java, "safezone.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideSosEventDao(db: SafeZoneDatabase): SosEventDao = db.sosEventDao()
    @Provides fun provideIncomingAlertDao(db: SafeZoneDatabase): IncomingAlertDao = db.incomingAlertDao()
    @Provides fun providePendingAudioDao(db: SafeZoneDatabase): PendingAudioDao = db.pendingAudioDao()

    @Provides @Singleton @AppScope
    fun provideAppScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides @IoDispatcher
    fun provideIoDispatcher() = Dispatchers.IO

    @Provides @DefaultDispatcher
    fun provideDefaultDispatcher() = Dispatchers.Default

    @Provides @MainDispatcher
    fun provideMainDispatcher() = Dispatchers.Main
}
