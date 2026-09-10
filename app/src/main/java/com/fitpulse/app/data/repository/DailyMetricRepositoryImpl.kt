package com.fitpulse.app.data.repository

import com.fitpulse.app.data.export.DataExportEngine
import com.fitpulse.app.data.export.FitPulseBackupPayload
import com.fitpulse.app.data.local.dao.DailyMetricDao
import com.fitpulse.app.data.local.dao.WorkoutDao
import com.fitpulse.app.data.local.dao.YogaDao
import com.fitpulse.app.data.local.entity.DailyMetricLogEntity
import com.fitpulse.app.domain.model.DailyMetricSummary
import com.fitpulse.app.domain.model.DayVolumeStat
import com.fitpulse.app.domain.model.StreakInfo
import com.fitpulse.app.domain.repository.DailyMetricRepository
import com.fitpulse.app.domain.usecase.CalculateDailyStreakUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class DailyMetricRepositoryImpl(
    private val dailyMetricDao: DailyMetricDao,
    private val workoutDao: WorkoutDao,
    private val yogaDao: YogaDao,
    private val calculateDailyStreakUseCase: CalculateDailyStreakUseCase,
    private val dataExportEngine: DataExportEngine
) : DailyMetricRepository {

    override fun getDailyMetricForDay(epochDay: Long): Flow<DailyMetricSummary?> {
        return dailyMetricDao.getMetricForDay(epochDay).map { it?.toDomain() }
    }

    override fun getTodayMetric(): Flow<DailyMetricSummary> {
        val todayEpoch = LocalDate.now().toEpochDay()
        return dailyMetricDao.getMetricForDay(todayEpoch).map { entity ->
            entity?.toDomain() ?: DailyMetricSummary(dateEpochDay = todayEpoch)
        }
    }

    private suspend fun getOrCreateTodayMetric(epochDay: Long): DailyMetricLogEntity {
        return dailyMetricDao.getMetricForDaySync(epochDay) ?: DailyMetricLogEntity(dateEpochDay = epochDay)
    }

    override suspend fun updateSteps(epochDay: Long, steps: Int) {
        val current = getOrCreateTodayMetric(epochDay)
        dailyMetricDao.insertOrUpdateMetric(current.copy(steps = steps))
    }

    override suspend fun addActiveMinutes(epochDay: Long, minutes: Int) {
        val current = getOrCreateTodayMetric(epochDay)
        dailyMetricDao.insertOrUpdateMetric(current.copy(activeMinutes = current.activeMinutes + minutes))
    }

    override suspend fun addCaloriesBurned(epochDay: Long, calories: Int) {
        val current = getOrCreateTodayMetric(epochDay)
        dailyMetricDao.insertOrUpdateMetric(current.copy(caloriesBurned = current.caloriesBurned + calories))
    }

    override suspend fun addWater(epochDay: Long, milliliters: Int) {
        val current = getOrCreateTodayMetric(epochDay)
        dailyMetricDao.insertOrUpdateMetric(current.copy(waterMilliliters = current.waterMilliliters + milliliters))
    }

    override suspend fun incrementCompletedWorkout(epochDay: Long) {
        val current = getOrCreateTodayMetric(epochDay)
        dailyMetricDao.insertOrUpdateMetric(
            current.copy(completedWorkoutsCount = current.completedWorkoutsCount + 1)
        )
    }

    override suspend fun incrementCompletedYoga(epochDay: Long) {
        val current = getOrCreateTodayMetric(epochDay)
        dailyMetricDao.insertOrUpdateMetric(
            current.copy(completedYogaCount = current.completedYogaCount + 1)
        )
    }

    override fun getStreakInfo(): Flow<StreakInfo> {
        return dailyMetricDao.getRecentMetrics(limit = 60).map { entities ->
            val domainSummaries = entities.map { it.toDomain() }
            calculateDailyStreakUseCase(domainSummaries)
        }
    }

    override fun getLast7DaysVolume(): Flow<List<DayVolumeStat>> {
        return workoutDao.getAllSessionsWithSets().map { sessions ->
            val today = LocalDate.now()
            (6 downTo 0).map { daysAgo ->
                val targetDate = today.minusDays(daysAgo.toLong())
                val targetEpoch = targetDate.toEpochDay()
                val dayLabel = targetDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

                val matchingSessions = sessions.filter { sessionWithSets ->
                    val sessionDate = LocalDate.ofEpochDay(sessionWithSets.session.startedAtTimestamp / (1000 * 60 * 60 * 24))
                    sessionDate == targetDate
                }

                val dayVolume = matchingSessions.sumOf { it.session.totalVolumeKg.toDouble() }.toFloat()
                val dayReps = matchingSessions.sumOf { it.session.totalReps }
                val dayDurationMinutes = matchingSessions.sumOf { (it.session.durationSeconds / 60).toInt() }

                DayVolumeStat(
                    epochDay = targetEpoch,
                    dayLabel = dayLabel,
                    totalVolumeKg = dayVolume,
                    totalReps = dayReps,
                    durationMinutes = dayDurationMinutes
                )
            }
        }
    }

    override suspend fun exportAllDataJson(): String {
        val exercises = workoutDao.getAllExercises().first().map { it.toDomain() }
        val routines = workoutDao.getAllRoutinesWithExercises().first().map { it.toDomain() }
        val sessions = workoutDao.getAllSessionsWithSets().first().map { it.toDomain() }
        val yogaSessions = yogaDao.getAllYogaSessions().first().map { it.toDomain() }
        val dailyMetrics = dailyMetricDao.getAllMetricsSync().map { it.toDomain() }

        val payload = FitPulseBackupPayload(
            exercises = exercises,
            routines = routines,
            workoutSessions = sessions,
            yogaSessions = yogaSessions,
            dailyMetrics = dailyMetrics
        )
        return dataExportEngine.exportToJson(payload)
    }

    override suspend fun importDataJson(jsonString: String): Boolean {
        val payload = dataExportEngine.importFromJson(jsonString) ?: return false

        if (payload.exercises.isNotEmpty()) {
            workoutDao.insertExercises(payload.exercises.map { com.fitpulse.app.data.local.entity.ExerciseEntity.fromDomain(it) })
        }
        payload.dailyMetrics.forEach { metric ->
            dailyMetricDao.insertOrUpdateMetric(DailyMetricLogEntity.fromDomain(metric))
        }
        return true
    }
}
