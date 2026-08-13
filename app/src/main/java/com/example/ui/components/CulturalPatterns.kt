package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AkoCulturalBackground(
    color: Color,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Soft Ambient Glowing Orbs for Glassmorphism depth
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(width * 0.15f, height * 0.2f),
                radius = width * 0.55f
            ),
            radius = width * 0.55f,
            center = Offset(width * 0.15f, height * 0.2f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondary.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(width * 0.85f, height * 0.45f),
                radius = width * 0.5f
            ),
            radius = width * 0.5f,
            center = Offset(width * 0.85f, height * 0.45f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(tertiary.copy(alpha = 0.16f), Color.Transparent),
                center = Offset(width * 0.3f, height * 0.8f),
                radius = width * 0.6f
            ),
            radius = width * 0.6f,
            center = Offset(width * 0.3f, height * 0.8f)
        )

        // 2. Beninese Cauri Pattern Mesh Overlay
        val patternStepX = 120.dp.toPx()
        val patternStepY = 120.dp.toPx()
        val strokeWidth = 1.2.dp.toPx()

        var y = 0f
        while (y < height + patternStepY) {
            var x = 0f
            var colIndex = 0
            while (x < width + patternStepX) {
                val offsetX = if (colIndex % 2 == 1) patternStepX / 2 else 0f
                val centerX = x + offsetX
                val centerY = y

                val path = Path().apply {
                    moveTo(centerX, centerY - 16)
                    quadraticTo(centerX + 16, centerY, centerX, centerY + 16)
                    quadraticTo(centerX - 16, centerY, centerX, centerY - 16)
                    close()
                }

                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.12f),
                    style = Stroke(width = strokeWidth)
                )

                drawLine(
                    color = color.copy(alpha = 0.12f),
                    start = Offset(centerX, centerY - 8),
                    end = Offset(centerX, centerY + 8),
                    strokeWidth = strokeWidth * 0.8f
                )

                x += patternStepX
                colIndex++
            }
            y += patternStepY
        }
    }
}
