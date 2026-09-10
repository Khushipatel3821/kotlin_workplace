package com.fitpulse.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fitpulse.app.domain.model.Exercise
import com.fitpulse.app.domain.model.ExerciseCategory
import com.fitpulse.app.domain.model.MuscleGroup

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup>,
    val category: ExerciseCategory,
    val instructions: String,
    val defaultRestSeconds: Int,
    val isCustom: Boolean
) {
    fun toDomain(): Exercise = Exercise(
        id = id,
        name = name,
        primaryMuscle = primaryMuscle,
        secondaryMuscles = secondaryMuscles,
        category = category,
        instructions = instructions,
        defaultRestSeconds = defaultRestSeconds,
        isCustom = isCustom
    )

    companion object {
        fun fromDomain(exercise: Exercise): ExerciseEntity = ExerciseEntity(
            id = exercise.id,
            name = exercise.name,
            primaryMuscle = exercise.primaryMuscle,
            secondaryMuscles = exercise.secondaryMuscles,
            category = exercise.category,
            instructions = exercise.instructions,
            defaultRestSeconds = exercise.defaultRestSeconds,
            isCustom = exercise.isCustom
        )
    }
}
