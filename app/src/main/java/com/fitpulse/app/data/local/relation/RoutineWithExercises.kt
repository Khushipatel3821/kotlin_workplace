package com.fitpulse.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.fitpulse.app.data.local.entity.ExerciseEntity
import com.fitpulse.app.data.local.entity.RoutineExerciseCrossRef
import com.fitpulse.app.data.local.entity.WorkoutRoutineEntity
import com.fitpulse.app.domain.model.WorkoutRoutine

data class RoutineWithExercises(
    @Embedded val routine: WorkoutRoutineEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = RoutineExerciseCrossRef::class,
            parentColumn = "routineId",
            entityColumn = "exerciseId"
        )
    )
    val exercises: List<ExerciseEntity>
) {
    fun toDomain(): WorkoutRoutine = WorkoutRoutine(
        id = routine.id,
        name = routine.name,
        description = routine.description,
        targetMuscles = routine.targetMuscles,
        estimatedDurationMinutes = routine.estimatedDurationMinutes,
        exercises = exercises.map { it.toDomain() },
        createdAtTimestamp = routine.createdAtTimestamp
    )
}
