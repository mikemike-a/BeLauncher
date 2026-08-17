package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Semi-circle alphabet arc at the bottom of the screen.
 * Allows selecting a letter or pulling up (swipe up) to access all apps.
 */
@Composable
fun CurvedAlphabetArc(
    alphabet: List<Char>,
    onLetterSelected: (Char) -> Unit,
    onSwipeUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val displayLetters = remember(alphabet) {
        if (alphabet.isNotEmpty()) alphabet else ('A'..'Z').toList()
    }

    var componentSize by remember { mutableStateOf(IntSize.Zero) }
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    var activePosition by remember { mutableStateOf<Offset?>(null) }
    var totalDragY by remember { mutableStateOf(0f) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .onSizeChanged { componentSize = it }
            .pointerInput(displayLetters) {
                detectTapGestures(
                    onTap = { offset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val centerX = width / 2f
                        val centerY = height + 40f
                        val dx = offset.x - centerX
                        val dy = centerY - offset.y
                        val angleDeg = (atan2(dy, dx) * 180.0 / PI).toFloat()

                        if (angleDeg in 20f..160f) {
                            val t = ((155f - angleDeg) / (155f - 25f)).coerceIn(0f, 1f)
                            val index = (t * (displayLetters.size - 1)).roundToInt().coerceIn(0, displayLetters.size - 1)
                            val selected = displayLetters[index]
                            onLetterSelected(selected)
                        } else {
                            onSwipeUp()
                        }
                    }
                )
            }
            .pointerInput(displayLetters) {
                detectDragGestures(
                    onDragStart = { offset ->
                        totalDragY = 0f
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val centerX = width / 2f
                        val centerY = height + 40f
                        val dx = offset.x - centerX
                        val dy = centerY - offset.y
                        val angleDeg = (atan2(dy, dx) * 180.0 / PI).toFloat()

                        if (angleDeg in 15f..165f) {
                            val t = ((155f - angleDeg) / (155f - 25f)).coerceIn(0f, 1f)
                            val index = (t * (displayLetters.size - 1)).roundToInt().coerceIn(0, displayLetters.size - 1)
                            val newLetter = displayLetters[index]
                            if (activeLetter != newLetter) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            activeLetter = newLetter
                            activePosition = offset
                        }
                    },
                    onDragEnd = {
                        if (totalDragY < -50f) {
                            if (activeLetter != null) {
                                onLetterSelected(activeLetter!!)
                            } else {
                                onSwipeUp()
                            }
                        } else if (activeLetter != null) {
                            onLetterSelected(activeLetter!!)
                        }
                        activeLetter = null
                        activePosition = null
                        totalDragY = 0f
                    },
                    onDragCancel = {
                        activeLetter = null
                        activePosition = null
                        totalDragY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragY += dragAmount.y

                        if (totalDragY < -120f) {
                            // Quick swipe up detected
                            if (activeLetter != null) {
                                onLetterSelected(activeLetter!!)
                            } else {
                                onSwipeUp()
                            }
                            activeLetter = null
                            activePosition = null
                            return@detectDragGestures
                        }

                        val pos = change.position
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val centerX = width / 2f
                        val centerY = height + 40f
                        val dx = pos.x - centerX
                        val dy = centerY - pos.y
                        val angleDeg = (atan2(dy, dx) * 180.0 / PI).toFloat()

                        if (angleDeg in 15f..165f) {
                            val t = ((155f - angleDeg) / (155f - 25f)).coerceIn(0f, 1f)
                            val index = (t * (displayLetters.size - 1)).roundToInt().coerceIn(0, displayLetters.size - 1)
                            val newLetter = displayLetters[index]
                            if (activeLetter != newLetter) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            activeLetter = newLetter
                            activePosition = pos
                        }
                    }
                )
            }
    ) {
        // Draw background arc and glowing track
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height + 35.dp.toPx()
            val radius = height + 10.dp.toPx()

            // Subtle gradient arc background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        surfaceColor.copy(alpha = 0.55f),
                        surfaceColor.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius + 25.dp.toPx()
                ),
                radius = radius + 25.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        // Swipe up indicator in top-center of arc
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-4).dp)
                .alpha(0.85f)
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowUp,
                contentDescription = "Tirer vers le haut pour les applications",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Position individual letters along the semi-circle arc
        if (componentSize.width > 0 && componentSize.height > 0) {
            val width = componentSize.width.toFloat()
            val height = componentSize.height.toFloat()
            val centerX = width / 2f
            val centerY = height + 35.dp.value * (width / 360f).coerceIn(0.8f, 1.5f)
            val radius = height + 10.dp.value * (width / 360f).coerceIn(0.8f, 1.5f)

            val startAngleDeg = 156f
            val endAngleDeg = 24f
            val count = displayLetters.size

            displayLetters.forEachIndexed { index, letter ->
                val fraction = if (count > 1) index.toFloat() / (count - 1) else 0.5f
                val angleDeg = startAngleDeg - fraction * (startAngleDeg - endAngleDeg)
                val angleRad = (angleDeg * PI / 180.0).toFloat()

                val letterX = centerX + radius * cos(angleRad)
                val letterY = centerY - radius * sin(angleRad)

                val isHovered = activeLetter == letter
                val letterScale by animateFloatAsState(
                    targetValue = if (isHovered) 1.4f else 1.0f,
                    animationSpec = spring(),
                    label = "letterScale"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (letterX - 12.dp.toPx()).roundToInt(),
                                y = (letterY - 12.dp.toPx()).roundToInt()
                            )
                        }
                        .size(24.dp)
                ) {
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isHovered) FontWeight.ExtraBold else FontWeight.SemiBold,
                            fontSize = (11 * letterScale).sp
                        ),
                        color = if (isHovered) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Active Letter Floating Badge / Magnifier
        AnimatedVisibility(
            visible = activeLetter != null,
            enter = scaleIn(animationSpec = spring()) + fadeIn(),
            exit = scaleOut(animationSpec = tween(100)) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .offset(y = (-36).dp)
                    .size(54.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeLetter?.toString() ?: "",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
