package com.example.ui.home

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.AppModel
import com.example.data.WorkspaceItem
import com.example.ui.components.AkoCulturalBackground
import com.example.ui.components.AppIconView
import com.example.ui.components.CurvedAlphabetArc
import com.example.ui.components.FloatingBubbleField
import com.example.ui.components.GlassCard
import com.example.ui.components.selectDefaultBubbleApps
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.gestures.detectTapGestures
import com.example.ui.components.AkoClockWidget

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    greetingMessage: String,
    favorites: List<AppModel>,
    allApps: List<AppModel>,
    workspaceItems: Set<String>,
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    onAddWorkspaceItem: (String, Boolean) -> Unit, // Add App or Widget
    onRemoveWorkspaceItem: (Int, Int, Int) -> Unit, // Remove by Page, Row, Col
    groupedApps: Map<Char, List<AppModel>>,
    alphabetIndex: List<Char>,
    iconSizeDp: Int,
    iconShape: com.example.ui.theme.AkoIconShape,
    clockStyle: String = "AKO",
    showAppLabels: Boolean = true,
    themedIcons: Boolean = false,
    doubleTapToLock: Boolean = true,
    onLockDevice: () -> Unit = {},
    onAppClick: (AppModel) -> Unit,
    onAppLongClick: (AppModel) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDrawer: (Char?) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    val pagerState = rememberPagerState(pageCount = { 3 })
    var showOptionsMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.FRENCH)
        val dateFormat = SimpleDateFormat("EEEE d MMMM", Locale.FRENCH)
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now).replaceFirstChar { it.uppercase() }
            delay(1000)
        }
    }

    val bubbleApps = remember(allApps, favorites) {
        selectDefaultBubbleApps(allApps, favorites)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInteropFilter { motionEvent ->
                if (motionEvent.action == android.view.MotionEvent.ACTION_DOWN) {
                    if (motionEvent.buttonState == android.view.MotionEvent.BUTTON_SECONDARY) {
                        showOptionsMenu = true
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
            .pointerInput(doubleTapToLock) {
                detectTapGestures(
                    onDoubleTap = {
                        if (doubleTapToLock) {
                            onLockDevice()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -25f) {
                        onOpenDrawer(null)
                    }
                }
            }
    ) {
        AkoCulturalBackground(
            color = MaterialTheme.colorScheme.secondary
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            if (pagerState.currentPage == 0) {
                AkoClockWidget(
                    clockStyle = clockStyle,
                    greetingMessage = greetingMessage,
                    currentTime = currentTime,
                    currentDate = currentDate
                )

                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Workspace Section (takes remaining available space)
            val parsedItems = remember(workspaceItems) {
                workspaceItems.mapNotNull { WorkspaceItem.fromPrefString(it) }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    if (page == 0) {
                        FloatingBubbleField(
                            apps = bubbleApps,
                            iconShape = iconShape,
                            isThemed = themedIcons,
                            showLabels = showAppLabels,
                            onAppClick = onAppClick,
                            onAppLongClick = onAppLongClick,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                        for (row in 0..3) { // 4 rows
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                for (col in 0..3) { // 4 columns
                                    val item = parsedItems.find { it.page == page && it.row == row && it.col == col }

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .then(
                                                if (item == null) {
                                                    Modifier.combinedClickable(
                                                        onClick = {},
                                                        onLongClick = { showOptionsMenu = true }
                                                    )
                                                } else {
                                                    Modifier
                                                }
                                            )
                                    ) {
                                        if (item != null) {
                                            if (item.isWidget) {
                                                // Render Widget
                                                val widgetId = item.identifier.toIntOrNull() ?: return@Box
                                                val appWidgetInfo = appWidgetManager.getAppWidgetInfo(widgetId)
                                                if (appWidgetInfo != null) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(4.dp)
                                                            .shadow(4.dp, RoundedCornerShape(16.dp))
                                                            .clip(RoundedCornerShape(16.dp))
                                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                                            .combinedClickable(
                                                                onClick = {},
                                                                onLongClick = { onRemoveWorkspaceItem(page, row, col) }
                                                            )
                                                    ) {
                                                        AndroidView(
                                                            factory = { context ->
                                                                appWidgetHost.createView(context, widgetId, appWidgetInfo).apply {
                                                                    setAppWidget(widgetId, appWidgetInfo)
                                                                }
                                                            },
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(8.dp)
                                                        )
                                                    }
                                                }
                                            } else {
                                                val appModel = allApps.find { it.packageName == item.identifier }
                                                if (appModel != null) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier
                                                            .combinedClickable(
                                                                onClick = { onAppClick(appModel) },
                                                                onLongClick = { onAppLongClick(appModel) }
                                                            )
                                                            .padding(4.dp)
                                                    ) {
                                                        AppIconView(
                                                            packageName = appModel.packageName,
                                                            drawable = appModel.iconDrawable,
                                                            size = 64.dp,
                                                            shape = com.example.ui.theme.getShapeFor(iconShape),
                                                            isThemed = themedIcons
                                                        )
                                                        if (showAppLabels) {
                                                            Spacer(modifier = Modifier.height(6.dp))
                                                            Text(
                                                                text = appModel.label,
                                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                                                color = MaterialTheme.colorScheme.onBackground,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                                textAlign = TextAlign.Center
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

                // Pager Indicators
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val color = if (pagerState.currentPage == iteration) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(if (pagerState.currentPage == iteration) 8.dp else 6.dp)
                        )
                    }
                }
            }

            // Semi-circle Alphabet Arc at the bottom of the screen
            CurvedAlphabetArc(
                alphabet = alphabetIndex,
                onLetterSelected = { letter -> onOpenDrawer(letter) },
                onSwipeUp = { onOpenDrawer(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        if (showOptionsMenu) {
            val context = LocalContext.current
            WorkspaceOptionsMenu(
                onAddWidget = {
                    onAddWorkspaceItem("", true)
                },
                onChangeWallpaper = {
                    try {
                        val intent = Intent(Intent.ACTION_SET_WALLPAPER)
                        context.startActivity(Intent.createChooser(intent, "Choisir un fond d'écran"))
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "Impossible d'ouvrir le sélecteur", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                onOpenSettings = onOpenSettings,
                onDismiss = { showOptionsMenu = false }
            )
        }
    }
}

@Composable
fun WorkspaceOptionsMenu(
    onAddWidget: () -> Unit,
    onChangeWallpaper: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            shape = RoundedCornerShape(28.dp),
            elevation = 8.dp,
            modifier = Modifier
                .width(280.dp)
                .clickable(enabled = false, onClick = {}) // prevent click propagation
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Options de l'écran",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Button 1: Ajouter un widget
                MenuOptionItem(
                    icon = Icons.Rounded.Widgets,
                    label = "Ajouter un widget",
                    onClick = {
                        onDismiss()
                        onAddWidget()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Button 2: Changer le fond d'écran
                MenuOptionItem(
                    icon = Icons.Rounded.Wallpaper,
                    label = "Changer le papier peint",
                    onClick = {
                        onDismiss()
                        onChangeWallpaper()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Button 3: Paramètres du lanceur
                MenuOptionItem(
                    icon = Icons.Rounded.Settings,
                    label = "Paramètres",
                    onClick = {
                        onDismiss()
                        onOpenSettings()
                    }
                )
            }
        }
    }
}

@Composable
fun MenuOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

