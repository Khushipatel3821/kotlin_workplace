package com.fitpulse.app.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitpulse.app.domain.model.WorkoutSession
import com.fitpulse.app.presentation.components.FitPulseCard
import com.fitpulse.app.presentation.components.MetricTile
import com.fitpulse.app.presentation.components.WeeklyVolumeChart
import com.fitpulse.app.presentation.theme.CyberOrange
import com.fitpulse.app.presentation.theme.ElectricCyan
import com.fitpulse.app.presentation.theme.NeonLime
import com.fitpulse.app.presentation.theme.NeonLimeDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onStartWorkout: (routineId: Long?, title: String) -> Unit,
    onOpenYoga: () -> Unit,
    onOpenAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Header & Daily Streak Badge
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FitPulse",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Offline-First Fitness & Motion",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Streak Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CyberOrange.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberOrange.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Streak",
                            tint = CyberOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${state.streakInfo.currentStreakDays} Days",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = CyberOrange
                        )
                    }
                }
            }
        }

        // 2. Hero Quick Action Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(NeonLimeDark, Color(0xFF1E3A1E))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Ready to Train?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Rep-counting heuristics & motion tracking enabled.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onStartWorkout(null, "Quick Strength Session") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonLime,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Gym", fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = onOpenYoga,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.2f),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.SelfImprovement, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Yoga Flow")
                            }
                        }
                    }
                }
            }
        }

        // 3. 2x2 Metric Tiles
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricTile(
                        title = "Steps",
                        value = "${state.todayMetric.steps}",
                        subtitle = "Goal: ${state.todayMetric.stepGoal}",
                        icon = Icons.Default.DirectionsRun,
                        progress = state.todayMetric.stepProgress,
                        accentColor = ElectricCyan,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = "Calories",
                        value = "${state.todayMetric.caloriesBurned} kcal",
                        subtitle = "Goal: ${state.todayMetric.calorieGoal}",
                        icon = Icons.Default.LocalFireDepartment,
                        progress = state.todayMetric.calorieProgress,
                        accentColor = CyberOrange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricTile(
                        title = "Active Time",
                        value = "${state.todayMetric.activeMinutes} min",
                        subtitle = "Goal: ${state.todayMetric.activeMinutesGoal} min",
                        icon = Icons.Default.Timer,
                        progress = state.todayMetric.activeMinuteProgress,
                        accentColor = NeonLime,
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        title = "Water Intake",
                        value = "${state.todayMetric.waterMilliliters} ml",
                        subtitle = "Tap +250ml",
                        icon = Icons.Default.Opacity,
                        progress = (state.todayMetric.waterMilliliters.toFloat() / state.todayMetric.waterGoalMilliliters).coerceIn(0f, 1f),
                        accentColor = Color(0xFF29B6F6),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Weekly Volume Chart
        item {
            WeeklyVolumeChart(weeklyStats = state.weeklyVolume)
        }

        // 5. Recent Sessions
        if (state.recentSessions.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Workouts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(state.recentSessions) { session ->
                WorkoutSessionRowItem(session = session)
            }
        }
    }
}

@Composable
fun WorkoutSessionRowItem(session: WorkoutSession) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(session.startedAtTimestamp))

    FitPulseCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${session.totalVolumeKg.toInt()} kg",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonLime
                )
                Text(
                    text = "${session.durationSeconds / 60} min • ${session.totalReps} reps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
