package com.fitpulse.app.domain.usecase

import com.fitpulse.app.domain.model.DailyMetricSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CalculateDailyStreakUseCaseTest {

    private lateinit var useCase: CalculateDailyStreakUseCase

    @Before
    fun setUp() {
        useCase = CalculateDailyStreakUseCase()
    }

    @Test
    fun `empty history returns 0 current and longest streak`() {
        val streak = useCase(emptyList(), todayEpochDay = 100L)
        assertEquals(0, streak.currentStreakDays)
        assertEquals(0, streak.longestStreakDays)
        assertFalse(streak.isActiveToday)
    }

    @Test
    fun `consecutive 3 active days ending today gives streak 3`() {
        val today = 100L
        val history = listOf(
            DailyMetricSummary(dateEpochDay = 98L, steps = 11000), // Goal met
            DailyMetricSummary(dateEpochDay = 99L, steps = 10500), // Goal met
            DailyMetricSummary(dateEpochDay = 100L, steps = 12000) // Goal met
        )

        val streak = useCase(history, todayEpochDay = today)
        assertEquals(3, streak.currentStreakDays)
        assertEquals(3, streak.longestStreakDays)
        assertTrue(streak.isActiveToday)
    }

    @Test
    fun `streak ending yesterday still preserves current streak`() {
        val today = 100L
        val history = listOf(
            DailyMetricSummary(dateEpochDay = 98L, steps = 10000),
            DailyMetricSummary(dateEpochDay = 99L, steps = 10000),
            DailyMetricSummary(dateEpochDay = 100L, steps = 2000) // Not yet met today
        )

        val streak = useCase(history, todayEpochDay = today)
        assertEquals(2, streak.currentStreakDays)
        assertFalse(streak.isActiveToday)
    }

    @Test
    fun `broken streak calculates longest streak properly`() {
        val today = 100L
        val history = listOf(
            // Past 4-day streak
            DailyMetricSummary(dateEpochDay = 90L, steps = 10000),
            DailyMetricSummary(dateEpochDay = 91L, steps = 10000),
            DailyMetricSummary(dateEpochDay = 92L, steps = 10000),
            DailyMetricSummary(dateEpochDay = 93L, steps = 10000),
            // Missed day 94
            DailyMetricSummary(dateEpochDay = 94L, steps = 1000),
            // Current 2-day streak
            DailyMetricSummary(dateEpochDay = 99L, steps = 10000),
            DailyMetricSummary(dateEpochDay = 100L, steps = 10000)
        )

        val streak = useCase(history, todayEpochDay = today)
        assertEquals(2, streak.currentStreakDays)
        assertEquals(4, streak.longestStreakDays)
    }
}
