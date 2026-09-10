package com.fitpulse.app.core.di

import com.fitpulse.app.data.export.DataExportEngine
import com.fitpulse.app.data.local.dao.DailyMetricDao
import com.fitpulse.app.data.local.dao.WorkoutDao
import com.fitpulse.app.data.local.dao.YogaDao
import com.fitpulse.app.data.repository.DailyMetricRepositoryImpl
import com.fitpulse.app.data.repository.WorkoutRepositoryImpl
import com.fitpulse.app.data.repository.YogaRepositoryImpl
import com.fitpulse.app.domain.repository.DailyMetricRepository
import com.fitpulse.app.domain.repository.WorkoutRepository
import com.fitpulse.app.domain.repository.YogaRepository
import com.fitpulse.app.domain.usecase.CalculateDailyStreakUseCase
import com.fitpulse.app.domain.usecase.DetectRepsFromMotionUseCase
import com.fitpulse.app.domain.usecase.EstimateCaloricBurnUseCase
import com.fitpulse.app.domain.usecase.ExportUserDataUseCase
import com.fitpulse.app.domain.usecase.ManageBreathingPacerUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideEstimateCaloricBurnUseCase(): EstimateCaloricBurnUseCase = EstimateCaloricBurnUseCase()

    @Provides
    @Singleton
    fun provideCalculateDailyStreakUseCase(): CalculateDailyStreakUseCase = CalculateDailyStreakUseCase()

    @Provides
    @Singleton
    fun provideDetectRepsFromMotionUseCase(): DetectRepsFromMotionUseCase = DetectRepsFromMotionUseCase()

    @Provides
    @Singleton
    fun provideManageBreathingPacerUseCase(): ManageBreathingPacerUseCase = ManageBreathingPacerUseCase()

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        workoutDao: WorkoutDao,
        estimateCaloricBurnUseCase: EstimateCaloricBurnUseCase
    ): WorkoutRepository {
        return WorkoutRepositoryImpl(workoutDao, estimateCaloricBurnUseCase)
    }

    @Provides
    @Singleton
    fun provideYogaRepository(yogaDao: YogaDao): YogaRepository {
        return YogaRepositoryImpl(yogaDao)
    }

    @Provides
    @Singleton
    fun provideDailyMetricRepository(
        dailyMetricDao: DailyMetricDao,
        workoutDao: WorkoutDao,
        yogaDao: YogaDao,
        calculateDailyStreakUseCase: CalculateDailyStreakUseCase,
        dataExportEngine: DataExportEngine
    ): DailyMetricRepository {
        return DailyMetricRepositoryImpl(
            dailyMetricDao = dailyMetricDao,
            workoutDao = workoutDao,
            yogaDao = yogaDao,
            calculateDailyStreakUseCase = calculateDailyStreakUseCase,
            dataExportEngine = dataExportEngine
        )
    }

    @Provides
    @Singleton
    fun provideExportUserDataUseCase(
        dailyMetricRepository: DailyMetricRepository,
        workoutRepository: WorkoutRepository,
        yogaRepository: YogaRepository
    ): ExportUserDataUseCase {
        return ExportUserDataUseCase(dailyMetricRepository, workoutRepository, yogaRepository)
    }
}
