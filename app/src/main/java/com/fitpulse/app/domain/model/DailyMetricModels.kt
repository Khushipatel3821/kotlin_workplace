package com.fitpulse.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyMetricSummary(
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val steps: Int = 0,
    val stepGoal: Int = 10000,
    val activeMinutes: Int = 0,
    val activeMinutesGoal: Int = 45,
    val caloriesBurned: Int = 0,
    val calorieGoal: Int = 500,
    val waterMilliliters: Int = 0,
    val waterGoalMilliliters: Int = 2500,
    val completedWorkoutsCount: Int = 0,
    val completedYogaCount: Int = 0
) {
    val stepProgress: Float
        get() = (steps.toFloat() / stepGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    val activeMinuteProgress: Float
        get() = (activeMinutes.toFloat() / activeMinutesGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    val calorieProgress: Float
        get() = (caloriesBurned.toFloat() / calorieGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    val isGoalMet: Boolean
        get() = steps >= stepGoal || activeMinutes >= activeMinutesGoal || completedWorkoutsCount > 0
}

@Serializable
data class StreakInfo(
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val lastActiveEpochDay: Long? = null,
    val isActiveToday: Boolean = false,
    val consistencyPercentage: Float = 0f
)

@Serializable
data class DayVolumeStat(
    val epochDay: Long,
    val dayLabel: String, // e.g. "Mon", "Tue"
    val totalVolumeKg: Float,
    val totalReps: Int,
    val durationMinutes: Int
)
