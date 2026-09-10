package com.fitpulse.app.presentation.viewmodel

import android.app.Application
import app.cash.turbine.test
import com.fitpulse.app.core.dispatcher.CoroutineDispatchersProvider
import com.fitpulse.app.data.sensor.SensorMotionDataSource
import com.fitpulse.app.domain.model.Exercise
import com.fitpulse.app.domain.model.ExerciseCategory
import com.fitpulse.app.domain.model.MuscleGroup
import com.fitpulse.app.domain.model.WorkoutSet
import com.fitpulse.app.domain.repository.DailyMetricRepository
import com.fitpulse.app.domain.repository.WorkoutRepository
import com.fitpulse.app.domain.usecase.DetectRepsFromMotionUseCase
import com.fitpulse.app.presentation.gym.ActiveWorkoutViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class TestDispatchersProvider(private val testDispatcher: TestDispatcher) : CoroutineDispatchersProvider {
    override val main: CoroutineDispatcher get() = testDispatcher
    override val io: CoroutineDispatcher get() = testDispatcher
    override val default: CoroutineDispatcher get() = testDispatcher
    override val unconfined: CoroutineDispatcher get() = testDispatcher
}

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveWorkoutViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = TestDispatchersProvider(testDispatcher)

    private val application: Application = mockk(relaxed = true)
    private val workoutRepository: WorkoutRepository = mockk(relaxed = true)
    private val dailyMetricRepository: DailyMetricRepository = mockk(relaxed = true)
    private val sensorMotionDataSource: SensorMotionDataSource = mockk(relaxed = true)
    private val detectRepsFromMotionUseCase: DetectRepsFromMotionUseCase = mockk(relaxed = true)

    private lateinit var viewModel: ActiveWorkoutViewModel

    private val dummyExercises = listOf(
        Exercise(
            id = 1,
            name = "Barbell Bench Press",
            primaryMuscle = MuscleGroup.CHEST,
            category = ExerciseCategory.BARBELL
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { workoutRepository.getAllExercises() } returns flowOf(dummyExercises)
        coEvery { workoutRepository.startWorkoutSession(any(), any()) } returns 101L
        coEvery { workoutRepository.getSetsForSession(101L) } returns flowOf(emptyList())

        viewModel = ActiveWorkoutViewModel(
            application = application,
            workoutRepository = workoutRepository,
            dailyMetricRepository = dailyMetricRepository,
            sensorMotionDataSource = sensorMotionDataSource,
            detectRepsFromMotionUseCase = detectRepsFromMotionUseCase,
            dispatchers = dispatchers
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initSession starts workout session and updates state`() = runTest(testDispatcher) {
        viewModel.initSession(routineId = null, title = "Chest Blitz")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Chest Blitz", state.title)
            assertEquals(101L, state.sessionId)
        }

        coVerify { workoutRepository.startWorkoutSession("Chest Blitz", null) }
    }

    @Test
    fun `addSet inserts set into workout repository`() = runTest(testDispatcher) {
        viewModel.initSession(routineId = null, title = "Quick Workout")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addSet(dummyExercises.first(), weightKg = 80f, targetReps = 8)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            workoutRepository.insertSet(match {
                it.exerciseId == 1L && it.weightKg == 80f && it.targetReps == 8
            })
        }
    }
}
