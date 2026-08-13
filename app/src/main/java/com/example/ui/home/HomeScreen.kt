package com.example.ui.home

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.AppModel
import com.example.data.WorkspaceItem
import com.example.ui.components.AkoCulturalBackground
import com.example.ui.components.AlphabetIndexBar
import com.example.ui.components.AppIconView
import com.example.ui.components.GlassCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
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
    onAppClick: (AppModel) -> Unit,
    onAppLongClick: (AppModel) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AkoCulturalBackground(
            color = MaterialTheme.colorScheme.secondary
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // Header Bar with Glassmorphism Search & Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Glass Search Field Button
                GlassCard(
                    shape = RoundedCornerShape(22.dp),
                    elevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(onClick = onOpenSearch)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Rechercher",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Cherche… / Dó…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                GlassCard(
                    shape = CircleShape,
                    elevation = 2.dp,
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Paramètres",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Glass Main Clock Header Card
            GlassCard(
                shape = RoundedCornerShape(28.dp),
                elevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 24.dp)
                ) {
                    Text(
                        text = greetingMessage,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentTime,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = currentDate,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Apps Scrollable Container with Alphabet Bar
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Workspace Section
                    item(key = "workspace") {
                        val parsedItems = remember(workspaceItems) {
                            workspaceItems.mapNotNull { WorkspaceItem.fromPrefString(it) }
                        }
                        val pagerState = rememberPagerState(pageCount = { 3 })
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(360.dp) // Height for a 4x4 grid
                            ) { page ->
                                Column(
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                                ) {
                                    for (row in 0..3) { // 4 rows
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            modifier = Modifier.fillMaxWidth().weight(1f)
                                        ) {
                                            for (col in 0..3) { // 4 columns
                                                val item = parsedItems.find { it.page == page && it.row == row && it.col == col }
                                                
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.weight(1f).fillMaxHeight()
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
                                                                        modifier = Modifier.fillMaxSize().padding(8.dp)
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
                                                                        size = (iconSizeDp).dp,
                                                                        shape = com.example.ui.theme.getShapeFor(iconShape)
                                                                    )
                                                                    Spacer(modifier = Modifier.height(4.dp))
                                                                    Text(
                                                                        text = appModel.label,
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
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
                            
                            // Pager Indicators
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(pagerState.pageCount) { iteration ->
                                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    Box(
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .size(8.dp)
                                    )
                                }
                            }
                            
                            // Add Widget Button
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                GlassCard(
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = 2.dp,
                                    modifier = Modifier
                                        .combinedClickable(onClick = { onAddWorkspaceItem("", true) })
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "Ajouter un widget",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Ajouter un widget",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Favorites Section
                    if (favorites.isNotEmpty()) {
                        item(key = "favorites_header") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Applications favorites",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(favorites, key = { "fav_${it.packageName}" }) { app ->
                                        GlassCard(
                                            shape = RoundedCornerShape(22.dp),
                                            elevation = 2.dp,
                                            modifier = Modifier.width((iconSizeDp + 28).dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .combinedClickable(
                                                        onClick = { onAppClick(app) },
                                                        onLongClick = { onAppLongClick(app) }
                                                    )
                                                    .padding(vertical = 12.dp, horizontal = 6.dp)
                                            ) {
                                                AppIconView(
                                                    packageName = app.packageName,
                                                    drawable = app.iconDrawable,
                                                    size = (iconSizeDp + 10).dp,
                                                    shape = com.example.ui.theme.getShapeFor(iconShape)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = app.label,
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        fontWeight = FontWeight.Medium
                                                    ),
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

                    // All Apps Grouped by First Letter
                    groupedApps.forEach { (letter, appsInGroup) ->
                        item(key = "header_$letter") {
                            Text(
                                text = letter.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .padding(top = 16.dp, bottom = 8.dp)
                            )
                        }

                        items(
                            items = appsInGroup,
                            key = { it.packageName }
                        ) { app ->
                            GlassCard(
                                shape = RoundedCornerShape(20.dp),
                                elevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { onAppClick(app) },
                                            onLongClick = { onAppLongClick(app) }
                                        )
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    AppIconView(
                                        packageName = app.packageName,
                                        drawable = app.iconDrawable,
                                        size = iconSizeDp.dp,
                                        shape = com.example.ui.theme.getShapeFor(iconShape)
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Alphabet Index Sidebar on the right
                AlphabetIndexBar(
                    alphabet = alphabetIndex,
                    onLetterSelected = { selectedLetter ->
                        scope.launch {
                            val targetIndex = calculateSectionIndex(
                                hasFavorites = favorites.isNotEmpty(),
                                groupedApps = groupedApps,
                                targetLetter = selectedLetter
                            )
                            if (targetIndex >= 0) {
                                listState.animateScrollToItem(targetIndex)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                )
            }
        }
    }
}

private fun calculateSectionIndex(
    hasFavorites: Boolean,
    groupedApps: Map<Char, List<AppModel>>,
    targetLetter: Char
): Int {
    var index = 1 // workspace is always there
    if (hasFavorites) index += 1
    
    for ((letter, apps) in groupedApps) {
        if (letter == targetLetter) return index
        index += 1 + apps.size
    }
    return -1
}
