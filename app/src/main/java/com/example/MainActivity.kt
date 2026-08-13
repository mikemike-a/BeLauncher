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

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    private lateinit var preferences: LauncherPreferences
    private lateinit var repository: AppRepository
    private val viewModel: LauncherViewModel by viewModels {
        LauncherViewModel.Factory(repository, preferences)
    }

    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var appWidgetHost: AppWidgetHost

    companion object {
        private const val APPWIDGET_HOST_ID = 1024
    }

    private val pickWidgetLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            if (appWidgetId != -1) {
                configureWidget(appWidgetId)
            }
        } else {
            val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != null && appWidgetId != -1) {
                appWidgetHost.deleteAppWidgetId(appWidgetId)
            }
        }
    }

    private val configureWidgetLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
            if (appWidgetId != -1) {
                viewModel.addWidgetToWorkspace(appWidgetId)
            }
        } else {
            val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != null && appWidgetId != -1) {
                appWidgetHost.deleteAppWidgetId(appWidgetId)
            }
        }
    }

    fun selectWidget() {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        pickWidgetLauncher.launch(pickIntent)
    }

    private fun configureWidget(appWidgetId: Int) {
        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
        if (appWidgetInfo?.configure != null) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = appWidgetInfo.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            configureWidgetLauncher.launch(intent)
        } else {
            viewModel.addWidgetToWorkspace(appWidgetId)
        }
    }

    private val defaultLauncherLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        // No specific action needed after user sets (or declines to set) default
    }

    private fun requestDefaultLauncherRole() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(android.app.role.RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME) && !roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)) {
                val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME)
                defaultLauncherLauncher.launch(intent)
            }
        }
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
        
        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, APPWIDGET_HOST_ID)
        appWidgetHost.startListening()

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

            androidx.compose.runtime.LaunchedEffect(Unit) {
                requestDefaultLauncherRole()
            }

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
                                    allApps = uiState.allApps,
                                    workspaceItems = uiState.preferences.workspaceItems,
                                    appWidgetHost = appWidgetHost,
                                    appWidgetManager = appWidgetManager,
                                    onAddWorkspaceItem = { _, isWidget ->
                                        if (isWidget) {
                                            selectWidget()
                                        }
                                    },
                                    onRemoveWorkspaceItem = { page, row, col -> 
                                        val item = uiState.preferences.workspaceItems.mapNotNull { 
                                            com.example.data.WorkspaceItem.fromPrefString(it)
                                        }.find { it.page == page && it.row == row && it.col == col }
                                        
                                        if (item?.isWidget == true) {
                                            val widgetId = item.identifier.toIntOrNull()
                                            if (widgetId != null) {
                                                appWidgetHost.deleteAppWidgetId(widgetId)
                                            }
                                        }
                                        viewModel.removeWorkspaceItem(page, row, col)
                                    },
                                    groupedApps = uiState.groupedApps,
                                    alphabetIndex = uiState.alphabetIndex,
                                    iconSizeDp = uiState.preferences.iconSizeDp,
                                    iconShape = uiState.preferences.iconShape,
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
                                    iconShape = uiState.preferences.iconShape,
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
                                    onIconShapeSelected = { shape -> viewModel.setIconShape(shape) },
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
                            isInWorkspace = uiState.preferences.workspaceItems.any { it.endsWith(app.packageName) },
                            iconShape = uiState.preferences.iconShape,
                            onDismiss = { viewModel.dismissQuickActions() },
                            onLaunch = { viewModel.launchApp(this@MainActivity, app) },
                            onAddToWorkspace = { viewModel.addAppToWorkspace(app) },
                            onRemoveFromWorkspace = { viewModel.removeAppFromWorkspace(app) },
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
            appWidgetHost.stopListening()
            unregisterReceiver(packageChangeReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }
}
