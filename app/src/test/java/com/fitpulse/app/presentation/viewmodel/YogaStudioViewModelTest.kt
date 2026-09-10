package com.fitpulse.app.presentation.viewmodel

import app.cash.turbine.test
import com.fitpulse.app.domain.model.BreathingPattern
import com.fitpulse.app.domain.model.YogaDifficulty
import com.fitpulse.app.domain.model.YogaPose
import com.fitpulse.app.domain.repository.DailyMetricRepository
import com.fitpulse.app.domain.repository.YogaRepository
import com.fitpulse.app.domain.usecase.EstimateCaloricBurnUseCase
import com.fitpulse.app.domain.usecase.ManageBreathingPacerUseCase
import com.fitpulse.app.presentation.yoga.YogaStudioViewModel
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class YogaStudioViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = TestDispatchersProvider(testDispatcher)

    private val yogaRepository: YogaRepository = mockk(relaxed = true)
    private val dailyMetricRepository: DailyMetricRepository = mockk(relaxed = true)
    private val manageBreathingPacerUseCase = ManageBreathingPacerUseCase()
    private val estimateCaloricBurnUseCase = EstimateCaloricBurnUseCase()

    private lateinit var viewModel: YogaStudioViewModel

    private val dummyPoses = listOf(
        YogaPose(
            id = 1,
            englishName = "Downward-Facing Dog",
            sanskritName = "Adho Mukha Svanasana",
            difficulty = YogaDifficulty.BEGINNER,
            targetArea = "Spine",
            benefits = "Energizing",
            defaultHoldSeconds = 45
        ),
        YogaPose(
            id = 2,
            englishName = "Warrior II",
            sanskritName = "Virabhadrasana II",
            difficulty = YogaDifficulty.BEGINNER,
            targetArea = "Legs",
            benefits = "Strength",
            defaultHoldSeconds = 30
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { yogaRepository.getAllPoses() } returns flowOf(dummyPoses)

        viewModel = YogaStudioViewModel(
            yogaRepository = yogaRepository,
            dailyMetricRepository = dailyMetricRepository,
            manageBreathingPacerUseCase = manageBreathingPacerUseCase,
            estimateCaloricBurnUseCase = estimateCaloricBurnUseCase,
            dispatchers = dispatchers
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selectPattern updates selected breathing pattern`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectPattern(BreathingPattern.RELAX_4_7_8)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("4-7-8 Deep Relaxation", state.selectedPattern.name)
            assertEquals(4, state.selectedPattern.inhaleSeconds)
            assertEquals(7, state.selectedPattern.holdInSeconds)
            assertEquals(8, state.selectedPattern.exhaleSeconds)
        }
    }

    @Test
    fun `nextPose increments pose index`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.nextPose()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.currentPoseIndex)
            assertEquals(30, state.poseRemainingSeconds)
        }
    }

    @Test
    fun `completeAndSaveSession saves yoga session and updates daily metrics`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.completeAndSaveSession()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            yogaRepository.saveYogaSession(any())
            dailyMetricRepository.incrementCompletedYoga(any())
        }
    }
}
