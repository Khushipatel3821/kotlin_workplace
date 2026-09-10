package com.fitpulse.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitpulse.app.domain.model.MuscleGroup

@Entity(tableName = "workout_routines")
data class WorkoutRoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val targetMuscles: List<MuscleGroup>,
    val estimatedDurationMinutes: Int,
    val createdAtTimestamp: Long
)

@Entity(
    tableName = "routine_exercise_cross_ref",
    primaryKeys = ["routineId", "exerciseId"],
    indices = [Index("exerciseId")]
)
data class RoutineExerciseCrossRef(
    val routineId: Long,
    val exerciseId: Long,
    val orderIndex: Int = 0
)
