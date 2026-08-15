package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.AppModel
import com.example.ui.theme.AkoIconShape
import com.example.ui.theme.getShapeFor
import kotlinx.coroutines.coroutineScope

/**
 * Intelligent helper to select popular default apps (YouTube, Camera, Files, Gallery, Chrome, etc.)
 * falling back to user favorites or installed apps.
 */
fun selectDefaultBubbleApps(allApps: List<AppModel>, favorites: List<AppModel>): List<AppModel> {
    if (allApps.isEmpty()) return emptyList()

    val selected = mutableListOf<AppModel>()
    val addedPackages = mutableSetOf<String>()

    fun addIfFound(predicate: (AppModel) -> Boolean) {
        val match = allApps.firstOrNull { predicate(it) && !addedPackages.contains(it.packageName) }
        if (match != null) {
            selected.add(match)
            addedPackages.add(match.packageName)
        }
    }

    // 1. YouTube
    addIfFound { 
        it.packageName.contains("youtube", ignoreCase = true) || 
        it.label.contains("youtube", ignoreCase = true) 
    }

    // 2. Camera / Appareil photo
    addIfFound { 
        it.packageName.contains("camera", ignoreCase = true) || 
        it.label.contains("camera", ignoreCase = true) ||
        it.label.contains("photo", ignoreCase = true) && it.label.contains("appareil", ignoreCase = true)
    }

    // 3. Files / Gestionnaire de fichiers
    addIfFound { 
        it.packageName.contains("documentsui", ignoreCase = true) || 
        it.packageName.contains("file", ignoreCase = true) || 
        it.label.contains("fichier", ignoreCase = true) ||
        it.label.contains("file", ignoreCase = true) ||
        it.label.contains("gestionnaire", ignoreCase = true)
    }

    // 4. Photos / Gallery
    addIfFound { 
        it.packageName.contains("photos", ignoreCase = true) || 
        it.packageName.contains("gallery", ignoreCase = true) || 
        it.label.contains("galerie", ignoreCase = true) ||
        it.label.contains("photos", ignoreCase = true)
    }

    // 5. Chrome / Browser
    addIfFound { 
        it.packageName.contains("chrome", ignoreCase = true) || 
        it.packageName.contains("browser", ignoreCase = true) || 
        it.label.contains("chrome", ignoreCase = true) ||
        it.label.contains("navigateur", ignoreCase = true)
    }

    // 6. Phone / Messages
    addIfFound { 
        it.packageName.contains("dialer", ignoreCase = true) || 
        it.packageName.contains("phone", ignoreCase = true) || 
        it.packageName.contains("messaging", ignoreCase = true) ||
        it.label.contains("téléphone", ignoreCase = true) ||
        it.label.contains("phone", ignoreCase = true)
    }

    // Fill with favorite apps if needed up to 6 apps
    for (fav in favorites) {
        if (selected.size >= 6) break
        if (!addedPackages.contains(fav.packageName)) {
            selected.add(fav)
            addedPackages.add(fav.packageName)
        }
    }

    // Fill with other available apps if still fewer than 6 apps
    for (app in allApps) {
        if (selected.size >= 6) break
        if (!addedPackages.contains(app.packageName)) {
            selected.add(app)
            addedPackages.add(app.packageName)
        }
    }

    return selected
}

/**
 * State representing a high-fidelity physical bubble.
 */
class BubbleState(
    val app: AppModel,
    val index: Int
) {
    var x by mutableStateOf(0f)
    var y by mutableStateOf(0f)
    var vx by mutableStateOf(0f)
    var vy by mutableStateOf(0f)
    var isDragging by mutableStateOf(false)
    
    var maxX by mutableStateOf(0f)
    var maxY by mutableStateOf(0f)
    
    var idlePhaseX = (Math.random() * 100f).toFloat()
    var idlePhaseY = (Math.random() * 100f).toFloat()
    
    var isInitialized by mutableStateOf(false)
}

/**
 * Beautiful full screen interactive physics bubble playground.
 * Supports fluid flinging in any direction, bouncing off screen edges, elastic bubble-to-bubble
 * collisions, and elegant background resting drift.
 * Completely optimized: Motion frames run directly on the GPU render layer.
 */
@Composable
fun FloatingBubbleField(
    apps: List<AppModel>,
    iconShape: AkoIconShape,
    onAppClick: (AppModel) -> Unit,
    onAppLongClick: (AppModel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (apps.isEmpty()) return

    val density = LocalDensity.current
    val bubbleDiameter = 74.dp
    val bubbleWidth = 85.dp
    val bubbleHeight = 105.dp

    val bubbleDiameterPx = with(density) { bubbleDiameter.toPx() }
    val bubbleWidthPx = with(density) { bubbleWidth.toPx() }
    val bubbleHeightPx = with(density) { bubbleHeight.toPx() }

    // Keep track of bubble states persistent across recompositions for the physics engine
    val bubbleStates = remember(apps) {
        apps.mapIndexed { index, app ->
            BubbleState(app, index)
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val totalHeightPx = constraints.maxHeight.toFloat()

        val maxX = (totalWidthPx - bubbleWidthPx).coerceAtLeast(0f)
        val maxY = (totalHeightPx - bubbleHeightPx).coerceAtLeast(0f)

        // Seed scattered positions
        val basePositions = remember {
            listOf(
                Pair(0.12f, 0.08f), // Top left
                Pair(0.68f, 0.05f), // Top right
                Pair(0.40f, 0.28f), // Center
                Pair(0.08f, 0.52f), // Mid left
                Pair(0.72f, 0.48f), // Mid right
                Pair(0.38f, 0.74f)  // Bottom center
            )
        }

        LaunchedEffect(maxX, maxY) {
            bubbleStates.forEachIndexed { idx, bubble ->
                bubble.maxX = maxX
                bubble.maxY = maxY
                if (!bubble.isInitialized) {
                    val pos = basePositions.getOrElse(idx) { Pair(0.5f, 0.5f) }
                    bubble.x = maxX * pos.first
                    bubble.y = maxY * pos.second
                    bubble.isInitialized = true
                }
            }
        }

        // Kinetic physics ticker loop synchronized with screen refresh rate
        LaunchedEffect(bubbleStates) {
            var lastTimeNanos = System.nanoTime()
            while (true) {
                withFrameNanos { frameTimeNanos ->
                    val dt = ((frameTimeNanos - lastTimeNanos) / 1_000_000_000f).coerceIn(0.005f, 0.03f)
                    lastTimeNanos = frameTimeNanos

                    // 1. Update positions and velocities
                    bubbleStates.forEach { bubble ->
                        if (!bubble.isDragging && bubble.isInitialized) {
                            // Apply velocity with inertia
                            bubble.x = (bubble.x + bubble.vx * dt).coerceIn(0f, bubble.maxX)
                            bubble.y = (bubble.y + bubble.vy * dt).coerceIn(0f, bubble.maxY)

                            // Friction / air resistance damping
                            bubble.vx *= 0.95f
                            bubble.vy *= 0.95f

                            // Elastic bounce off walls
                            if (bubble.x <= 0f) {
                                bubble.x = 0f
                                bubble.vx = -bubble.vx * 0.72f
                            } else if (bubble.x >= bubble.maxX) {
                                bubble.x = bubble.maxX
                                bubble.vx = -bubble.vx * 0.72f
                            }

                            if (bubble.y <= 0f) {
                                bubble.y = 0f
                                bubble.vy = -bubble.vy * 0.72f
                            } else if (bubble.y >= bubble.maxY) {
                                bubble.y = bubble.maxY
                                bubble.vy = -bubble.vy * 0.72f
                            }

                            // Gentle organic background floating if kinetic energy is near zero
                            val speed = Math.sqrt((bubble.vx * bubble.vx + bubble.vy * bubble.vy).toDouble()).toFloat()
                            if (speed < 18f) {
                                bubble.idlePhaseX += dt * 0.9f
                                bubble.idlePhaseY += dt * 0.8f
                                bubble.vx += Math.sin(bubble.idlePhaseX.toDouble()).toFloat() * 12f * dt
                                bubble.vy += Math.cos(bubble.idlePhaseY.toDouble()).toFloat() * 12f * dt
                            }
                        }
                    }

                    // 2. Resolve bubble-to-bubble elastic collisions (prevent overlapping)
                    for (i in bubbleStates.indices) {
                        for (j in (i + 1) until bubbleStates.size) {
                            val b1 = bubbleStates[i]
                            val b2 = bubbleStates[j]

                            if (!b1.isInitialized || !b2.isInitialized) continue

                            // Calculate distance between bubble circular centers
                            val center1X = b1.x + bubbleWidthPx / 2f
                            val center1Y = b1.y + bubbleDiameterPx / 2f
                            val center2X = b2.x + bubbleWidthPx / 2f
                            val center2Y = b2.y + bubbleDiameterPx / 2f

                            val dx = center2X - center1X
                            val dy = center2Y - center1Y
                            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                            // Set minimum separation distance
                            val minSeparation = bubbleDiameterPx * 0.94f
                            if (dist < minSeparation && dist > 0.1f) {
                                val overlap = minSeparation - dist

                                val nx = dx / dist
                                val ny = dy / dist

                                // Soft push to resolve overlaps
                                if (!b1.isDragging && !b2.isDragging) {
                                    b1.x = (b1.x - nx * overlap * 0.5f).coerceIn(0f, b1.maxX)
                                    b1.y = (b1.y - ny * overlap * 0.5f).coerceIn(0f, b1.maxY)
                                    b2.x = (b2.x + nx * overlap * 0.5f).coerceIn(0f, b2.maxX)
                                    b2.y = (b2.y + ny * overlap * 0.5f).coerceIn(0f, b2.maxY)
                                } else if (b1.isDragging) {
                                    b2.x = (b2.x + nx * overlap).coerceIn(0f, b2.maxX)
                                    b2.y = (b2.y + ny * overlap).coerceIn(0f, b2.maxY)
                                } else if (b2.isDragging) {
                                    b1.x = (b1.x - nx * overlap).coerceIn(0f, b1.maxX)
                                    b1.y = (b1.y - ny * overlap).coerceIn(0f, b1.maxY)
                                }

                                // Relate elastic velocity formulas
                                val rvx = b2.vx - b1.vx
                                val rvy = b2.vy - b1.vy
                                val velAlongNormal = rvx * nx + rvy * ny

                                if (velAlongNormal < 0f) {
                                    val bounceFactor = 0.8f
                                    val impulse = -(1f + bounceFactor) * velAlongNormal / 2f

                                    if (!b1.isDragging) {
                                        b1.vx -= impulse * nx
                                        b1.vy -= impulse * ny
                                    }
                                    if (!b2.isDragging) {
                                        b2.vx += impulse * nx
                                        b2.vy += impulse * ny
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Render each interactive bubble at its current dynamic coordinates
        bubbleStates.forEach { bubble ->
            InteractiveBubbleItem(
                bubble = bubble,
                iconShape = iconShape,
                onClick = { onAppClick(bubble.app) },
                onLongClick = { onAppLongClick(bubble.app) },
                modifier = Modifier.offset {
                    IntOffset(bubble.x.toInt(), bubble.y.toInt())
                }
            )
        }
    }
}

/**
 * Single Interactive Physics Bubble.
 * Leverages high performance gestures and hardware GPU rendering layers.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InteractiveBubbleItem(
    bubble: BubbleState,
    iconShape: AkoIconShape,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Smooth transition scale on press
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed || bubble.isDragging) 0.88f else 1f,
        animationSpec = spring(stiffness = 380f),
        label = "press_scale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val bubbleGradient = remember(primaryColor, secondaryColor) {
        Brush.sweepGradient(
            listOf(
                primaryColor.copy(alpha = 0.65f),
                secondaryColor.copy(alpha = 0.45f),
                primaryColor.copy(alpha = 0.75f),
                secondaryColor.copy(alpha = 0.55f),
                primaryColor.copy(alpha = 0.65f)
            )
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(85.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .pointerInput(bubble) {
                coroutineScope {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown()
                            bubble.isDragging = true
                            bubble.vx = 0f
                            bubble.vy = 0f

                            var lastDragTime = System.currentTimeMillis()
                            var dragVelocityX = 0f
                            var dragVelocityY = 0f
                            var totalDistance = 0f
                            var hasMoved = false
                            val startTime = System.currentTimeMillis()
                            var lastPosition = down.position

                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.pressed } ?: break

                                    val currentPos = change.position
                                    val delta = currentPos - lastPosition
                                    totalDistance += delta.getDistance()

                                    if (totalDistance > 8.dp.toPx()) {
                                        hasMoved = true
                                    }

                                    if (hasMoved) {
                                        bubble.x = (bubble.x + delta.x).coerceIn(0f, bubble.maxX)
                                        bubble.y = (bubble.y + delta.y).coerceIn(0f, bubble.maxY)

                                        val now = System.currentTimeMillis()
                                        val dtSec = (now - lastDragTime) / 1000f
                                        if (dtSec > 0.001f) {
                                            dragVelocityX = delta.x / dtSec
                                            dragVelocityY = delta.y / dtSec
                                        }
                                        lastDragTime = now
                                    }

                                    lastPosition = currentPos
                                    change.consume()
                                }
                            } finally {
                                bubble.isDragging = false
                                val duration = System.currentTimeMillis() - startTime
                                if (!hasMoved) {
                                    if (duration > 500) {
                                        onLongClick()
                                    } else {
                                        onClick()
                                    }
                                } else {
                                    // Set kinetic velocity on launch/fling
                                    val maxVel = 3500f
                                    bubble.vx = dragVelocityX.coerceIn(-maxVel, maxVel)
                                    bubble.vy = dragVelocityY.coerceIn(-maxVel, maxVel)
                                }
                            }
                        }
                    }
                }
            }
    ) {
        // Glowing Glass Soap-Bubble sphere
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(74.dp)
                .shadow(
                    elevation = if (bubble.isDragging) 12.dp else 5.dp,
                    shape = CircleShape,
                    ambientColor = primaryColor.copy(alpha = 0.25f),
                    spotColor = secondaryColor.copy(alpha = 0.35f)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.52f)
                        ),
                        radius = 120f
                    )
                )
                .border(
                    width = 2.dp,
                    brush = bubbleGradient,
                    shape = CircleShape
                )
        ) {
            // Glass reflection highlight
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.32f),
                                Color.Transparent
                            ),
                            radius = 70f
                        )
                    )
            )

            // Inner icon
            AppIconView(
                packageName = bubble.app.packageName,
                drawable = bubble.app.iconDrawable,
                size = 52.dp,
                shape = getShapeFor(iconShape)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // App Label
        Text(
            text = bubble.app.label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Extension helper to clamp float ranges.
 */
private fun Float.coerceIn(minimumValue: Float, maximumValue: Float): Float {
    if (this < minimumValue) return minimumValue
    if (this > maximumValue) return maximumValue
    return this
}
