package com.fitpulse.app.domain.usecase

import com.fitpulse.app.domain.model.DailyMetricSummary
import com.fitpulse.app.domain.model.StreakInfo
import java.time.LocalDate

class CalculateDailyStreakUseCase {

    /**
     * Computes the user's current streak, all-time longest streak, and consistency percentage.
     *
     * @param history List of daily metric summaries from persistence.
     * @param todayEpochDay The epoch day of today (defaults to LocalDate.now().toEpochDay()).
     */
    operator fun invoke(
        history: List<DailyMetricSummary>,
        todayEpochDay: Long = LocalDate.now().toEpochDay()
    ): StreakInfo {
        if (history.isEmpty()) {
            return StreakInfo(
                currentStreakDays = 0,
                longestStreakDays = 0,
                lastActiveEpochDay = null,
                isActiveToday = false,
                consistencyPercentage = 0f
            )
        }

        val activeDaysSet = history
            .filter { it.isGoalMet }
            .map { it.dateEpochDay }
            .toSortedSet()

        if (activeDaysSet.isEmpty()) {
            return StreakInfo(
                currentStreakDays = 0,
                longestStreakDays = 0,
                lastActiveEpochDay = null,
                isActiveToday = false,
                consistencyPercentage = 0f
            )
        }

        val isActiveToday = activeDaysSet.contains(todayEpochDay)
        val lastActiveDay = activeDaysSet.last()

        // Calculate Current Streak
        var currentStreak = 0
        var checkDay = if (isActiveToday) todayEpochDay else (todayEpochDay - 1)

        while (activeDaysSet.contains(checkDay)) {
            currentStreak++
            checkDay--
        }

        // Calculate Longest Streak
        var longestStreak = 0
        var tempStreak = 0
        var previousDay: Long? = null

        for (day in activeDaysSet) {
            if (previousDay == null || day == previousDay + 1) {
                tempStreak++
            } else {
                tempStreak = 1
            }
            if (tempStreak > longestStreak) {
                longestStreak = tempStreak
            }
            previousDay = day
        }

        // Consistency percentage over past 30 days
        val past30DaysStart = todayEpochDay - 29
        val activeDaysInPast30 = activeDaysSet.count { it in past30DaysStart..todayEpochDay }
        val consistency = (activeDaysInPast30.toFloat() / 30f) * 100f

        return StreakInfo(
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak.coerceAtLeast(currentStreak),
            lastActiveEpochDay = lastActiveDay,
            isActiveToday = isActiveToday,
            consistencyPercentage = consistency.coerceIn(0f, 100f)
        )
    }
}
