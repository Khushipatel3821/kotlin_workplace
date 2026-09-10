package com.fitpulse.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitpulse.app.domain.model.DailyMetricSummary
import com.fitpulse.app.domain.model.DayVolumeStat
import com.fitpulse.app.domain.model.StreakInfo
import com.fitpulse.app.domain.model.WorkoutSession
import com.fitpulse.app.domain.repository.DailyMetricRepository
import com.fitpulse.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val todayMetric: DailyMetricSummary = DailyMetricSummary(dateEpochDay = LocalDate.now().toEpochDay()),
    val streakInfo: StreakInfo = StreakInfo(),
    val weeklyVolume: List<DayVolumeStat> = emptyList(),
    val recentSessions: List<WorkoutSession> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dailyMetricRepository: DailyMetricRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        dailyMetricRepository.getTodayMetric(),
        dailyMetricRepository.getStreakInfo(),
        dailyMetricRepository.getLast7DaysVolume(),
        workoutRepository.getAllSessions()
    ) { todayMetric, streakInfo, weeklyVolume, sessions ->
        DashboardUiState(
            todayMetric = todayMetric,
            streakInfo = streakInfo,
            weeklyVolume = weeklyVolume,
            recentSessions = sessions.take(5),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun addWater(milliliters: Int = 250) {
        viewModelScope.launch {
            dailyMetricRepository.addWater(LocalDate.now().toEpochDay(), milliliters)
        }
    }

    fun addSteps(steps: Int) {
        viewModelScope.launch {
            val currentSteps = uiState.value.todayMetric.steps
            dailyMetricRepository.updateSteps(LocalDate.now().toEpochDay(), currentSteps + steps)
        }
    }
}
