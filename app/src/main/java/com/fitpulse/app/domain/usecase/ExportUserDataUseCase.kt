package com.fitpulse.app.domain.usecase

import com.fitpulse.app.domain.repository.DailyMetricRepository
import com.fitpulse.app.domain.repository.WorkoutRepository
import com.fitpulse.app.domain.repository.YogaRepository
import kotlinx.coroutines.flow.first

class ExportUserDataUseCase(
    private val dailyMetricRepository: DailyMetricRepository,
    private val workoutRepository: WorkoutRepository,
    private val yogaRepository: YogaRepository
) {
    suspend fun exportJson(): String {
        return dailyMetricRepository.exportAllDataJson()
    }

    suspend fun exportWorkoutHistoryCsv(): String {
        val sessions = workoutRepository.getAllSessions().first()
        val builder = StringBuilder()
        builder.append("SessionId,Title,DateTimestamp,DurationMinutes,TotalVolumeKg,TotalReps,Calories\n")
        sessions.forEach { s ->
            builder.append("${s.id},\"${s.title}\",${s.startedAtTimestamp},${s.durationSeconds / 60},${s.totalVolumeKg},${s.totalReps},${s.estimatedCaloriesBurned}\n")
        }
        return builder.toString()
    }

    suspend fun importJson(json: String): Boolean {
        return dailyMetricRepository.importDataJson(json)
    }
}
