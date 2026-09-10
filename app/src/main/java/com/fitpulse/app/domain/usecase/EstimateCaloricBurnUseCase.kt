package com.fitpulse.app.domain.usecase

import com.fitpulse.app.domain.model.ExerciseCategory
import com.fitpulse.app.domain.model.WorkoutSet

class EstimateCaloricBurnUseCase {

    /**
     * Calculates estimated calories burned using standard ACSM Metabolic Equivalent of Task (MET) calculations:
     * Calories = (MET * 3.5 * weightKg / 200) * durationMinutes
     */
    operator fun invoke(
        durationSeconds: Long,
        exerciseCategory: ExerciseCategory = ExerciseCategory.BARBELL,
        weightKg: Float = 75f,
        averageRpe: Float = 7.0f
    ): Int {
        if (durationSeconds <= 0) return 0

        val durationMinutes = durationSeconds / 60f

        val baseMet = when (exerciseCategory) {
            ExerciseCategory.BARBELL -> 6.0f
            ExerciseCategory.DUMBBELL -> 5.5f
            ExerciseCategory.MACHINE -> 5.0f
            ExerciseCategory.BODYWEIGHT -> 6.5f
            ExerciseCategory.CABLE -> 4.8f
            ExerciseCategory.CARDIO -> 8.5f
        }

        // Adjust MET based on intensity (RPE 1-10)
        val intensityMultiplier = (averageRpe / 7.0f).coerceIn(0.7f, 1.4f)
        val effectiveMet = baseMet * intensityMultiplier

        val calories = (effectiveMet * 3.5f * weightKg / 200f) * durationMinutes
        return calories.toInt().coerceAtLeast(1)
    }

    fun forYoga(durationSeconds: Long, weightKg: Float = 75f): Int {
        if (durationSeconds <= 0) return 0
        val durationMinutes = durationSeconds / 60f
        val yogaMet = 3.0f // Hatha/Restorative yoga MET
        val calories = (yogaMet * 3.5f * weightKg / 200f) * durationMinutes
        return calories.toInt().coerceAtLeast(1)
    }

    fun forSteps(steps: Int, weightKg: Float = 75f): Int {
        // Average caloric cost: ~0.04 - 0.05 kcal per step for a 70-80kg adult
        val kcalPerStep = (weightKg / 75f) * 0.045f
        return (steps * kcalPerStep).toInt()
    }
}
