package com.fitpulse.app.presentation.gym

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitpulse.app.domain.model.ExerciseCategory
import com.fitpulse.app.domain.model.WorkoutRoutine
import com.fitpulse.app.domain.repository.WorkoutRepository
import com.fitpulse.app.domain.usecase.EstimateCaloricBurnUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseCalorieBreakdown(
    val exerciseName: String,
    val categoryName: String,
    val baseMet: Float,
    val estimatedCalories: Int
)

data class RoutineDetailUiState(
    val routine: WorkoutRoutine? = null,
    val isLoading: Boolean = true,
    val weightKg: Float = 15.0f,
    val setsCount: Int = 4,
    val repsCount: Int = 12,
    val rpeIntensity: Float = 7.5f,
    val caloriesPerSequence: Float = 22.5f,
    val totalRoutineCalories: Int = 220,
    val exerciseBreakdowns: List<ExerciseCalorieBreakdown> = emptyList()
)

@HiltViewModel
class RoutineDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val estimateCaloricBurnUseCase: EstimateCaloricBurnUseCase
) : ViewModel() {

    val routineId: Long = savedStateHandle.get<Long>("routineId") ?: 1L

    private val _uiState = MutableStateFlow(RoutineDetailUiState())
    val uiState: StateFlow<RoutineDetailUiState> = _uiState.asStateFlow()

    init {
        loadRoutine()
    }

    private fun loadRoutine() {
        viewModelScope.launch {
            workoutRepository.getRoutineById(routineId).collectLatest { routine ->
                _uiState.value = _uiState.value.copy(
                    routine = routine,
                    isLoading = false
                )
                recalculateCalories()
            }
        }
    }

    fun updateWeight(weightKg: Float) {
        _uiState.value = _uiState.value.copy(weightKg = weightKg.coerceIn(5f, 150f))
        recalculateCalories()
    }

    fun updateSets(setsCount: Int) {
        _uiState.value = _uiState.value.copy(setsCount = setsCount.coerceIn(1, 10))
        recalculateCalories()
    }

    fun updateReps(repsCount: Int) {
        _uiState.value = _uiState.value.copy(repsCount = repsCount.coerceIn(1, 30))
        recalculateCalories()
    }

    fun updateRpe(rpe: Float) {
        _uiState.value = _uiState.value.copy(rpeIntensity = rpe.coerceIn(5f, 10f))
        recalculateCalories()
    }

    private fun recalculateCalories() {
        val currentState = _uiState.value
        val routine = currentState.routine ?: return

        val weight = currentState.weightKg
        val sets = currentState.setsCount
        val reps = currentState.repsCount
        val rpe = currentState.rpeIntensity

        val calPerSequence = estimateCaloricBurnUseCase.forSequence(
            category = ExerciseCategory.BARBELL,
            weightKg = weight,
            sets = sets,
            reps = reps,
            rpe = rpe
        )

        val breakdowns = routine.exercises.map { exercise ->
            val exCalories = estimateCaloricBurnUseCase.forSequence(
                category = exercise.category,
                weightKg = weight,
                sets = sets,
                reps = reps,
                rpe = rpe
            ).toInt()

            ExerciseCalorieBreakdown(
                exerciseName = exercise.name,
                categoryName = exercise.category.name,
                baseMet = exercise.metValue,
                estimatedCalories = exCalories
            )
        }

        val totalBurn = breakdowns.sumOf { it.estimatedCalories }

        _uiState.value = _uiState.value.copy(
            caloriesPerSequence = calPerSequence,
            totalRoutineCalories = totalBurn,
            exerciseBreakdowns = breakdowns
        )
    }
}
