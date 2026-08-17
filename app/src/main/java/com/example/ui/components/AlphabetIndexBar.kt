package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun AlphabetIndexBar(
    alphabet: List<Char>,
    currentLetter: Char? = null,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    if (alphabet.isEmpty()) return

    var activeLetter by remember { mutableStateOf<Char?>(null) }
    var activeIndex by remember { mutableStateOf(-1) }
    var columnHeight by remember { mutableStateOf(0f) }

    val effectiveLetter = activeLetter ?: currentLetter

    Box(modifier = modifier) {
        // Floating Bubble
        AnimatedVisibility(
            visible = activeLetter != null,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150)),
            modifier = Modifier.matchParentSize()
        ) {
            Box(modifier = Modifier.matchParentSize()) {
                if (activeIndex >= 0 && columnHeight > 0) {
                    val itemHeight = columnHeight / alphabet.size
                    val bubbleY = (activeIndex * itemHeight) + (itemHeight / 2)
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset {
                                IntOffset(
                                    x = -72.dp.roundToPx(), // move to the left
                                    y = (bubbleY - 28.dp.toPx()).roundToInt() // center vertically around item
                                )
                            }
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeLetter?.toString() ?: "",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        // Alphabet Column
        Column(
            modifier = Modifier
                .onSizeChanged { columnHeight = it.height.toFloat() }
                .width(28.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                .padding(vertical = 8.dp)
                .pointerInput(alphabet) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val itemHeight = size.height / alphabet.size
                        var index = (down.position.y / itemHeight).toInt().coerceIn(0, alphabet.size - 1)
                        activeLetter = alphabet[index]
                        activeIndex = index
                        onLetterSelected(alphabet[index])
                        
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null && change.pressed) {
                                index = (change.position.y / itemHeight).toInt().coerceIn(0, alphabet.size - 1)
                                if (activeLetter != alphabet[index]) {
                                    activeLetter = alphabet[index]
                                    activeIndex = index
                                    onLetterSelected(alphabet[index])
                                }
                            }
                        } while (event.changes.any { it.pressed })
                        
                        activeLetter = null
                        activeIndex = -1
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            alphabet.forEach { letter ->
                val isBeingTouched = activeLetter == letter
                val isCurrentInScroll = effectiveLetter == letter
                val scale by animateFloatAsState(
                    targetValue = if (isBeingTouched) 1.6f else if (isCurrentInScroll) 1.25f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "letterScale"
                )
                val color = if (isBeingTouched || isCurrentInScroll) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isBeingTouched || isCurrentInScroll) FontWeight.ExtraBold else FontWeight.Bold
                        ),
                        color = color,
                        modifier = Modifier.scale(scale)
                    )
                }
            }
        }
    }
}
