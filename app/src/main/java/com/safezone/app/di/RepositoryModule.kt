package com.safezone.app.di

import com.safezone.app.data.repository.AlertRepositoryImpl
import com.safezone.app.data.repository.AuthRepositoryImpl
import com.safezone.app.data.repository.LocationRepositoryImpl
import com.safezone.app.data.repository.PhraseRepositoryImpl
import com.safezone.app.data.repository.ProfileRepositoryImpl
import com.safezone.app.data.repository.SosRepositoryImpl
import com.safezone.app.domain.repository.AlertRepository
import com.safezone.app.domain.repository.AuthRepository
import com.safezone.app.domain.repository.LocationRepository
import com.safezone.app.domain.repository.PhraseRepository
import com.safezone.app.domain.repository.ProfileRepository
import com.safezone.app.domain.repository.SosRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuth(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindProfile(impl: ProfileRepositoryImpl): ProfileRepository
    @Binds @Singleton abstract fun bindPhrase(impl: PhraseRepositoryImpl): PhraseRepository
    @Binds @Singleton abstract fun bindSos(impl: SosRepositoryImpl): SosRepository
    @Binds @Singleton abstract fun bindAlert(impl: AlertRepositoryImpl): AlertRepository
    @Binds @Singleton abstract fun bindLocation(impl: LocationRepositoryImpl): LocationRepository
}
