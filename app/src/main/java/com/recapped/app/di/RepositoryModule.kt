package com.recapped.app.di

import com.recapped.app.data.repository.ArtistRepository
import com.recapped.app.data.repository.ArtistRepositoryImpl
import com.recapped.app.data.repository.AuthRepository
import com.recapped.app.data.repository.AuthRepositoryImpl
import com.recapped.app.data.repository.OnboardingRepository
import com.recapped.app.data.repository.OnboardingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Bind las interfaces de repositorios a sus implementaciones concretas.
 * Permite que los ViewModels dependan de la interfaz, no de la impl.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindArtistRepository(impl: ArtistRepositoryImpl): ArtistRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(impl: OnboardingRepositoryImpl): OnboardingRepository
}