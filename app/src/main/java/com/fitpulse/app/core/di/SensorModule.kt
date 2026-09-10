package com.fitpulse.app.core.di

import android.content.Context
import com.fitpulse.app.data.sensor.SensorMotionDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SensorModule {

    @Provides
    @Singleton
    fun provideSensorMotionDataSource(@ApplicationContext context: Context): SensorMotionDataSource {
        return SensorMotionDataSource(context)
    }
}
