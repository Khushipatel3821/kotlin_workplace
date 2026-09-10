package com.fitpulse.app.presentation.gym

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitpulse.app.domain.model.Exercise
import com.fitpulse.app.domain.model.FormQuality
import com.fitpulse.app.domain.model.WorkoutSet
import com.fitpulse.app.presentation.components.FitPulseCard
import com.fitpulse.app.presentation.components.RestTimerDialog
import com.fitpulse.app.presentation.theme.CyberOrange
import com.fitpulse.app.presentation.theme.ElectricCyan
import com.fitpulse.app.presentation.theme.NeonLime
import com.fitpulse.app.presentation.theme.PulseRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    viewModel: ActiveWorkoutViewModel,
    routineId: Long?,
    title: String,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddExerciseSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (state.sessionId == 0L) {
            viewModel.initSession(routineId, title)
        }
    }

    LaunchedEffect(state.isSessionFinished) {
        if (state.isSessionFinished) {
            onFinish()
        }
    }

    // Rest Timer Overlay
    if (state.isRestTimerActive) {
        RestTimerDialog(
            remainingSeconds = state.restSecondsRemaining,
            totalRestSeconds = state.totalRestDuration,
            onAddSeconds = { viewModel.adjustRestTimer(it) },
            onSubtractSeconds = { viewModel.adjustRestTimer(-it) },
            onSkipRest = { viewModel.skipRestTimer() }
        )
    }

    val groupedSets = remember(state.sets) {
        state.sets.groupBy { it.exerciseId to it.exerciseName }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Session Header (Timer & Finish Button)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = NeonLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDuration(state.elapsedSeconds),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime
                        )
                    }
                }

                Button(
                    onClick = { viewModel.finishSession() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Finish", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Motion Tracking & Real-Time Sensor Heuristics Card
        item {
            FitPulseCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                borderColor = if (state.isMotionTrackingActive) ElectricCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Sensor",
                            tint = if (state.isMotionTrackingActive) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Motion Rep Counter (DSP)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (state.isMotionTrackingActive) "Active Phase: ${state.motionState.currentPhase.name}" else "Sensor tracking disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.isMotionTrackingActive) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = state.isMotionTrackingActive,
                        onCheckedChange = { viewModel.toggleMotionTracking(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = ElectricCyan
                        )
                    )
                }

                AnimatedVisibility(visible = state.isMotionTrackingActive) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Reps Detected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${state.motionState.currentRepCount}",
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                                    fontWeight = FontWeight.Black,
                                    color = NeonLime
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Form Quality",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = state.motionState.lastFormQuality.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when (state.motionState.lastFormQuality) {
                                        FormQuality.EXCELLENT -> NeonLime
                                        FormQuality.GOOD -> ElectricCyan
                                        FormQuality.RUSHED_TEMPO -> CyberOrange
                                        FormQuality.INCOMPLETE_ROM -> PulseRed
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Exercise Sets Groups
        groupedSets.forEach { (exerciseKey, sets) ->
            item(key = exerciseKey.first) {
                ExerciseSetsCard(
                    exerciseName = exerciseKey.second,
                    sets = sets,
                    onAddSet = {
                        val exercise = state.availableExercises.find { it.id == exerciseKey.first }
                            ?: Exercise(id = exerciseKey.first, name = exerciseKey.second, primaryMuscle = com.fitpulse.app.domain.model.MuscleGroup.FULL_BODY, category = com.fitpulse.app.domain.model.ExerciseCategory.BARBELL)
                        viewModel.addSet(exercise)
                    },
                    onCompleteSet = { set, reps, rpe ->
                        viewModel.completeSet(set, reps, rpe)
                    }
                )
            }
        }

        // 4. Add Exercise Action Button
        item {
            OutlinedButton(
                onClick = { showAddExerciseSheet = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Exercise to Session", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal Bottom Sheet for Selecting Exercises
    if (showAddExerciseSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddExerciseSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Exercise",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.height(350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.availableExercises) { exercise ->
                        FitPulseCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
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
                                }
                                Button(
                                    onClick = {
                                        viewModel.addSet(exercise)
                                        showAddExerciseSheet = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Add")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseSetsCard(
    exerciseName: String,
    sets: List<WorkoutSet>,
    onAddSet: () -> Unit,
    onCompleteSet: (WorkoutSet, Int, Float) -> Unit
) {
    FitPulseCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            FilledTonalButton(
                onClick = onAddSet,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Set", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Table Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SET", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(36.dp))
            Text("WEIGHT (KG)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text("REPS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text("DONE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(44.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        sets.forEach { set ->
            var weightText by remember(set.weightKg) { mutableStateOf(set.weightKg.toString()) }
            var repsText by remember(set.completedReps, set.targetReps) {
                mutableStateOf(if (set.completedReps > 0) set.completedReps.toString() else set.targetReps.toString())
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${set.setNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (set.isCompleted) NeonLime else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(36.dp)
                )

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    enabled = !set.isCompleted
                )

                OutlinedTextField(
                    value = repsText,
                    onValueChange = { repsText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    enabled = !set.isCompleted
                )

                IconButton(
                    onClick = {
                        val reps = repsText.toIntOrNull() ?: set.targetReps
                        onCompleteSet(set, reps, 8.0f)
                    },
                    enabled = !set.isCompleted,
                    modifier = Modifier.width(44.dp)
                ) {
                    Icon(
                        imageVector = if (set.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Complete Set",
                        tint = if (set.isCompleted) NeonLime else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
