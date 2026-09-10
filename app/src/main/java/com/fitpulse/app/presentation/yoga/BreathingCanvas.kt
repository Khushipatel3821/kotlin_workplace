package com.fitpulse.app.presentation.yoga

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.app.domain.model.BreathingPhase
import com.fitpulse.app.domain.usecase.BreathingState
import com.fitpulse.app.presentation.theme.ElectricCyan
import com.fitpulse.app.presentation.theme.NeonLime

@Composable
fun BreathingCanvas(
    state: BreathingState,
    modifier: Modifier = Modifier
) {
    // Dynamic scale factor based on breathing phase
    val targetScale = when (state.phase) {
        BreathingPhase.INHALE -> 0.4f + (0.6f * state.phaseProgress)
        BreathingPhase.HOLD_IN -> 1.0f
        BreathingPhase.EXHALE -> 1.0f - (0.6f * state.phaseProgress)
        BreathingPhase.HOLD_OUT -> 0.4f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        label = "BreathingScale"
    )

    val phaseColor = when (state.phase) {
        BreathingPhase.INHALE -> ElectricCyan
        BreathingPhase.HOLD_IN -> NeonLime
        BreathingPhase.EXHALE -> Color(0xFF81D4FA)
        BreathingPhase.HOLD_OUT -> Color(0xFFB0BEC5)
    }

    val phaseTitle = when (state.phase) {
        BreathingPhase.INHALE -> "INHALE"
        BreathingPhase.HOLD_IN -> "HOLD"
        BreathingPhase.EXHALE -> "EXHALE"
        BreathingPhase.HOLD_OUT -> "HOLD"
    }

    Box(
        modifier = modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2.2f
            val currentRadius = maxRadius * animatedScale

            // Outer subtle boundary ring
            drawCircle(
                color = phaseColor.copy(alpha = 0.15f),
                radius = maxRadius,
                center = centerOffset,
                style = Stroke(width = 2.dp.toPx())
            )

            // Dynamic pulsing gradient aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        phaseColor.copy(alpha = 0.45f),
                        phaseColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = currentRadius * 1.25f
                ),
                radius = currentRadius * 1.25f,
                center = centerOffset
            )

            // Inner core glowing circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(phaseColor, phaseColor.copy(alpha = 0.8f)),
                    center = centerOffset,
                    radius = currentRadius
                ),
                radius = currentRadius,
                center = centerOffset
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = phaseTitle,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            val remainingInPhase = (state.phaseTotalSeconds - state.phaseElapsedSeconds).coerceAtLeast(0f)
            Text(
                text = "%.1fs".format(remainingInPhase),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = "Cycle ${state.currentCycle} / ${state.totalCycles}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
