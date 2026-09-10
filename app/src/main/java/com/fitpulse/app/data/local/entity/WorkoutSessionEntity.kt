package com.fitpulse.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fitpulse.app.domain.model.SetType
import com.fitpulse.app.domain.model.WorkoutSet

@Entity(
    tableName = "workout_sessions",
    indices = [Index("startedAtTimestamp")]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long?,
    val title: String,
    val startedAtTimestamp: Long,
    val endedAtTimestamp: Long?,
    val durationSeconds: Long,
    val totalVolumeKg: Float,
    val totalReps: Int,
    val estimatedCaloriesBurned: Int,
    val notes: String
)

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("exerciseId")]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val weightKg: Float,
    val targetReps: Int,
    val completedReps: Int,
    val rpe: Float?,
    val setType: SetType,
    val isCompleted: Boolean,
    val notes: String,
    val completedAtTimestamp: Long?
) {
    fun toDomain(): WorkoutSet = WorkoutSet(
        id = id,
        sessionId = sessionId,
        exerciseId = exerciseId,
        exerciseName = exerciseName,
        setNumber = setNumber,
        weightKg = weightKg,
        targetReps = targetReps,
        completedReps = completedReps,
        rpe = rpe,
        setType = setType,
        isCompleted = isCompleted,
        notes = notes,
        completedAtTimestamp = completedAtTimestamp
    )

    companion object {
        fun fromDomain(set: WorkoutSet, sessionId: Long = set.sessionId): WorkoutSetEntity = WorkoutSetEntity(
            id = set.id,
            sessionId = sessionId,
            exerciseId = set.exerciseId,
            exerciseName = set.exerciseName,
            setNumber = set.setNumber,
            weightKg = set.weightKg,
            targetReps = set.targetReps,
            completedReps = set.completedReps,
            rpe = set.rpe,
            setType = set.setType,
            isCompleted = set.isCompleted,
            notes = set.notes,
            completedAtTimestamp = set.completedAtTimestamp
        )
    }
}
