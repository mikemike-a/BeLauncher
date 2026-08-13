package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppRepository
import com.example.data.LauncherPreferences
import com.example.ui.drawer.AppDrawerScreen
import com.example.ui.home.HomeScreen
import com.example.ui.lock.LockScreen
import com.example.ui.popup.QuickActionsPopup
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.AkoTheme
import com.example.viewmodel.LauncherScreen
import com.example.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {

    private lateinit var preferences: LauncherPreferences
    private lateinit var repository: AppRepository
    private val viewModel: LauncherViewModel by viewModels {
        LauncherViewModel.Factory(repository, preferences)
    }

    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.reloadApps()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = LauncherPreferences(applicationContext)
        repository = AppRepository(applicationContext, preferences)

        // Register package change receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        registerReceiver(packageChangeReceiver, filter)

        // Handle System Back Button appropriately for Launcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val state = viewModel.uiState.value
                if (state.quickActionApp != null) {
                    viewModel.dismissQuickActions()
                } else if (state.currentScreen != LauncherScreen.HOME && state.currentScreen != LauncherScreen.LOCK) {
                    viewModel.navigateTo(LauncherScreen.HOME)
                } else {
                    // Do nothing on Home/Lock screen to prevent closing launcher
                }
            }
        })

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            AkoTheme(
                themeMode = uiState.preferences.themeMode,
                darkTheme = uiState.preferences.isDarkModeEnabled
            ) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) { innerPadding ->
                    Crossfade(
                        targetState = uiState.currentScreen,
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        label = "ScreenTransition",
                        modifier = Modifier.padding(innerPadding)
                    ) { screen ->
                        when (screen) {
                            LauncherScreen.LOCK -> {
                                LockScreen(
                                    onUnlock = { viewModel.navigateTo(LauncherScreen.HOME) }
                                )
                            }
                            LauncherScreen.HOME -> {
                                HomeScreen(
                                    greetingMessage = uiState.greetingMessage,
                                    favorites = uiState.favoriteApps,
                                    groupedApps = uiState.groupedApps,
                                    alphabetIndex = uiState.alphabetIndex,
                                    iconSizeDp = uiState.preferences.iconSizeDp,
                                    onAppClick = { app -> viewModel.launchApp(this@MainActivity, app) },
                                    onAppLongClick = { app -> viewModel.showQuickActions(app) },
                                    onOpenSearch = { viewModel.navigateTo(LauncherScreen.DRAWER) },
                                    onOpenSettings = { viewModel.navigateTo(LauncherScreen.SETTINGS) }
                                )
                            }
                            LauncherScreen.DRAWER -> {
                                AppDrawerScreen(
                                    searchQuery = uiState.searchQuery,
                                    onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                                    filteredApps = uiState.searchFilteredApps,
                                    iconSizeDp = uiState.preferences.iconSizeDp,
                                    onBack = { viewModel.navigateTo(LauncherScreen.HOME) },
                                    onAppClick = { app -> viewModel.launchApp(this@MainActivity, app) },
                                    onAppLongClick = { app -> viewModel.showQuickActions(app) }
                                )
                            }
                            LauncherScreen.SETTINGS -> {
                                SettingsScreen(
                                    preferences = uiState.preferences,
                                    onThemeSelected = { mode -> viewModel.setThemeMode(mode) },
                                    onIconSizeSelected = { size -> viewModel.setIconSize(size) },
                                    onLockScreenToggle = { enabled -> viewModel.setLockScreenEnabled(enabled) },
                                    onDarkModeToggle = { enabled -> viewModel.setDarkModeEnabled(enabled) },
                                    onBack = { viewModel.navigateTo(LauncherScreen.HOME) }
                                )
                            }
                        }
                    }

                    // Quick Actions Bottom Sheet Popup
                    uiState.quickActionApp?.let { app ->
                        QuickActionsPopup(
                            app = app,
                            onDismiss = { viewModel.dismissQuickActions() },
                            onLaunch = { viewModel.launchApp(this@MainActivity, app) },
                            onToggleFavorite = { viewModel.toggleFavorite(app) },
                            onToggleHide = { viewModel.toggleHideApp(app) },
                            onAppInfo = { viewModel.openAppDetails(this@MainActivity, app) },
                            onUninstall = { viewModel.uninstallApp(this@MainActivity, app) },
                            sheetState = sheetState
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(packageChangeReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }
}
