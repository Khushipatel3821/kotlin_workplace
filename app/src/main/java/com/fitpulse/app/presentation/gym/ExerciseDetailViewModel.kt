package com.fitpulse.app.presentation.gym

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitpulse.app.domain.model.Exercise
import com.fitpulse.app.domain.repository.WorkoutRepository
import com.fitpulse.app.domain.usecase.EstimateCaloricBurnUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetCalorieBreakdown(
    val setNumber: Int,
    val weightKg: Float,
    val targetReps: Int,
    val estimatedCalories: Float
)

data class ExerciseDetailUiState(
    val exercise: Exercise? = null,
    val isLoading: Boolean = true,
    val weightKg: Float = 15.0f,
    val setsCount: Int = 4,
    val repsCount: Int = 12,
    val rpeIntensity: Float = 7.5f,
    val caloriesPerSequence: Float = 22.0f,
    val totalCaloriesForSequence: Int = 22,
    val setBreakdowns: List<SetCalorieBreakdown> = emptyList()
)

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val estimateCaloricBurnUseCase: EstimateCaloricBurnUseCase
) : ViewModel() {

    val exerciseId: Long = savedStateHandle.get<Long>("exerciseId") ?: 1L

    private val _uiState = MutableStateFlow(ExerciseDetailUiState())
    val uiState: StateFlow<ExerciseDetailUiState> = _uiState.asStateFlow()

    init {
        loadExercise()
    }

    private fun loadExercise() {
        viewModelScope.launch {
            workoutRepository.getExerciseById(exerciseId).collectLatest { exercise ->
                val initialWeight = when (exercise?.category) {
                    com.fitpulse.app.domain.model.ExerciseCategory.BARBELL -> 40.0f
                    com.fitpulse.app.domain.model.ExerciseCategory.DUMBBELL -> 15.0f
                    com.fitpulse.app.domain.model.ExerciseCategory.BODYWEIGHT -> 0.0f
                    com.fitpulse.app.domain.model.ExerciseCategory.CABLE -> 25.0f
                    com.fitpulse.app.domain.model.ExerciseCategory.MACHINE -> 35.0f
                    else -> 15.0f
                }

                _uiState.value = _uiState.value.copy(
                    exercise = exercise,
                    weightKg = initialWeight,
                    isLoading = false
                )
                recalculateCalories()
            }
        }
    }

    fun updateWeight(weightKg: Float) {
        _uiState.value = _uiState.value.copy(weightKg = weightKg.coerceIn(0f, 200f))
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
        val exercise = currentState.exercise ?: return

        val weight = currentState.weightKg
        val sets = currentState.setsCount
        val reps = currentState.repsCount
        val rpe = currentState.rpeIntensity

        // Delegate to EstimateCaloricBurnUseCase (Single Source of Truth)
        val calPerSequence = estimateCaloricBurnUseCase.forSequence(
            category = exercise.category,
            weightKg = weight,
            sets = sets,
            reps = reps,
            rpe = rpe
        )

        val setCalorie = calPerSequence / sets.toFloat()
        val breakdowns = (1..sets).map { setNum ->
            SetCalorieBreakdown(
                setNumber = setNum,
                weightKg = weight,
                targetReps = reps,
                estimatedCalories = setCalorie
            )
        }

        _uiState.value = _uiState.value.copy(
            caloriesPerSequence = calPerSequence,
            totalCaloriesForSequence = calPerSequence.toInt(),
            setBreakdowns = breakdowns
        )
    }
}
