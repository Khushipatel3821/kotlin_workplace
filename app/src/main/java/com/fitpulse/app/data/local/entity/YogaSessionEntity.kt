package com.fitpulse.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fitpulse.app.domain.model.YogaSession

@Entity(tableName = "yoga_sessions")
data class YogaSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineTitle: String,
    val startedAtTimestamp: Long,
    val durationSeconds: Long,
    val completedPoses: Int,
    val totalPoses: Int,
    val breathingPatternName: String,
    val estimatedCaloriesBurned: Int,
    val notes: String
) {
    fun toDomain(): YogaSession = YogaSession(
        id = id,
        routineTitle = routineTitle,
        startedAtTimestamp = startedAtTimestamp,
        durationSeconds = durationSeconds,
        completedPoses = completedPoses,
        totalPoses = totalPoses,
        breathingPatternName = breathingPatternName,
        estimatedCaloriesBurned = estimatedCaloriesBurned,
        notes = notes
    )

    companion object {
        fun fromDomain(session: YogaSession): YogaSessionEntity = YogaSessionEntity(
            id = session.id,
            routineTitle = session.routineTitle,
            startedAtTimestamp = session.startedAtTimestamp,
            durationSeconds = session.durationSeconds,
            completedPoses = session.completedPoses,
            totalPoses = session.totalPoses,
            breathingPatternName = session.breathingPatternName,
            estimatedCaloriesBurned = session.estimatedCaloriesBurned,
            notes = session.notes
        )
    }
}
