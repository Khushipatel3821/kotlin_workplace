package com.fitpulse.app.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitpulse.app.domain.model.DayVolumeStat
import com.fitpulse.app.domain.model.MuscleGroup
import com.fitpulse.app.domain.model.StreakInfo
import com.fitpulse.app.domain.model.WorkoutSession
import com.fitpulse.app.domain.model.YogaSession
import com.fitpulse.app.domain.repository.DailyMetricRepository
import com.fitpulse.app.domain.repository.WorkoutRepository
import com.fitpulse.app.domain.repository.YogaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AnalyticsUiState(
    val totalVolumeKgAllTime: Double = 0.0,
    val totalRepsAllTime: Int = 0,
    val totalWorkoutSessionsCount: Int = 0,
    val totalYogaSessionsCount: Int = 0,
    val streakInfo: StreakInfo = StreakInfo(),
    val weeklyVolume: List<DayVolumeStat> = emptyList(),
    val workoutSessions: List<WorkoutSession> = emptyList(),
    val yogaSessions: List<YogaSession> = emptyList(),
    val muscleGroupDistribution: Map<MuscleGroup, Int> = emptyMap()
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val yogaRepository: YogaRepository,
    private val dailyMetricRepository: DailyMetricRepository
) : ViewModel() {

    val uiState: StateFlow<AnalyticsUiState> = combine(
        workoutRepository.getAllSessions(),
        yogaRepository.getAllYogaSessions(),
        dailyMetricRepository.getStreakInfo(),
        dailyMetricRepository.getLast7DaysVolume()
    ) { workouts, yoga, streak, volume ->
        val totalVolume = workouts.sumOf { it.totalVolumeKg.toDouble() }
        val totalReps = workouts.sumOf { it.totalReps }

        AnalyticsUiState(
            totalVolumeKgAllTime = totalVolume,
            totalRepsAllTime = totalReps,
            totalWorkoutSessionsCount = workouts.size,
            totalYogaSessionsCount = yoga.size,
            streakInfo = streak,
            weeklyVolume = volume,
            workoutSessions = workouts,
            yogaSessions = yoga
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )
}
