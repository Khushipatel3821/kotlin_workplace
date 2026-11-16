package com.fitpulse.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.fitpulse.app.domain.model.Exercise
import com.fitpulse.app.domain.model.ExerciseCategory
import com.fitpulse.app.domain.model.MuscleGroup
import com.fitpulse.app.domain.repository.WorkoutRepository
import com.fitpulse.app.presentation.gym.ExerciseDetailViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val workoutRepository: WorkoutRepository = mockk(relaxed = true)

    private val dummyExercise = Exercise(
        id = 101L,
        name = "Incline Dumbbell Press",
        primaryMuscle = MuscleGroup.CHEST,
        secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS),
        category = ExerciseCategory.DUMBBELL,
        instructions = "Set bench to 30 degrees. Press dumbbells upward with controlled eccentric tempo.",
        defaultRestSeconds = 90
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { workoutRepository.getExerciseById(101L) } returns flowOf(dummyExercise)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads clicked exercise dynamically and computes initial calorie breakdown`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("exerciseId" to 101L))
        val viewModel = ExerciseDetailViewModel(
            savedStateHandle = savedStateHandle,
            workoutRepository = workoutRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNotNull(state.exercise)
            assertEquals("Incline Dumbbell Press", state.exercise?.name)
            assertEquals(15.0f, state.weightKg)
            assertEquals(4, state.setsCount)
            assertEquals(12, state.repsCount)
            assertTrue(state.caloriesPerSequence > 0f)
            assertEquals(4, state.setBreakdowns.size)
        }
    }

    @Test
    fun `updating weight and sets recalculates exercise calorie burn dynamically`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("exerciseId" to 101L))
        val viewModel = ExerciseDetailViewModel(
            savedStateHandle = savedStateHandle,
            workoutRepository = workoutRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val initialCalories = viewModel.uiState.value.caloriesPerSequence

        // Increase weight and sets
        viewModel.updateWeight(25.0f)
        viewModel.updateSets(5)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedCalories = viewModel.uiState.value.caloriesPerSequence
        assertTrue("Calories should increase when weight & sets increase", updatedCalories > initialCalories)
    }
}
