package com.fitpulse.app.presentation.yoga

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitpulse.app.core.dispatcher.CoroutineDispatchersProvider
import com.fitpulse.app.domain.model.BreathingPattern
import com.fitpulse.app.domain.model.BreathingPhase
import com.fitpulse.app.domain.model.YogaPose
import com.fitpulse.app.domain.model.YogaRoutine
import com.fitpulse.app.domain.model.YogaSession
import com.fitpulse.app.domain.repository.DailyMetricRepository
import com.fitpulse.app.domain.repository.YogaRepository
import com.fitpulse.app.domain.usecase.BreathingState
import com.fitpulse.app.domain.usecase.EstimateCaloricBurnUseCase
import com.fitpulse.app.domain.usecase.ManageBreathingPacerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class YogaStudioUiState(
    val selectedPattern: BreathingPattern = BreathingPattern.BOX_BREATHING,
    val breathingState: BreathingState = BreathingState(
        phase = BreathingPhase.INHALE,
        phaseProgress = 0f,
        phaseElapsedSeconds = 0f,
        phaseTotalSeconds = 4,
        currentCycle = 1,
        totalCycles = 8
    ),
    val isPacingActive: Boolean = false,
    val availablePoses: List<YogaPose> = emptyList(),
    val currentPoseIndex: Int = 0,
    val isPoseTimerRunning: Boolean = false,
    val poseRemainingSeconds: Int = 30,
    val totalYogaSessionSeconds: Long = 0,
    val isSessionSaved: Boolean = false
)

@HiltViewModel
class YogaStudioViewModel @Inject constructor(
    private val yogaRepository: YogaRepository,
    private val dailyMetricRepository: DailyMetricRepository,
    private val manageBreathingPacerUseCase: ManageBreathingPacerUseCase,
    private val estimateCaloricBurnUseCase: EstimateCaloricBurnUseCase,
    private val dispatchers: CoroutineDispatchersProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(YogaStudioUiState())
    val uiState: StateFlow<YogaStudioUiState> = _uiState.asStateFlow()

    private var pacingJob: Job? = null
    private var poseTimerJob: Job? = null
    private var sessionTimerJob: Job? = null

    init {
        loadPoses()
        startSessionTimer()
    }

    private fun loadPoses() {
        viewModelScope.launch(dispatchers.io) {
            yogaRepository.getAllPoses().collectLatest { poses ->
                _uiState.value = _uiState.value.copy(
                    availablePoses = poses,
                    poseRemainingSeconds = poses.firstOrNull()?.defaultHoldSeconds ?: 30
                )
            }
        }
    }

    private fun startSessionTimer() {
        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch(dispatchers.default) {
            while (true) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    totalYogaSessionSeconds = _uiState.value.totalYogaSessionSeconds + 1
                )
            }
        }
    }

    fun selectPattern(pattern: BreathingPattern) {
        stopBreathingPacer()
        _uiState.value = _uiState.value.copy(
            selectedPattern = pattern,
            breathingState = BreathingState(
                phase = BreathingPhase.INHALE,
                phaseProgress = 0f,
                phaseElapsedSeconds = 0f,
                phaseTotalSeconds = pattern.inhaleSeconds,
                currentCycle = 1,
                totalCycles = pattern.totalCycles
            )
        )
    }

    fun toggleBreathingPacer() {
        if (_uiState.value.isPacingActive) {
            stopBreathingPacer()
        } else {
            startBreathingPacer()
        }
    }

    private fun startBreathingPacer() {
        _uiState.value = _uiState.value.copy(isPacingActive = true)
        pacingJob?.cancel()
        pacingJob = viewModelScope.launch(dispatchers.default) {
            manageBreathingPacerUseCase.startPacing(_uiState.value.selectedPattern)
                .collect { state ->
                    _uiState.value = _uiState.value.copy(breathingState = state)
                    if (state.isSessionCompleted) {
                        _uiState.value = _uiState.value.copy(isPacingActive = false)
                    }
                }
        }
    }

    private fun stopBreathingPacer() {
        pacingJob?.cancel()
        pacingJob = null
        _uiState.value = _uiState.value.copy(isPacingActive = false)
    }

    fun togglePoseTimer() {
        if (_uiState.value.isPoseTimerRunning) {
            poseTimerJob?.cancel()
            _uiState.value = _uiState.value.copy(isPoseTimerRunning = false)
        } else {
            startPoseTimer()
        }
    }

    private fun startPoseTimer() {
        _uiState.value = _uiState.value.copy(isPoseTimerRunning = true)
        poseTimerJob?.cancel()
        poseTimerJob = viewModelScope.launch(dispatchers.default) {
            while (_uiState.value.poseRemainingSeconds > 0 && _uiState.value.isPoseTimerRunning) {
                delay(1000)
                val remaining = _uiState.value.poseRemainingSeconds - 1
                _uiState.value = _uiState.value.copy(poseRemainingSeconds = remaining)
            }
            _uiState.value = _uiState.value.copy(isPoseTimerRunning = false)
        }
    }

    fun nextPose() {
        val nextIdx = (_uiState.value.currentPoseIndex + 1).coerceAtMost(
            (_uiState.value.availablePoses.size - 1).coerceAtLeast(0)
        )
        val nextPose = _uiState.value.availablePoses.getOrNull(nextIdx)
        poseTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            currentPoseIndex = nextIdx,
            poseRemainingSeconds = nextPose?.defaultHoldSeconds ?: 30,
            isPoseTimerRunning = false
        )
    }

    fun previousPose() {
        val prevIdx = (_uiState.value.currentPoseIndex - 1).coerceAtLeast(0)
        val prevPose = _uiState.value.availablePoses.getOrNull(prevIdx)
        poseTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            currentPoseIndex = prevIdx,
            poseRemainingSeconds = prevPose?.defaultHoldSeconds ?: 30,
            isPoseTimerRunning = false
        )
    }

    fun completeAndSaveSession() {
        viewModelScope.launch(dispatchers.io) {
            val state = _uiState.value
            val duration = state.totalYogaSessionSeconds
            val calories = estimateCaloricBurnUseCase.forYoga(duration)

            val session = YogaSession(
                routineTitle = "Mindfulness & Flow Session",
                startedAtTimestamp = System.currentTimeMillis() - (duration * 1000),
                durationSeconds = duration,
                completedPoses = state.currentPoseIndex + 1,
                totalPoses = state.availablePoses.size,
                breathingPatternName = state.selectedPattern.name,
                estimatedCaloriesBurned = calories
            )

            yogaRepository.saveYogaSession(session)

            val todayEpoch = LocalDate.now().toEpochDay()
            dailyMetricRepository.incrementCompletedYoga(todayEpoch)
            dailyMetricRepository.addActiveMinutes(todayEpoch, (duration / 60).toInt())
            dailyMetricRepository.addCaloriesBurned(todayEpoch, calories)

            _uiState.value = _uiState.value.copy(isSessionSaved = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pacingJob?.cancel()
        poseTimerJob?.cancel()
        sessionTimerJob?.cancel()
    }
}
