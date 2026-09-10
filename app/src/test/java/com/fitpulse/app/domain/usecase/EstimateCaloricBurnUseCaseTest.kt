package com.fitpulse.app.domain.usecase

import com.fitpulse.app.domain.model.ExerciseCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EstimateCaloricBurnUseCaseTest {

    private lateinit var useCase: EstimateCaloricBurnUseCase

    @Before
    fun setUp() {
        useCase = EstimateCaloricBurnUseCase()
    }

    @Test
    fun `zero duration returns zero calories`() {
        val calories = useCase(durationSeconds = 0)
        assertEquals(0, calories)
    }

    @Test
    fun `one hour barbell workout calculates expected caloric range`() {
        // 60 minutes, 75kg, Barbell base MET 6.0, RPE 7.0 (multiplier 1.0)
        // Cal = (6.0 * 3.5 * 75 / 200) * 60 = 7.875 * 60 = 472.5 -> 472
        val calories = useCase(
            durationSeconds = 3600,
            exerciseCategory = ExerciseCategory.BARBELL,
            weightKg = 75f,
            averageRpe = 7.0f
        )
        assertEquals(472, calories)
    }

    @Test
    fun `higher RPE increases caloric burn estimate`() {
        val moderateCal = useCase(durationSeconds = 1800, averageRpe = 6.0f)
        val highCal = useCase(durationSeconds = 1800, averageRpe = 9.0f)
        assertTrue(highCal > moderateCal)
    }

    @Test
    fun `yoga calculates correct restorative burn rate`() {
        val calories = useCase.forYoga(durationSeconds = 1800, weightKg = 75f)
        // MET 3.0 -> Cal = (3.0 * 3.5 * 75 / 200) * 30 = 3.9375 * 30 = 118
        assertEquals(118, calories)
    }

    @Test
    fun `step count calculates realistic energy expenditure`() {
        val calories = useCase.forSteps(steps = 10000, weightKg = 75f)
        // 10000 * 0.045 = 450 kcal
        assertEquals(450, calories)
    }
}
