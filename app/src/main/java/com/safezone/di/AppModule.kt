package com.safezone.di

import android.content.Context
import com.safezone.data.db.*
import com.safezone.data.preferences.SessionManager
import com.safezone.data.repository.ContactRepository
import com.safezone.data.repository.SecurityRepository
import com.safezone.data.repository.VaultRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): SafeZoneDatabase =
        SafeZoneDatabase.getInstance(context)

    @Singleton
    @Provides
    fun provideContactDao(db: SafeZoneDatabase): ContactDao = db.contactDao()

    @Singleton
    @Provides
    fun provideTriggerPhraseDao(db: SafeZoneDatabase): TriggerPhraseDao = db.triggerPhraseDao()

    @Singleton
    @Provides
    fun provideVaultFileDao(db: SafeZoneDatabase): VaultFileDao = db.vaultFileDao()

    @Singleton
    @Provides
    fun provideContactRepository(dao: ContactDao): ContactRepository = ContactRepository(dao)

    @Singleton
    @Provides
    fun provideVaultRepository(dao: VaultFileDao): VaultRepository = VaultRepository(dao)

    @Singleton
    @Provides
    fun provideSecurityRepository(
        @ApplicationContext context: Context,
        dao: TriggerPhraseDao
    ): SecurityRepository = SecurityRepository(context, dao)
}
