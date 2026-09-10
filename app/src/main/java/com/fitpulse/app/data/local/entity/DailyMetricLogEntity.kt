package com.fitpulse.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fitpulse.app.domain.model.DailyMetricSummary

@Entity(tableName = "daily_metric_logs")
data class DailyMetricLogEntity(
    @PrimaryKey
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
    fun toDomain(): DailyMetricSummary = DailyMetricSummary(
        dateEpochDay = dateEpochDay,
        steps = steps,
        stepGoal = stepGoal,
        activeMinutes = activeMinutes,
        activeMinutesGoal = activeMinutesGoal,
        caloriesBurned = caloriesBurned,
        calorieGoal = calorieGoal,
        waterMilliliters = waterMilliliters,
        waterGoalMilliliters = waterGoalMilliliters,
        completedWorkoutsCount = completedWorkoutsCount,
        completedYogaCount = completedYogaCount
    )

    companion object {
        fun fromDomain(summary: DailyMetricSummary): DailyMetricLogEntity = DailyMetricLogEntity(
            dateEpochDay = summary.dateEpochDay,
            steps = summary.steps,
            stepGoal = summary.stepGoal,
            activeMinutes = summary.activeMinutes,
            activeMinutesGoal = summary.activeMinutesGoal,
            caloriesBurned = summary.caloriesBurned,
            calorieGoal = summary.calorieGoal,
            waterMilliliters = summary.waterMilliliters,
            waterGoalMilliliters = summary.waterGoalMilliliters,
            completedWorkoutsCount = summary.completedWorkoutsCount,
            completedYogaCount = summary.completedYogaCount
        )
    }
}
