package com.example.ui.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppModel
import com.example.ui.components.AkoCulturalBackground
import com.example.ui.components.AlphabetIndexBar
import com.example.ui.components.AppIconView
import com.example.ui.components.GlassCard
import kotlinx.coroutines.launch

enum class DrawerCategory(val label: String) {
    ALL("Toutes"),
    SOCIAL("Social"),
    MEDIA("Médias & Jeux"),
    PRODUCTIVITY("Travail & Outils"),
    SYSTEM("Système")
}

private fun isAppInCategory(app: AppModel, category: DrawerCategory): Boolean {
    val pkg = app.packageName.lowercase()
    val name = app.label.lowercase()
    return when (category) {
        DrawerCategory.ALL -> true
        DrawerCategory.SOCIAL -> {
            pkg.contains("message") || pkg.contains("dialer") || pkg.contains("contact") ||
            pkg.contains("phone") || pkg.contains("whatsapp") || pkg.contains("telegram") ||
            pkg.contains("messenger") || pkg.contains("facebook") || pkg.contains("twitter") ||
            pkg.contains("instagram") || pkg.contains("social") || name.contains("message") ||
            name.contains("contact") || name.contains("téléphone") || name.contains("appel")
        }
        DrawerCategory.MEDIA -> {
            pkg.contains("camera") || pkg.contains("photo") || pkg.contains("gallery") ||
            pkg.contains("youtube") || pkg.contains("music") || pkg.contains("spotify") ||
            pkg.contains("video") || pkg.contains("sound") || pkg.contains("game") ||
            pkg.contains("play") || name.contains("photo") || name.contains("caméra") ||
            name.contains("galerie") || name.contains("musique") || name.contains("jeu")
        }
        DrawerCategory.PRODUCTIVITY -> {
            pkg.contains("mail") || pkg.contains("gmail") || pkg.contains("chrome") ||
            pkg.contains("browser") || pkg.contains("clock") || pkg.contains("calendar") ||
            pkg.contains("calculator") || pkg.contains("calc") || pkg.contains("deskclock") ||
            pkg.contains("notes") || pkg.contains("drive") || pkg.contains("doc") ||
            pkg.contains("sheet") || name.contains("horloge") || name.contains("calcul") ||
            name.contains("calendrier") || name.contains("navigateur") || name.contains("notes")
        }
        DrawerCategory.SYSTEM -> {
            pkg.contains("settings") || pkg.contains("parametre") || pkg.contains("file") ||
            pkg.contains("documentsui") || pkg.contains("vending") || pkg.contains("packageinstaller") ||
            pkg.contains("system") || pkg.contains("launcher") || name.contains("paramètre") ||
            name.contains("fichier") || name.contains("système") || name.contains("téléchargement")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredApps: List<AppModel>,
    iconSizeDp: Int,
    iconShape: com.example.ui.theme.AkoIconShape,
    onBack: () -> Unit,
    onAppClick: (AppModel) -> Unit,
    onAppLongClick: (AppModel) -> Unit,
    modifier: Modifier = Modifier,
    autoOpenKeyboard: Boolean = false,
    themedIcons: Boolean = false
) {
    var isGridView by rememberSaveable { mutableStateOf(false) }
    var selectedCategory by rememberSaveable { mutableStateOf(DrawerCategory.ALL) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (autoOpenKeyboard) {
            focusRequester.requestFocus()
        }
    }

    // Filter apps according to the active category
    val categoryFilteredApps = remember(filteredApps, selectedCategory) {
        if (selectedCategory == DrawerCategory.ALL) {
            filteredApps
        } else {
            filteredApps.filter { isAppInCategory(it, selectedCategory) }
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
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Glass Search Bar Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                GlassCard(
                    shape = CircleShape,
                    elevation = 2.dp,
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Cherche… / Dó…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Effacer",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                )
            }

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                items(DrawerCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                            .combinedClickable(
                                onClick = { selectedCategory = category }
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = category.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Quick Access / Suggested Apps (visible when not searching and on ALL category)
            if (searchQuery.isEmpty() && selectedCategory == DrawerCategory.ALL && filteredApps.isNotEmpty()) {
                val suggestedApps = remember(filteredApps) { filteredApps.take(5) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Accès Rapide",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(
                            items = suggestedApps,
                            key = { "quick_${it.packageName}" }
                        ) { app ->
                            GlassCard(
                                shape = RoundedCornerShape(16.dp),
                                elevation = 2.dp,
                                modifier = Modifier.width(72.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .combinedClickable(
                                            onClick = { onAppClick(app) },
                                            onLongClick = { onAppLongClick(app) }
                                        )
                                        .padding(vertical = 8.dp, horizontal = 4.dp)
                                ) {
                                    AppIconView(
                                        packageName = app.packageName,
                                        drawable = app.iconDrawable,
                                        size = 42.dp,
                                        shape = com.example.ui.theme.getShapeFor(iconShape),
                                        isThemed = themedIcons
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
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

            // Results Counter and Switch Style Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 4.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "${categoryFilteredApps.size} applications",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                GlassCard(
                    shape = CircleShape,
                    elevation = 1.dp,
                    modifier = Modifier.size(36.dp)
                ) {
                    IconButton(
                        onClick = { isGridView = !isGridView },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.Rounded.List else Icons.Rounded.GridView,
                            contentDescription = if (isGridView) "Passer en vue liste" else "Passer en vue grille",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // App List / Grid with Glass Cards and Sections
            if (categoryFilteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune application dans cette catégorie",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Group apps alphabetically by their initial letter
                val groupedApps = remember(categoryFilteredApps) {
                    categoryFilteredApps.groupBy { app ->
                        val firstChar = app.label.trim().firstOrNull()?.uppercaseChar() ?: '#'
                        if (firstChar.isLetter()) firstChar else '#'
                    }.toSortedMap(compareBy { if (it == '#') Char.MAX_VALUE else it })
                }

                val alphabetIndex = remember(groupedApps) {
                    groupedApps.keys.toList()
                }

                // Map of letter to its scroll position
                val listLetterIndices = remember(groupedApps) {
                    val map = mutableMapOf<Char, Int>()
                    var currentIndex = 0
                    groupedApps.forEach { (letter, apps) ->
                        map[letter] = currentIndex
                        currentIndex += 1 + apps.size // 1 for header + apps
                    }
                    map
                }

                val gridLetterIndices = remember(groupedApps) {
                    val map = mutableMapOf<Char, Int>()
                    var currentIndex = 0
                    groupedApps.forEach { (letter, apps) ->
                        map[letter] = currentIndex
                        currentIndex += 1 + apps.size // 1 for header span + apps
                    }
                    map
                }

                val listState = rememberLazyListState()
                val gridState = rememberLazyGridState()
                val coroutineScope = rememberCoroutineScope()

                // Calculate currently visible letter dynamically based on scroll position
                val currentVisibleLetter by remember(isGridView, groupedApps) {
                    derivedStateOf {
                        if (isGridView) {
                            val firstVisible = gridState.firstVisibleItemIndex
                            var matched = alphabetIndex.firstOrNull()
                            for ((letter, index) in gridLetterIndices) {
                                if (firstVisible >= index) {
                                    matched = letter
                                } else {
                                    break
                                }
                            }
                            matched
                        } else {
                            val firstVisible = listState.firstVisibleItemIndex
                            var matched = alphabetIndex.firstOrNull()
                            for ((letter, index) in listLetterIndices) {
                                if (firstVisible >= index) {
                                    matched = letter
                                } else {
                                    break
                                }
                            }
                            matched
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            state = gridState,
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp, start = 4.dp, end = 32.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            groupedApps.forEach { (letter, appsInGroup) ->
                                item(
                                    span = { GridItemSpan(maxLineSpan) },
                                    key = "header_$letter"
                                ) {
                                    AlphabetSectionHeader(
                                        letter = letter,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                    )
                                }

                                items(
                                    items = appsInGroup,
                                    key = { it.packageName }
                                ) { app ->
                                    GlassCard(
                                        shape = RoundedCornerShape(18.dp),
                                        elevation = 2.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = { onAppClick(app) },
                                                    onLongClick = { onAppLongClick(app) }
                                                )
                                                .padding(vertical = 12.dp, horizontal = 4.dp)
                                        ) {
                                            AppIconView(
                                                packageName = app.packageName,
                                                drawable = app.iconDrawable,
                                                size = iconSizeDp.dp,
                                                shape = com.example.ui.theme.getShapeFor(iconShape),
                                                isThemed = themedIcons
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            HighlightedDrawerText(
                                                text = app.label,
                                                query = searchQuery,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp, start = 8.dp, end = 36.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            groupedApps.forEach { (letter, appsInGroup) ->
                                stickyHeader(key = "header_$letter") {
                                    AlphabetSectionHeader(
                                        letter = letter,
                                        isSticky = true
                                    )
                                }

                                items(
                                    items = appsInGroup,
                                    key = { it.packageName }
                                ) { app ->
                                    GlassCard(
                                        shape = RoundedCornerShape(20.dp),
                                        elevation = 2.dp,
                                        modifier = Modifier.fillMaxWidth()
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
                                                shape = com.example.ui.theme.getShapeFor(iconShape),
                                                isThemed = themedIcons
                                            )

                                            Spacer(modifier = Modifier.width(16.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                HighlightedDrawerText(
                                                    text = app.label,
                                                    query = searchQuery,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = app.packageName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Floating Current Letter Indicator (Indicator of the current section)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = currentVisibleLetter != null,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 8.dp, bottom = 12.dp)
                    ) {
                        currentVisibleLetter?.let { letter ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .shadow(6.dp, RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = letter.toString(),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Section $letter",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Alphabet Index Sidebar on the right with current letter feedback
                    if (alphabetIndex.isNotEmpty()) {
                        AlphabetIndexBar(
                            alphabet = alphabetIndex,
                            currentLetter = currentVisibleLetter,
                            onLetterSelected = { selectedLetter ->
                                if (isGridView) {
                                    val targetIndex = gridLetterIndices[selectedLetter] ?: 0
                                    coroutineScope.launch {
                                        gridState.animateScrollToItem(targetIndex)
                                    }
                                } else {
                                    val targetIndex = listLetterIndices[selectedLetter] ?: 0
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(targetIndex)
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp)
                                .fillMaxHeight(0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightedDrawerText(
    text: String,
    query: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    highlightColor: Color = MaterialTheme.colorScheme.secondary,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    textAlign: TextAlign? = null,
    modifier: Modifier = Modifier
) {
    val trimmed = query.trim()
    if (trimmed.isEmpty() || !text.contains(trimmed, ignoreCase = true)) {
        Text(
            text = text,
            style = style,
            color = color,
            maxLines = maxLines,
            overflow = overflow,
            textAlign = textAlign,
            modifier = modifier
        )
    } else {
        val annotated = remember(text, trimmed, color, highlightColor) {
            buildAnnotatedString {
                var startIndex = 0
                val lowerText = text.lowercase()
                val lowerQuery = trimmed.lowercase()

                while (startIndex < text.length) {
                    val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
                    if (matchIndex == -1) {
                        append(text.substring(startIndex))
                        break
                    } else {
                        if (matchIndex > startIndex) {
                            append(text.substring(startIndex, matchIndex))
                        }
                        val matchEnd = matchIndex + lowerQuery.length
                        withStyle(
                            SpanStyle(
                                color = highlightColor,
                                fontWeight = FontWeight.ExtraBold
                            )
                        ) {
                            append(text.substring(matchIndex, matchEnd))
                        }
                        startIndex = matchEnd
                    }
                }
            }
        }
        Text(
            text = annotated,
            style = style,
            color = color,
            maxLines = maxLines,
            overflow = overflow,
            textAlign = textAlign,
            modifier = modifier
        )
    }
}

@Composable
fun AlphabetSectionHeader(
    letter: Char,
    isSticky: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSticky) {
                    MaterialTheme.colorScheme.background.copy(alpha = 0.92f)
                } else {
                    Color.Transparent
                }
            )
            .padding(vertical = if (isSticky) 6.dp else 8.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
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
                text = letter.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.5.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        )
    }
}
