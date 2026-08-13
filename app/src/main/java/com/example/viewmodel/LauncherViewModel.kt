package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AkoUserPreferences
import com.example.data.AppModel
import com.example.data.AppRepository
import com.example.data.LauncherPreferences
import com.example.ui.theme.AkoThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class LauncherScreen {
    LOCK,
    HOME,
    DRAWER,
    SETTINGS
}

data class LauncherUiState(
    val currentScreen: LauncherScreen = LauncherScreen.HOME,
    val preferences: AkoUserPreferences = AkoUserPreferences(),
    val allApps: List<AppModel> = emptyList(),
    val visibleApps: List<AppModel> = emptyList(),
    val favoriteApps: List<AppModel> = emptyList(),
    val groupedApps: Map<Char, List<AppModel>> = emptyMap(),
    val alphabetIndex: List<Char> = emptyList(),
    val searchQuery: String = "",
    val searchFilteredApps: List<AppModel> = emptyList(),
    val quickActionApp: AppModel? = null,
    val greetingMessage: String = "Mì kwábɔ̀"
)

class LauncherViewModel(
    private val repository: AppRepository,
    private val preferences: LauncherPreferences
) : ViewModel() {

    private val currentScreenState = MutableStateFlow(LauncherScreen.HOME)
    private val searchQueryState = MutableStateFlow("")
    private val quickActionAppState = MutableStateFlow<AppModel?>(null)

    val uiState: StateFlow<LauncherUiState> = combine(
        repository.appsFlow,
        preferences.preferencesFlow,
        currentScreenState,
        searchQueryState,
        quickActionAppState
    ) { apps, prefs, screen, query, quickApp ->

        val visible = apps.filter { !it.isHidden }
        val favorites = visible.filter { it.isFavorite }

        val grouped = visible.groupBy { app ->
            val first = app.label.trim().firstOrNull()?.uppercaseChar() ?: '#'
            if (first in 'A'..'Z') first else '#'
        }.toSortedMap()

        val alphabet = grouped.keys.toList()

        val filtered = if (query.isBlank()) {
            visible
        } else {
            visible.filter {
                it.label.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
            }
        }

        // Determine if lock screen should be shown by default when initialized
        val actualScreen = if (screen == LauncherScreen.LOCK && !prefs.enableLockScreen) {
            LauncherScreen.HOME
        } else {
            screen
        }

        LauncherUiState(
            currentScreen = actualScreen,
            preferences = prefs,
            allApps = apps,
            visibleApps = visible,
            favoriteApps = favorites,
            groupedApps = grouped,
            alphabetIndex = alphabet,
            searchQuery = query,
            searchFilteredApps = filtered,
            quickActionApp = quickApp,
            greetingMessage = calculateGreeting()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherUiState()
    )

    init {
        reloadApps()
    }

    fun reloadApps() {
        viewModelScope.launch {
            repository.reloadApps()
        }
    }

    fun navigateTo(screen: LauncherScreen) {
        currentScreenState.value = screen
    }

    fun setSearchQuery(query: String) {
        searchQueryState.value = query
    }

    fun showQuickActions(app: AppModel) {
        quickActionAppState.value = app
    }

    fun dismissQuickActions() {
        quickActionAppState.value = null
    }

    fun launchApp(context: Context, app: AppModel) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Impossible d'ouvrir ${app.label}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur lors du lancement: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleFavorite(app: AppModel) {
        viewModelScope.launch {
            preferences.toggleFavorite(app.packageName)
        }
    }

    fun toggleHideApp(app: AppModel) {
        viewModelScope.launch {
            preferences.toggleHidden(app.packageName)
        }
    }

    fun openAppDetails(context: Context, app: AppModel) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", app.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Impossible d'ouvrir les infos de l'application", Toast.LENGTH_SHORT).show()
        }
    }

    fun uninstallApp(context: Context, app: AppModel) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", app.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Impossible de désinstaller", Toast.LENGTH_SHORT).show()
        }
    }

    fun setThemeMode(mode: AkoThemeMode) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    fun setIconSize(sizeDp: Int) {
        viewModelScope.launch {
            preferences.setIconSize(sizeDp)
        }
    }

    fun setIconShape(shape: com.example.ui.theme.AkoIconShape) {
        viewModelScope.launch {
            preferences.setIconShape(shape)
        }
    }

    fun setLockScreenEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setEnableLockScreen(enabled)
        }
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setDarkMode(enabled)
        }
    }

    private fun calculateGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Bonjour / Mì kwábɔ̀"
            in 12..17 -> "Bon après-midi / Mì kwábɔ̀"
            else -> "Bonsoir / Mì kwábɔ̀"
        }
    }

    class Factory(
        private val repository: AppRepository,
        private val preferences: LauncherPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LauncherViewModel(repository, preferences) as T
        }
    }
}
