package com.example.dawnlightclinicalstudy.di

import com.example.dawnlightclinicalstudy.data.LifeSignalRepository
import com.example.dawnlightclinicalstudy.usecases.main.LifeSignalDataParsingUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object MainActivityModule {

    @ActivityRetainedScoped
    @Provides
    fun provideLifeSignalRepository(): LifeSignalRepository {
        return LifeSignalRepository()
    }

    @Provides
    fun provideLifeSignalDataParsingUseCase(
        lifeSignalRepository: LifeSignalRepository
    ): LifeSignalDataParsingUseCase {
        return LifeSignalDataParsingUseCase(lifeSignalRepository)
    }
}