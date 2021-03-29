package com.example.dawnlightclinicalstudy.di

import com.example.dawnlightclinicalstudy.data.LifeSignalRepository
import com.example.dawnlightclinicalstudy.data.UserSessionRepository
import com.example.dawnlightclinicalstudy.data_source.RetrofitService
import com.example.dawnlightclinicalstudy.presentation.MainActivityEventListener
import com.example.dawnlightclinicalstudy.usecases.SessionManager
import com.example.dawnlightclinicalstudy.usecases.main.LifeSignalUseCase
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
    fun provideLifeSignalRepository(retrofitService: RetrofitService): LifeSignalRepository {
        return LifeSignalRepository(retrofitService)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideUserSessionRepository(retrofitService: RetrofitService): UserSessionRepository {
        return UserSessionRepository(retrofitService)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideMainActivityEventListener(): MainActivityEventListener {
        return MainActivityEventListener()
    }

    @ActivityRetainedScoped
    @Provides
    fun provideSessionManager(
        userSessionRepository: UserSessionRepository,
    ): SessionManager {
        return SessionManager(userSessionRepository)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideLifeSignalDataParsingUseCase(
        lifeSignalRepository: LifeSignalRepository
    ): LifeSignalUseCase {
        return LifeSignalUseCase(lifeSignalRepository)
    }
}