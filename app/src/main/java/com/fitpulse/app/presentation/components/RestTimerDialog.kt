package com.fitpulse.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.fitpulse.app.presentation.theme.NeonLime

@Composable
fun RestTimerDialog(
    remainingSeconds: Int,
    totalRestSeconds: Int,
    onAddSeconds: (Int) -> Unit,
    onSubtractSeconds: (Int) -> Unit,
    onSkipRest: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (totalRestSeconds > 0) remainingSeconds.toFloat() / totalRestSeconds else 0f,
        label = "RestProgress"
    )

    Dialog(onDismissRequest = onSkipRest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rest Period",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(160.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.size(160.dp),
                        strokeWidth = 10.dp,
                        color = NeonLime,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${remainingSeconds}s",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Adjustment Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { onSubtractSeconds(15) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Minus 15s")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("15s")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    FilledTonalButton(
                        onClick = { onAddSeconds(15) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Plus 15s")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("15s")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onSkipRest,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime, contentColor = MaterialTheme.colorScheme.scrim),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Skip Rest & Start Next Set", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
