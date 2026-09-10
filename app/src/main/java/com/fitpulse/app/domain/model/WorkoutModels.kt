package com.fitpulse.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MuscleGroup {
    CHEST,
    BACK,
    LEGS,
    SHOULDERS,
    BICEPS,
    TRICEPS,
    CORE,
    FULL_BODY
}

@Serializable
enum class ExerciseCategory {
    BARBELL,
    DUMBBELL,
    MACHINE,
    BODYWEIGHT,
    CABLE,
    CARDIO
}

@Serializable
enum class SetType {
    WARMUP,
    NORMAL,
    DROP_SET,
    FAILURE
}

@Serializable
data class Exercise(
    val id: Long = 0,
    val name: String,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val category: ExerciseCategory,
    val instructions: String = "",
    val defaultRestSeconds: Int = 90,
    val isCustom: Boolean = false
)

@Serializable
data class WorkoutSet(
    val id: Long = 0,
    val sessionId: Long = 0,
    val exerciseId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val weightKg: Float,
    val targetReps: Int,
    val completedReps: Int,
    val rpe: Float? = null, // Rate of Perceived Exertion (1.0 to 10.0)
    val setType: SetType = SetType.NORMAL,
    val isCompleted: Boolean = false,
    val notes: String = "",
    val completedAtTimestamp: Long? = null
)

@Serializable
data class WorkoutSession(
    val id: Long = 0,
    val routineId: Long? = null,
    val title: String,
    val startedAtTimestamp: Long,
    val endedAtTimestamp: Long? = null,
    val durationSeconds: Long = 0,
    val totalVolumeKg: Float = 0f,
    val totalReps: Int = 0,
    val estimatedCaloriesBurned: Int = 0,
    val sets: List<WorkoutSet> = emptyList(),
    val notes: String = ""
)

@Serializable
data class WorkoutRoutine(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val targetMuscles: List<MuscleGroup> = emptyList(),
    val estimatedDurationMinutes: Int = 45,
    val exercises: List<Exercise> = emptyList(),
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
