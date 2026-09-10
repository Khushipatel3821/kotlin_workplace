package com.fitpulse.app.core.di

import com.fitpulse.app.core.dispatcher.CoroutineDispatchersProvider
import com.fitpulse.app.core.dispatcher.DefaultCoroutineDispatchersProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @Singleton
    fun provideCoroutineDispatchers(): CoroutineDispatchersProvider {
        return DefaultCoroutineDispatchersProvider()
    }
}
