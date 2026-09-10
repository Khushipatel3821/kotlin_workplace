package com.fitpulse.app.data.export

import com.fitpulse.app.domain.model.DailyMetricSummary
import com.fitpulse.app.domain.model.Exercise
import com.fitpulse.app.domain.model.WorkoutRoutine
import com.fitpulse.app.domain.model.WorkoutSession
import com.fitpulse.app.domain.model.YogaSession
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class FitPulseBackupPayload(
    val exportTimestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0",
    val exercises: List<Exercise> = emptyList(),
    val routines: List<WorkoutRoutine> = emptyList(),
    val workoutSessions: List<WorkoutSession> = emptyList(),
    val yogaSessions: List<YogaSession> = emptyList(),
    val dailyMetrics: List<DailyMetricSummary> = emptyList()
)

class DataExportEngine {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun exportToJson(payload: FitPulseBackupPayload): String {
        return json.encodeToString(payload)
    }

    fun importFromJson(jsonString: String): FitPulseBackupPayload? {
        return try {
            json.decodeFromString<FitPulseBackupPayload>(jsonString)
        } catch (e: Exception) {
            null
        }
    }
}
