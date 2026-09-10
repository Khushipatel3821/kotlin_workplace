package com.fitpulse.app.presentation.gym

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitpulse.app.domain.model.Exercise
import com.fitpulse.app.domain.model.MuscleGroup
import com.fitpulse.app.domain.model.WorkoutRoutine
import com.fitpulse.app.presentation.components.FitPulseCard
import com.fitpulse.app.presentation.theme.ElectricCyan
import com.fitpulse.app.presentation.theme.NeonLime

@Composable
fun GymTrackerScreen(
    exercises: List<Exercise>,
    routines: List<WorkoutRoutine>,
    onStartRoutine: (routineId: Long, title: String) -> Unit,
    onStartEmptyWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMuscleFilter by remember { mutableStateOf<MuscleGroup?>(null) }

    val filteredExercises = remember(exercises, selectedMuscleFilter) {
        if (selectedMuscleFilter == null) exercises
        else exercises.filter { it.primaryMuscle == selectedMuscleFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Header & Quick Start Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Strength Training",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 26.sp),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Routines, Rep-Tracking & Exercises",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onStartEmptyWorkout,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Empty Workout", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Structured Workout Routines Section
        item {
            Text(
                text = "Pre-Built Routines",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(routines) { routine ->
            RoutineCardItem(routine = routine, onStart = { onStartRoutine(routine.id, routine.name) })
        }

        // 3. Exercise Catalog with Muscle Group Filter
        item {
            Column {
                Text(
                    text = "Exercise Library (${filteredExercises.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedMuscleFilter == null,
                            onClick = { selectedMuscleFilter = null },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonLime,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                    items(MuscleGroup.values()) { muscle ->
                        FilterChip(
                            selected = selectedMuscleFilter == muscle,
                            onClick = { selectedMuscleFilter = muscle },
                            label = { Text(muscle.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonLime,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }
        }

        items(filteredExercises) { exercise ->
            ExerciseCatalogItem(exercise = exercise)
        }
    }
}

@Composable
fun RoutineCardItem(routine: WorkoutRoutine, onStart: () -> Unit) {
    FitPulseCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = routine.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${routine.estimatedDurationMinutes} mins • ${routine.exercises.size} exercises",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = ElectricCyan
                    )
                }
            }

            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Start")
            }
        }
    }
}

@Composable
fun ExerciseCatalogItem(exercise: Exercise) {
    FitPulseCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${exercise.primaryMuscle.name} • ${exercise.category.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (exercise.instructions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = exercise.instructions,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}
