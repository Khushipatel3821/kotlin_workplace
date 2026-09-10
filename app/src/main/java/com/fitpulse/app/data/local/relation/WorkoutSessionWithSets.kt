package com.fitpulse.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.fitpulse.app.data.local.entity.WorkoutSessionEntity
import com.fitpulse.app.data.local.entity.WorkoutSetEntity
import com.fitpulse.app.domain.model.WorkoutSession

data class WorkoutSessionWithSets(
    @Embedded val session: WorkoutSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val sets: List<WorkoutSetEntity>
) {
    fun toDomain(): WorkoutSession = WorkoutSession(
        id = session.id,
        routineId = session.routineId,
        title = session.title,
        startedAtTimestamp = session.startedAtTimestamp,
        endedAtTimestamp = session.endedAtTimestamp,
        durationSeconds = session.durationSeconds,
        totalVolumeKg = session.totalVolumeKg,
        totalReps = session.totalReps,
        estimatedCaloriesBurned = session.estimatedCaloriesBurned,
        sets = sets.sortedBy { it.setNumber }.map { it.toDomain() },
        notes = session.notes
    )
}
