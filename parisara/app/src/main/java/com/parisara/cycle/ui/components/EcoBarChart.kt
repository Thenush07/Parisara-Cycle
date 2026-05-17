package com.parisara.cycle.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parisara.cycle.data.model.EcoCalculator

@Composable
fun EcoBarChart(
    dailyKm: Double,
    weeklyKm: Double,
    monthlyKm: Double,
    modifier: Modifier = Modifier
) {
    val bars = listOf(
        "Today" to dailyKm,
        "Week" to weeklyKm,
        "Month" to monthlyKm
    )
    val maxKm = bars.maxOf { it.second }.coerceAtLeast(0.5)
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val textColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            bars.forEach { (label, km) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        EcoCalculator.formatCo2(EcoCalculator.co2FromDistanceKm(km)),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Canvas(
                        modifier = Modifier
                            .width(48.dp)
                            .height(100.dp)
                    ) {
                        val radius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        drawRoundRect(
                            color = trackColor,
                            size = size,
                            cornerRadius = radius
                        )
                        val barHeight = (km / maxKm * size.height).toFloat().coerceAtLeast(8f)
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(0f, size.height - barHeight),
                            size = Size(size.width, barHeight),
                            cornerRadius = radius
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        label, 
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor
                    )
                    Text(
                        "${"%.1f".format(km)} km",
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryTextColor
                    )
                }
            }
        }
    }
}
