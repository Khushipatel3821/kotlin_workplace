package com.fitpulse.app.domain.usecase

import com.fitpulse.app.domain.model.FormQuality
import com.fitpulse.app.domain.model.RepPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.sin

class DetectRepsFromMotionUseCaseTest {

    private lateinit var useCase: DetectRepsFromMotionUseCase

    @Before
    fun setUp() {
        useCase = DetectRepsFromMotionUseCase()
    }

    @Test
    fun `idle state returns 0 reps and idle phase`() {
        val state = useCase.processSample(0f, 9.8f, 0f, 1000L)
        assertEquals(0, state.currentRepCount)
        assertEquals(RepPhase.IDLE, state.currentPhase)
    }

    @Test
    fun `subtle jitter below threshold does not trigger reps`() {
        var currentTime = 1000L
        for (i in 0..100) {
            val jitter = (sin(i * 0.1) * 0.3).toFloat()
            val state = useCase.processSample(jitter, 9.8f + jitter, jitter, currentTime)
            assertEquals(0, state.currentRepCount)
            currentTime += 50L
        }
    }

    @Test
    fun `simulated complete repetition wave correctly increments rep count`() {
        var currentTime = 1000L
        var state = useCase.processSample(0f, 9.8f, 0f, currentTime)

        // Initialize gravity baseline
        for (i in 0..20) {
            state = useCase.processSample(0f, 9.8f, 0f, currentTime)
            currentTime += 50L
        }

        // Simulate 2 distinct squat reps
        for (rep in 1..2) {
            // Eccentric descent (Y drops below threshold)
            for (step in 0..10) {
                state = useCase.processSample(0f, 9.8f - 2.5f, 0f, currentTime)
                currentTime += 50L
            }

            // Bottom inflection / turnaround
            for (step in 0..5) {
                state = useCase.processSample(0f, 9.8f - 0.5f, 0f, currentTime)
                currentTime += 50L
            }

            // Concentric ascent (Y rises with positive exertion)
            for (step in 0..12) {
                state = useCase.processSample(0f, 9.8f + 3.0f, 0f, currentTime)
                currentTime += 50L
            }

            // Top peak turnaround
            for (step in 0..5) {
                state = useCase.processSample(0f, 9.8f + 0.2f, 0f, currentTime)
                currentTime += 50L
            }

            // Settle to idle
            for (step in 0..10) {
                state = useCase.processSample(0f, 9.8f, 0f, currentTime)
                currentTime += 50L
            }

            assertEquals(rep, state.currentRepCount)
        }

        assertEquals(2, state.currentRepCount)
        assertTrue(state.recentReps.isNotEmpty())
    }
}
