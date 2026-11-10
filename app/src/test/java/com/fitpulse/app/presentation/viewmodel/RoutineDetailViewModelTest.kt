package com.fitpulse.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.fitpulse.app.domain.model.Exercise
import com.fitpulse.app.domain.model.ExerciseCategory
import com.fitpulse.app.domain.model.MuscleGroup
import com.fitpulse.app.domain.model.WorkoutRoutine
import com.fitpulse.app.domain.repository.WorkoutRepository
import com.fitpulse.app.domain.usecase.EstimateCaloricBurnUseCase
import com.fitpulse.app.presentation.gym.RoutineDetailViewModel
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
class RoutineDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val workoutRepository: WorkoutRepository = mockk(relaxed = true)
    private val estimateCaloricBurnUseCase = EstimateCaloricBurnUseCase()

    private val dummyRoutine = WorkoutRoutine(
        id = 1L,
        name = "Push Power Hypertrophy",
        description = "Chest & Triceps Focus",
        targetMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS),
        estimatedDurationMinutes = 45,
        exercises = listOf(
            Exercise(
                id = 101,
                name = "Barbell Bench Press",
                primaryMuscle = MuscleGroup.CHEST,
                category = ExerciseCategory.BARBELL
            ),
            Exercise(
                id = 102,
                name = "Incline Dumbbell Press",
                primaryMuscle = MuscleGroup.CHEST,
                category = ExerciseCategory.DUMBBELL
            )
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { workoutRepository.getRoutineById(1L) } returns flowOf(dummyRoutine)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads routine and computes initial calorie estimation breakdown`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("routineId" to 1L))
        val viewModel = RoutineDetailViewModel(
            savedStateHandle = savedStateHandle,
            workoutRepository = workoutRepository,
            estimateCaloricBurnUseCase = estimateCaloricBurnUseCase
        )

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNotNull(state.routine)
            assertEquals("Push Power Hypertrophy", state.routine?.name)
            assertEquals(2, state.exerciseBreakdowns.size)
            assertTrue(state.caloriesPerSequence > 0f)
            assertTrue(state.totalRoutineCalories > 0)
        }
    }

    @Test
    fun `updating weight and sets recalculates calories dynamically`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle(mapOf("routineId" to 1L))
        val viewModel = RoutineDetailViewModel(
            savedStateHandle = savedStateHandle,
            workoutRepository = workoutRepository,
            estimateCaloricBurnUseCase = estimateCaloricBurnUseCase
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val initialCalories = viewModel.uiState.value.caloriesPerSequence

        // Increase weight and sets
        viewModel.updateWeight(30.0f)
        viewModel.updateSets(6)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedCalories = viewModel.uiState.value.caloriesPerSequence
        assertTrue("Calories should increase when weight & sets increase", updatedCalories > initialCalories)
    }
}
