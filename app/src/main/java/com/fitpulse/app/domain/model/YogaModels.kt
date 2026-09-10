package com.fitpulse.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class BreathingPhase {
    INHALE,
    HOLD_IN,
    EXHALE,
    HOLD_OUT
}

@Serializable
data class BreathingPattern(
    val name: String,
    val description: String,
    val inhaleSeconds: Int,
    val holdInSeconds: Int,
    val exhaleSeconds: Int,
    val holdOutSeconds: Int,
    val totalCycles: Int = 10
) {
    val cycleDurationSeconds: Int
        get() = inhaleSeconds + holdInSeconds + exhaleSeconds + holdOutSeconds

    companion object {
        val BOX_BREATHING = BreathingPattern(
            name = "Box Breathing (Samavritti)",
            description = "Equal duration for all four phases. Calms nervous system and boosts focus.",
            inhaleSeconds = 4,
            holdInSeconds = 4,
            exhaleSeconds = 4,
            holdOutSeconds = 4,
            totalCycles = 8
        )

        val RELAX_4_7_8 = BreathingPattern(
            name = "4-7-8 Deep Relaxation",
            description = "Dr. Weil's tranquilizing breath method. Reduces anxiety and promotes deep rest.",
            inhaleSeconds = 4,
            holdInSeconds = 7,
            exhaleSeconds = 8,
            holdOutSeconds = 0,
            totalCycles = 6
        )

        val PRANAYAMA_ENERGIZE = BreathingPattern(
            name = "Energizing Breath (Kapalabhati Cadence)",
            description = "Quick active exhalations with passive inhalations to stimulate energy.",
            inhaleSeconds = 2,
            holdInSeconds = 1,
            exhaleSeconds = 2,
            holdOutSeconds = 1,
            totalCycles = 15
        )

        val DEEP_CALM_2_1_4 = BreathingPattern(
            name = "Calm Pacing (2-1-4-1)",
            description = "Extended exhalation for heart rate reduction and mindful recovery.",
            inhaleSeconds = 4,
            holdInSeconds = 2,
            exhaleSeconds = 6,
            holdOutSeconds = 2,
            totalCycles = 10
        )
    }
}

@Serializable
enum class YogaDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

@Serializable
data class YogaPose(
    val id: Long = 0,
    val englishName: String,
    val sanskritName: String,
    val difficulty: YogaDifficulty,
    val targetArea: String,
    val benefits: String,
    val defaultHoldSeconds: Int = 30,
    val cues: List<String> = emptyList()
)

@Serializable
data class YogaRoutine(
    val id: Long = 0,
    val title: String,
    val description: String,
    val difficulty: YogaDifficulty,
    val estimatedMinutes: Int,
    val poses: List<YogaPose> = emptyList()
)

@Serializable
data class YogaSession(
    val id: Long = 0,
    val routineTitle: String,
    val startedAtTimestamp: Long,
    val durationSeconds: Long,
    val completedPoses: Int,
    val totalPoses: Int,
    val breathingPatternName: String,
    val estimatedCaloriesBurned: Int,
    val notes: String = ""
)
