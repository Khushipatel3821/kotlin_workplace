package com.fitpulse.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.app.domain.model.DayVolumeStat
import com.fitpulse.app.presentation.theme.NeonLime

@Composable
fun WeeklyVolumeChart(
    weeklyStats: List<DayVolumeStat>,
    modifier: Modifier = Modifier,
    barColor: Color = NeonLime,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val maxVolume = weeklyStats.maxOfOrNull { it.totalVolumeKg }?.takeIf { it > 0f } ?: 1000f

    FitPulseCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Weekly Workout Volume",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total Tonnage (kg) Lifted Across Days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${weeklyStats.sumOf { it.totalVolumeKg.toDouble() }.toInt()} kg",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(horizontal = 4.dp)
        ) {
            val totalBars = weeklyStats.size.coerceAtLeast(1)
            val barWidth = size.width / (totalBars * 2.2f)
            val spacing = (size.width - (barWidth * totalBars)) / (totalBars + 1)
            val cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)

            weeklyStats.forEachIndexed { index, stat ->
                val xPos = spacing + index * (barWidth + spacing)
                val barHeightRatio = (stat.totalVolumeKg / maxVolume).coerceIn(0.04f, 1.0f)
                val barHeight = size.height * barHeightRatio
                val yPos = size.height - barHeight

                // Draw background track
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(xPos, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = cornerRadius
                )

                // Draw active volume bar
                if (stat.totalVolumeKg > 0f) {
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(xPos, yPos),
                        size = Size(barWidth, barHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklyStats.forEach { stat ->
                Text(
                    text = stat.dayLabel,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
