package com.fitpulse.app.domain.repository

import com.fitpulse.app.domain.model.DailyMetricSummary
import com.fitpulse.app.domain.model.DayVolumeStat
import com.fitpulse.app.domain.model.StreakInfo
import kotlinx.coroutines.flow.Flow

interface DailyMetricRepository {
    fun getDailyMetricForDay(epochDay: Long): Flow<DailyMetricSummary?>
    fun getTodayMetric(): Flow<DailyMetricSummary>
    suspend fun updateSteps(epochDay: Long, steps: Int)
    suspend fun addActiveMinutes(epochDay: Long, minutes: Int)
    suspend fun addCaloriesBurned(epochDay: Long, calories: Int)
    suspend fun addWater(epochDay: Long, milliliters: Int)
    suspend fun incrementCompletedWorkout(epochDay: Long)
    suspend fun incrementCompletedYoga(epochDay: Long)
    fun getStreakInfo(): Flow<StreakInfo>
    fun getLast7DaysVolume(): Flow<List<DayVolumeStat>>
    suspend fun exportAllDataJson(): String
    suspend fun importDataJson(jsonString: String): Boolean
}
