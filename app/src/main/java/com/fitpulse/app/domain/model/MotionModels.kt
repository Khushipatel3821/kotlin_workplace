package com.fitpulse.app.domain.model

enum class RepPhase {
    IDLE,
    ECCENTRIC,        // Downward / loading phase
    INFLECTION_VALLEY,// Bottom of repetition
    CONCENTRIC,       // Upward / exertion phase
    INFLECTION_PEAK   // Top of repetition (completion)
}

enum class FormQuality {
    EXCELLENT,
    GOOD,
    RUSHED_TEMPO,
    INCOMPLETE_ROM
}

data class MotionSample(
    val timestampMs: Long,
    val rawX: Float,
    val rawY: Float,
    val rawZ: Float,
    val filteredMagnitude: Float,
    val verticalAcceleration: Float
)

data class RepetitionEvent(
    val repIndex: Int,
    val durationSeconds: Float,
    val peakAcceleration: Float,
    val formQuality: FormQuality,
    val cadenceRepsPerMinute: Float,
    val timestampMs: Long = System.currentTimeMillis()
)

data class MotionTrackingState(
    val currentPhase: RepPhase = RepPhase.IDLE,
    val currentRepCount: Int = 0,
    val currentMagnitude: Float = 0f,
    val currentCadence: Float = 0f,
    val recentReps: List<RepetitionEvent> = emptyList(),
    val isActivelyMoving: Boolean = false,
    val lastFormQuality: FormQuality = FormQuality.GOOD
)
