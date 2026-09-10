package com.fitpulse.app.presentation.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitpulse.app.presentation.components.FitPulseCard
import com.fitpulse.app.presentation.components.WeeklyVolumeChart
import com.fitpulse.app.presentation.dashboard.WorkoutSessionRowItem
import com.fitpulse.app.presentation.theme.CyberOrange
import com.fitpulse.app.presentation.theme.ElectricCyan
import com.fitpulse.app.presentation.theme.NeonLime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title
        item {
            Column {
                Text(
                    text = "Performance & History",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 26.sp),
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Lifetime Metrics & Consistency Analytics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Lifetime Summary 2x2 Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStatsCard(
                        title = "Total Volume",
                        value = "${(state.totalVolumeKgAllTime / 1000.0).format(1)} Tons",
                        color = NeonLime,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatsCard(
                        title = "Total Reps",
                        value = "${state.totalRepsAllTime}",
                        color = ElectricCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStatsCard(
                        title = "Gym Sessions",
                        value = "${state.totalWorkoutSessionsCount}",
                        color = CyberOrange,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatsCard(
                        title = "Yoga & Mindfulness",
                        value = "${state.totalYogaSessionsCount}",
                        color = Color(0xFFAB47BC),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Weekly Volume Chart
        item {
            WeeklyVolumeChart(weeklyStats = state.weeklyVolume)
        }

        // 4. Workout History Log
        item {
            Text(
                text = "Gym Workout History (${state.workoutSessions.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(state.workoutSessions) { session ->
            WorkoutSessionRowItem(session = session)
        }

        // 5. Yoga History Log
        if (state.yogaSessions.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Yoga & Mindfulness History (${state.yogaSessions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(state.yogaSessions) { yoga ->
                val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(yoga.startedAtTimestamp))
                FitPulseCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Text(
                        text = yoga.routineTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${yoga.durationSeconds / 60} mins • ${yoga.completedPoses} poses • ${yoga.breathingPatternName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElectricCyan
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryStatsCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    FitPulseCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        borderColor = color.copy(alpha = 0.3f)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)
