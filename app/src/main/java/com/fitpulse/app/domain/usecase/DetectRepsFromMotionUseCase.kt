package com.fitpulse.app.domain.usecase

import com.fitpulse.app.domain.model.FormQuality
import com.fitpulse.app.domain.model.MotionSample
import com.fitpulse.app.domain.model.MotionTrackingState
import com.fitpulse.app.domain.model.RepPhase
import com.fitpulse.app.domain.model.RepetitionEvent
import kotlin.math.sqrt

/**
 * UseCase implementing real-time motion analysis and rep-counting heuristic engine.
 *
 * Algorithm Architecture:
 * 1. Gravity vector estimation using exponential moving average (Low-pass filter).
 * 2. Dynamic linear acceleration magnitude extraction: |a_linear| = ||a_raw|| - g.
 * 3. Finite State Machine (FSM):
 *    IDLE -> ECCENTRIC (valley entry) -> INFLECTION_VALLEY -> CONCENTRIC (peak entry) -> INFLECTION_PEAK -> REP_RECORDED
 * 4. Temporal and magnitude hysteresis thresholding to prevent false triggers from jitter.
 */
class DetectRepsFromMotionUseCase {

    private var gravityX = 0f
    private var gravityY = 9.8f
    private var gravityZ = 0f

    private var currentState = MotionTrackingState()
    private var repStartIndexMs = 0L
    private var valleyTimestampMs = 0L
    private var peakAccelerationInCycle = 0f
    private var lowestAccelerationInCycle = 0f

    private val recentIntervals = ArrayDeque<Float>()

    companion object {
        private const val ALPHA_GRAVITY = 0.85f
        private const val MIN_REP_DURATION_MS = 800L    // 0.8s minimum per realistic rep
        private const val MAX_REP_DURATION_MS = 8000L   // 8.0s maximum per rep
        private const val ECCENTRIC_THRESHOLD = -1.2f   // Significant downward/deceleration movement
        private const val CONCENTRIC_THRESHOLD = 1.4f   // Significant upward/concentric exertion
        private const val HYSTERESIS_BAND = 0.4f
    }

    fun reset() {
        gravityX = 0f
        gravityY = 9.8f
        gravityZ = 0f
        currentState = MotionTrackingState()
        repStartIndexMs = 0L
        valleyTimestampMs = 0L
        peakAccelerationInCycle = 0f
        lowestAccelerationInCycle = 0f
        recentIntervals.clear()
    }

    /**
     * Ingests a new raw 3-axis accelerometer sample and returns updated motion tracking state.
     */
    fun processSample(rawX: Float, rawY: Float, rawZ: Float, timestampMs: Long): MotionTrackingState {
        // Step 1: Low-Pass Filter to estimate gravitational vector
        gravityX = ALPHA_GRAVITY * gravityX + (1f - ALPHA_GRAVITY) * rawX
        gravityY = ALPHA_GRAVITY * gravityY + (1f - ALPHA_GRAVITY) * rawY
        gravityZ = ALPHA_GRAVITY * gravityZ + (1f - ALPHA_GRAVITY) * rawZ

        // Step 2: Linear acceleration (High-Pass / dynamic motion component)
        val linearX = rawX - gravityX
        val linearY = rawY - gravityY
        val linearZ = rawZ - gravityZ

        val totalLinearMagnitude = sqrt(linearX * linearX + linearY * linearY + linearZ * linearZ)
        // Project onto primary vertical axis (estimated from gravity orientation)
        val verticalSign = if (linearY >= 0) 1f else -1f
        val signedLinearAcc = verticalSign * totalLinearMagnitude

        val isMoving = totalLinearMagnitude > 0.5f

        // Step 3: FSM State Transitions
        var nextPhase = currentState.currentPhase
        var newRepCount = currentState.currentRepCount
        var newRecentReps = currentState.recentReps
        var latestForm = currentState.lastFormQuality

        when (currentState.currentPhase) {
            RepPhase.IDLE -> {
                if (signedLinearAcc < ECCENTRIC_THRESHOLD) {
                    nextPhase = RepPhase.ECCENTRIC
                    repStartIndexMs = timestampMs
                    lowestAccelerationInCycle = signedLinearAcc
                    peakAccelerationInCycle = 0f
                }
            }

            RepPhase.ECCENTRIC -> {
                if (signedLinearAcc < lowestAccelerationInCycle) {
                    lowestAccelerationInCycle = signedLinearAcc
                }
                // Check if bottom inflection is reached and reversal begins
                if (signedLinearAcc > (lowestAccelerationInCycle + HYSTERESIS_BAND)) {
                    nextPhase = RepPhase.INFLECTION_VALLEY
                    valleyTimestampMs = timestampMs
                }
            }

            RepPhase.INFLECTION_VALLEY -> {
                if (signedLinearAcc > CONCENTRIC_THRESHOLD) {
                    nextPhase = RepPhase.CONCENTRIC
                    peakAccelerationInCycle = signedLinearAcc
                } else if (timestampMs - repStartIndexMs > MAX_REP_DURATION_MS) {
                    // Timed out / incomplete rep
                    nextPhase = RepPhase.IDLE
                }
            }

            RepPhase.CONCENTRIC -> {
                if (signedLinearAcc > peakAccelerationInCycle) {
                    peakAccelerationInCycle = signedLinearAcc
                }
                // Peak inflection reached (deceleration at the top)
                if (signedLinearAcc < (peakAccelerationInCycle - HYSTERESIS_BAND)) {
                    nextPhase = RepPhase.INFLECTION_PEAK
                }
            }

            RepPhase.INFLECTION_PEAK -> {
                val repDurationMs = timestampMs - repStartIndexMs
                if (repDurationMs in MIN_REP_DURATION_MS..MAX_REP_DURATION_MS) {
                    val durationSec = repDurationMs / 1000f
                    newRepCount += 1

                    // Assess tempo & form
                    latestForm = when {
                        durationSec < 1.2f -> FormQuality.RUSHED_TEMPO
                        peakAccelerationInCycle > 7.0f -> FormQuality.GOOD
                        else -> FormQuality.EXCELLENT
                    }

                    // Track rolling cadence (reps per minute)
                    if (recentIntervals.size >= 5) {
                        recentIntervals.removeFirst()
                    }
                    recentIntervals.addLast(durationSec)
                    val avgInterval = recentIntervals.average().toFloat()
                    val cadenceRpm = if (avgInterval > 0f) (60f / avgInterval) else 0f

                    val repEvent = RepetitionEvent(
                        repIndex = newRepCount,
                        durationSeconds = durationSec,
                        peakAcceleration = peakAccelerationInCycle,
                        formQuality = latestForm,
                        cadenceRepsPerMinute = cadenceRpm,
                        timestampMs = timestampMs
                    )

                    newRecentReps = (newRecentReps + repEvent).takeLast(10)
                }

                // Reset for next repetition cycle
                nextPhase = RepPhase.IDLE
                repStartIndexMs = 0L
                lowestAccelerationInCycle = 0f
                peakAccelerationInCycle = 0f
            }
        }

        val avgCadence = if (recentIntervals.isNotEmpty()) {
            60f / recentIntervals.average().toFloat()
        } else 0f

        currentState = MotionTrackingState(
            currentPhase = nextPhase,
            currentRepCount = newRepCount,
            currentMagnitude = totalLinearMagnitude,
            currentCadence = avgCadence,
            recentReps = newRecentReps,
            isActivelyMoving = isMoving,
            lastFormQuality = latestForm
        )

        return currentState
    }
}
