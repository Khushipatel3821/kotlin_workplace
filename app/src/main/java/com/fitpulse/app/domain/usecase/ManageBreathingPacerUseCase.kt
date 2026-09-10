package com.fitpulse.app.domain.usecase

import com.fitpulse.app.domain.model.BreathingPattern
import com.fitpulse.app.domain.model.BreathingPhase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class BreathingState(
    val phase: BreathingPhase,
    val phaseProgress: Float, // 0.0f to 1.0f
    val phaseElapsedSeconds: Float,
    val phaseTotalSeconds: Int,
    val currentCycle: Int,
    val totalCycles: Int,
    val isCycleCompleted: Boolean = false,
    val isSessionCompleted: Boolean = false
)

class ManageBreathingPacerUseCase {

    /**
     * Emits state updates 20 times a second (50ms interval) for fluid Compose Canvas animations.
     */
    fun startPacing(pattern: BreathingPattern): Flow<BreathingState> = flow {
        val updateIntervalMs = 50L
        val updatesPerSecond = 1000f / updateIntervalMs

        for (cycle in 1..pattern.totalCycles) {
            // 1. INHALE
            if (pattern.inhaleSeconds > 0) {
                val totalSteps = (pattern.inhaleSeconds * updatesPerSecond).toInt()
                for (step in 0..totalSteps) {
                    val progress = step.toFloat() / totalSteps
                    val elapsedSec = step * (updateIntervalMs / 1000f)
                    emit(
                        BreathingState(
                            phase = BreathingPhase.INHALE,
                            phaseProgress = progress,
                            phaseElapsedSeconds = elapsedSec,
                            phaseTotalSeconds = pattern.inhaleSeconds,
                            currentCycle = cycle,
                            totalCycles = pattern.totalCycles
                        )
                    )
                    delay(updateIntervalMs)
                }
            }

            // 2. HOLD IN
            if (pattern.holdInSeconds > 0) {
                val totalSteps = (pattern.holdInSeconds * updatesPerSecond).toInt()
                for (step in 0..totalSteps) {
                    val progress = step.toFloat() / totalSteps
                    val elapsedSec = step * (updateIntervalMs / 1000f)
                    emit(
                        BreathingState(
                            phase = BreathingPhase.HOLD_IN,
                            phaseProgress = progress,
                            phaseElapsedSeconds = elapsedSec,
                            phaseTotalSeconds = pattern.holdInSeconds,
                            currentCycle = cycle,
                            totalCycles = pattern.totalCycles
                        )
                    )
                    delay(updateIntervalMs)
                }
            }

            // 3. EXHALE
            if (pattern.exhaleSeconds > 0) {
                val totalSteps = (pattern.exhaleSeconds * updatesPerSecond).toInt()
                for (step in 0..totalSteps) {
                    val progress = step.toFloat() / totalSteps
                    val elapsedSec = step * (updateIntervalMs / 1000f)
                    emit(
                        BreathingState(
                            phase = BreathingPhase.EXHALE,
                            phaseProgress = progress,
                            phaseElapsedSeconds = elapsedSec,
                            phaseTotalSeconds = pattern.exhaleSeconds,
                            currentCycle = cycle,
                            totalCycles = pattern.totalCycles,
                            isCycleCompleted = (step == totalSteps && pattern.holdOutSeconds == 0)
                        )
                    )
                    delay(updateIntervalMs)
                }
            }

            // 4. HOLD OUT
            if (pattern.holdOutSeconds > 0) {
                val totalSteps = (pattern.holdOutSeconds * updatesPerSecond).toInt()
                for (step in 0..totalSteps) {
                    val progress = step.toFloat() / totalSteps
                    val elapsedSec = step * (updateIntervalMs / 1000f)
                    emit(
                        BreathingState(
                            phase = BreathingPhase.HOLD_OUT,
                            phaseProgress = progress,
                            phaseElapsedSeconds = elapsedSec,
                            phaseTotalSeconds = pattern.holdOutSeconds,
                            currentCycle = cycle,
                            totalCycles = pattern.totalCycles,
                            isCycleCompleted = (step == totalSteps)
                        )
                    )
                    delay(updateIntervalMs)
                }
            }
        }

        // Final completion state
        emit(
            BreathingState(
                phase = BreathingPhase.HOLD_IN,
                phaseProgress = 1.0f,
                phaseElapsedSeconds = 0f,
                phaseTotalSeconds = 0,
                currentCycle = pattern.totalCycles,
                totalCycles = pattern.totalCycles,
                isCycleCompleted = true,
                isSessionCompleted = true
            )
        )
    }
}
