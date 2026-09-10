package com.fitpulse.app.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fitpulse.app.data.datastore.UserPreferencesDataStore
import com.fitpulse.app.data.export.DataExportEngine
import com.fitpulse.app.data.local.FitPulseDatabase
import com.fitpulse.app.data.local.dao.DailyMetricDao
import com.fitpulse.app.data.local.dao.WorkoutDao
import com.fitpulse.app.data.local.dao.YogaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFitPulseDatabase(
        @ApplicationContext context: Context,
        databaseProvider: Provider<FitPulseDatabase>
    ): FitPulseDatabase {
        return Room.databaseBuilder(
            context,
            FitPulseDatabase::class.java,
            FitPulseDatabase.DATABASE_NAME
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    databaseProvider.get().workoutDao().insertExercises(FitPulseDatabase.INITIAL_EXERCISES)
                }
            }
        }).fallbackToDestructiveMigration()
          .build()
    }

    @Provides
    @Singleton
    fun provideWorkoutDao(database: FitPulseDatabase): WorkoutDao = database.workoutDao()

    @Provides
    @Singleton
    fun provideYogaDao(database: FitPulseDatabase): YogaDao = database.yogaDao()

    @Provides
    @Singleton
    fun provideDailyMetricDao(database: FitPulseDatabase): DailyMetricDao = database.dailyMetricDao()

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(@ApplicationContext context: Context): UserPreferencesDataStore {
        return UserPreferencesDataStore(context)
    }

    @Provides
    @Singleton
    fun provideDataExportEngine(): DataExportEngine = DataExportEngine()
}
