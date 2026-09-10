package com.fitpulse.app.presentation.gym

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitpulse.app.core.dispatcher.CoroutineDispatchersProvider
import com.fitpulse.app.data.sensor.SensorMotionDataSource
import com.fitpulse.app.data.service.WorkoutForegroundService
import com.fitpulse.app.domain.model.Exercise
import com.fitpulse.app.domain.model.MotionTrackingState
import com.fitpulse.app.domain.model.SetType
import com.fitpulse.app.domain.model.WorkoutSession
import com.fitpulse.app.domain.model.WorkoutSet
import com.fitpulse.app.domain.repository.DailyMetricRepository
import com.fitpulse.app.domain.repository.WorkoutRepository
import com.fitpulse.app.domain.usecase.DetectRepsFromMotionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ActiveWorkoutUiState(
    val sessionId: Long = 0,
    val title: String = "Active Workout",
    val elapsedSeconds: Long = 0,
    val sets: List<WorkoutSet> = emptyList(),
    val availableExercises: List<Exercise> = emptyList(),
    val motionState: MotionTrackingState = MotionTrackingState(),
    val isMotionTrackingActive: Boolean = false,
    val isRestTimerActive: Boolean = false,
    val restSecondsRemaining: Int = 0,
    val totalRestDuration: Int = 90,
    val isSessionFinished: Boolean = false
)

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    application: Application,
    private val workoutRepository: WorkoutRepository,
    private val dailyMetricRepository: DailyMetricRepository,
    private val sensorMotionDataSource: SensorMotionDataSource,
    private val detectRepsFromMotionUseCase: DetectRepsFromMotionUseCase,
    private val dispatchers: CoroutineDispatchersProvider
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    private var motionJob: Job? = null
    private var timerJob: Job? = null
    private var restTimerJob: Job? = null

    init {
        loadAvailableExercises()
    }

    private fun loadAvailableExercises() {
        viewModelScope.launch(dispatchers.io) {
            workoutRepository.getAllExercises().collectLatest { list ->
                _uiState.value = _uiState.value.copy(availableExercises = list)
            }
        }
    }

    fun initSession(routineId: Long?, title: String) {
        viewModelScope.launch(dispatchers.io) {
            val sessionId = workoutRepository.startWorkoutSession(title, routineId)
            _uiState.value = _uiState.value.copy(
                sessionId = sessionId,
                title = title,
                elapsedSeconds = 0,
                isSessionFinished = false
            )

            // Start Foreground Service
            WorkoutForegroundService.startWorkout(getApplication(), title)

            // If a routine was selected, pre-populate default sets
            if (routineId != null && routineId > 0) {
                val routine = workoutRepository.getRoutineById(routineId).first()
                routine?.exercises?.forEach { exercise ->
                    workoutRepository.insertSet(
                        WorkoutSet(
                            sessionId = sessionId,
                            exerciseId = exercise.id,
                            exerciseName = exercise.name,
                            setNumber = 1,
                            weightKg = 60f,
                            targetReps = 10,
                            completedReps = 0,
                            setType = SetType.NORMAL
                        )
                    )
                }
            }

            // Observe sets from DB
            observeSets(sessionId)
            startSessionTimer()
        }
    }

    private fun observeSets(sessionId: Long) {
        viewModelScope.launch(dispatchers.io) {
            workoutRepository.getSetsForSession(sessionId).collectLatest { setList ->
                _uiState.value = _uiState.value.copy(sets = setList)
            }
        }
    }

    private fun startSessionTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch(dispatchers.default) {
            while (!uiState.value.isSessionFinished) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    elapsedSeconds = _uiState.value.elapsedSeconds + 1
                )
            }
        }
    }

    fun toggleMotionTracking(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isMotionTrackingActive = enabled)
        if (enabled) {
            detectRepsFromMotionUseCase.reset()
            motionJob?.cancel()
            motionJob = viewModelScope.launch(dispatchers.default) {
                sensorMotionDataSource.observeMotionSamples().collect { sample ->
                    val updatedState = detectRepsFromMotionUseCase.processSample(
                        sample.rawX,
                        sample.rawY,
                        sample.rawZ,
                        sample.timestampMs
                    )
                    _uiState.value = _uiState.value.copy(motionState = updatedState)
                }
            }
        } else {
            motionJob?.cancel()
            motionJob = null
        }
    }

    fun addSet(exercise: Exercise, weightKg: Float = 50f, targetReps: Int = 10) {
        viewModelScope.launch(dispatchers.io) {
            val existingSets = uiState.value.sets.filter { it.exerciseId == exercise.id }
            val nextSetNumber = (existingSets.maxOfOrNull { it.setNumber } ?: 0) + 1

            val newSet = WorkoutSet(
                sessionId = uiState.value.sessionId,
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                setNumber = nextSetNumber,
                weightKg = weightKg,
                targetReps = targetReps,
                completedReps = 0,
                setType = SetType.NORMAL
            )
            workoutRepository.insertSet(newSet)
        }
    }

    fun completeSet(set: WorkoutSet, completedReps: Int, rpe: Float = 8.0f) {
        viewModelScope.launch(dispatchers.io) {
            val updated = set.copy(
                completedReps = completedReps,
                rpe = rpe,
                isCompleted = true,
                completedAtTimestamp = System.currentTimeMillis()
            )
            workoutRepository.updateSet(updated)

            // Start Rest Timer (e.g. 90s)
            startRestTimer(90)
        }
    }

    fun startRestTimer(durationSeconds: Int) {
        restTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isRestTimerActive = true,
            totalRestDuration = durationSeconds,
            restSecondsRemaining = durationSeconds
        )

        WorkoutForegroundService.startRest(getApplication(), durationSeconds)

        restTimerJob = viewModelScope.launch(dispatchers.default) {
            var remaining = durationSeconds
            while (remaining > 0) {
                delay(1000)
                remaining -= 1
                _uiState.value = _uiState.value.copy(restSecondsRemaining = remaining)
            }
            _uiState.value = _uiState.value.copy(isRestTimerActive = false, restSecondsRemaining = 0)
        }
    }

    fun adjustRestTimer(deltaSeconds: Int) {
        val newDuration = (_uiState.value.restSecondsRemaining + deltaSeconds).coerceAtLeast(0)
        if (newDuration == 0) {
            skipRestTimer()
        } else {
            _uiState.value = _uiState.value.copy(restSecondsRemaining = newDuration)
        }
    }

    fun skipRestTimer() {
        restTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(isRestTimerActive = false, restSecondsRemaining = 0)
        WorkoutForegroundService.stopRest(getApplication())
    }

    fun finishSession(notes: String = "") {
        viewModelScope.launch(dispatchers.io) {
            val state = uiState.value
            workoutRepository.finishWorkoutSession(
                sessionId = state.sessionId,
                durationSeconds = state.elapsedSeconds,
                notes = notes
            )

            // Increment daily workout count and active minutes
            val todayEpoch = LocalDate.now().toEpochDay()
            dailyMetricRepository.incrementCompletedWorkout(todayEpoch)
            dailyMetricRepository.addActiveMinutes(todayEpoch, (state.elapsedSeconds / 60).toInt())

            WorkoutForegroundService.stopWorkout(getApplication())
            _uiState.value = _uiState.value.copy(isSessionFinished = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        motionJob?.cancel()
        timerJob?.cancel()
        restTimerJob?.cancel()
    }
}
