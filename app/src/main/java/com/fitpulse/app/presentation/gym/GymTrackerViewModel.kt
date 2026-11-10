package com.fitpulse.app.presentation.gym

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitpulse.app.domain.model.Exercise
import com.fitpulse.app.domain.model.WorkoutRoutine
import com.fitpulse.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GymTrackerUiState(
    val exercises: List<Exercise> = emptyList(),
    val routines: List<WorkoutRoutine> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class GymTrackerViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GymTrackerUiState())
    val uiState: StateFlow<GymTrackerUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            workoutRepository.getAllExercises().collectLatest { exercises ->
                _uiState.value = _uiState.value.copy(exercises = exercises)
            }
        }

        viewModelScope.launch {
            workoutRepository.getAllRoutines().collectLatest { routines ->
                if (routines.isEmpty()) {
                    seedDefaultRoutinesIfEmpty()
                } else {
                    _uiState.value = _uiState.value.copy(routines = routines, isLoading = false)
                }
            }
        }
    }

    private suspend fun seedDefaultRoutinesIfEmpty() {
        try {
            val pushRoutine = WorkoutRoutine(
                id = 1,
                name = "Push Power Hypertrophy",
                description = "Target chest, shoulders & triceps strength with progressive overload.",
                targetMuscles = listOf(
                    com.fitpulse.app.domain.model.MuscleGroup.CHEST,
                    com.fitpulse.app.domain.model.MuscleGroup.SHOULDERS,
                    com.fitpulse.app.domain.model.MuscleGroup.TRICEPS
                ),
                estimatedDurationMinutes = 45,
                exercises = listOf(
                    Exercise(1, "Barbell Bench Press", com.fitpulse.app.domain.model.MuscleGroup.CHEST, listOf(com.fitpulse.app.domain.model.MuscleGroup.TRICEPS, com.fitpulse.app.domain.model.MuscleGroup.SHOULDERS), com.fitpulse.app.domain.model.ExerciseCategory.BARBELL, "Lie on bench, grip bar wide, press up.", 120),
                    Exercise(6, "Incline Dumbbell Press", com.fitpulse.app.domain.model.MuscleGroup.CHEST, listOf(com.fitpulse.app.domain.model.MuscleGroup.SHOULDERS), com.fitpulse.app.domain.model.ExerciseCategory.DUMBBELL, "Set bench 30 deg, press dumbbells.", 90),
                    Exercise(4, "Overhead Barbell Press", com.fitpulse.app.domain.model.MuscleGroup.SHOULDERS, listOf(com.fitpulse.app.domain.model.MuscleGroup.TRICEPS), com.fitpulse.app.domain.model.ExerciseCategory.BARBELL, "Press bar overhead vertically.", 120),
                    Exercise(9, "Tricep Rope Pushdown", com.fitpulse.app.domain.model.MuscleGroup.TRICEPS, emptyList(), com.fitpulse.app.domain.model.ExerciseCategory.CABLE, "Extend arms downward from pulley.", 60)
                )
            )

            val pullRoutine = WorkoutRoutine(
                id = 2,
                name = "Pull & Back Sculpt",
                description = "Build back thickness, lat width, and bicep isolation.",
                targetMuscles = listOf(
                    com.fitpulse.app.domain.model.MuscleGroup.BACK,
                    com.fitpulse.app.domain.model.MuscleGroup.BICEPS
                ),
                estimatedDurationMinutes = 50,
                exercises = listOf(
                    Exercise(3, "Conventional Deadlift", com.fitpulse.app.domain.model.MuscleGroup.BACK, listOf(com.fitpulse.app.domain.model.MuscleGroup.LEGS, com.fitpulse.app.domain.model.MuscleGroup.CORE), com.fitpulse.app.domain.model.ExerciseCategory.BARBELL, "Hinge at hips, pull bar to lockout.", 180),
                    Exercise(7, "Bodyweight Pull-Up", com.fitpulse.app.domain.model.MuscleGroup.BACK, listOf(com.fitpulse.app.domain.model.MuscleGroup.BICEPS), com.fitpulse.app.domain.model.ExerciseCategory.BODYWEIGHT, "Overhand grip, pull chin over bar.", 90),
                    Exercise(5, "Barbell Bent-Over Row", com.fitpulse.app.domain.model.MuscleGroup.BACK, listOf(com.fitpulse.app.domain.model.MuscleGroup.BICEPS), com.fitpulse.app.domain.model.ExerciseCategory.BARBELL, "Hinge 45 deg, pull bar to rib cage.", 90),
                    Exercise(8, "Standing Dumbbell Bicep Curl", com.fitpulse.app.domain.model.MuscleGroup.BICEPS, emptyList(), com.fitpulse.app.domain.model.ExerciseCategory.DUMBBELL, "Curl dumbbells with elbows tucked.", 60)
                )
            )

            val legsRoutine = WorkoutRoutine(
                id = 3,
                name = "Legs & Core Dynamic",
                description = "Quad dominance, hamstring stretch and core stability overload.",
                targetMuscles = listOf(
                    com.fitpulse.app.domain.model.MuscleGroup.LEGS,
                    com.fitpulse.app.domain.model.MuscleGroup.CORE
                ),
                estimatedDurationMinutes = 55,
                exercises = listOf(
                    Exercise(2, "Barbell Back Squat", com.fitpulse.app.domain.model.MuscleGroup.LEGS, listOf(com.fitpulse.app.domain.model.MuscleGroup.CORE), com.fitpulse.app.domain.model.ExerciseCategory.BARBELL, "Squat parallel, drive through midfoot.", 150),
                    Exercise(10, "Romanian Deadlift (RDL)", com.fitpulse.app.domain.model.MuscleGroup.LEGS, listOf(com.fitpulse.app.domain.model.MuscleGroup.BACK), com.fitpulse.app.domain.model.ExerciseCategory.BARBELL, "Hinge back for deep hamstring stretch.", 120)
                )
            )

            workoutRepository.insertRoutine(pushRoutine)
            workoutRepository.insertRoutine(pullRoutine)
            workoutRepository.insertRoutine(legsRoutine)
        } catch (_: Exception) {
            // Fallback gracefully
        }
    }
}
