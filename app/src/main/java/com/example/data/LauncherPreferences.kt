package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AkoIconShape
import com.example.ui.theme.AkoThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ako_launcher_prefs")

data class AkoUserPreferences(
    val themeMode: AkoThemeMode = AkoThemeMode.COTONOU,
    val iconShape: AkoIconShape = AkoIconShape.SQUIRCLE,
    val iconSizeDp: Int = 56,
    val favoritePackages: Set<String> = emptySet(),
    val hiddenPackages: Set<String> = emptySet(),
    val widgets: Set<String> = emptySet(),
    val workspaceItems: Set<String> = emptySet(),
    val enableLockScreen: Boolean = true,
    val isDarkModeEnabled: Boolean = false,
    val hiddenAppsPassword: String = "1234"
)

class LauncherPreferences(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ICON_SHAPE = stringPreferencesKey("icon_shape")
        val ICON_SIZE = intPreferencesKey("icon_size_dp")
        val FAVORITES = stringSetPreferencesKey("favorite_packages")
        val HIDDEN = stringSetPreferencesKey("hidden_packages")
        val WIDGETS = stringSetPreferencesKey("home_widgets")
        val WORKSPACE_ITEMS = stringSetPreferencesKey("workspace_items")
        val LOCK_SCREEN = booleanPreferencesKey("enable_lock_screen")
        val DARK_MODE = booleanPreferencesKey("enable_dark_mode")
        val HIDDEN_PASSWORD = stringPreferencesKey("hidden_apps_password")
    }

    val preferencesFlow: Flow<AkoUserPreferences> = context.dataStore.data.map { prefs ->
        val themeName = prefs[Keys.THEME_MODE] ?: AkoThemeMode.COTONOU.name
        val themeMode = try {
            AkoThemeMode.valueOf(themeName)
        } catch (e: Exception) {
            AkoThemeMode.COTONOU
        }
        
        val shapeName = prefs[Keys.ICON_SHAPE] ?: AkoIconShape.SQUIRCLE.name
        val iconShape = try {
            AkoIconShape.valueOf(shapeName)
        } catch (e: Exception) {
            AkoIconShape.SQUIRCLE
        }
        
        val iconSize = prefs[Keys.ICON_SIZE] ?: 56
        val favorites = prefs[Keys.FAVORITES] ?: emptySet()
        val hidden = prefs[Keys.HIDDEN] ?: emptySet()
        val widgets = prefs[Keys.WIDGETS] ?: emptySet()
        val workspaceItems = prefs[Keys.WORKSPACE_ITEMS] ?: emptySet()
        val lockScreen = prefs[Keys.LOCK_SCREEN] ?: true
        val darkMode = prefs[Keys.DARK_MODE] ?: false
        val hiddenPassword = prefs[Keys.HIDDEN_PASSWORD] ?: "1234"

        AkoUserPreferences(
            themeMode = themeMode,
            iconShape = iconShape,
            iconSizeDp = iconSize,
            favoritePackages = favorites,
            hiddenPackages = hidden,
            widgets = widgets,
            workspaceItems = workspaceItems,
            enableLockScreen = lockScreen,
            isDarkModeEnabled = darkMode,
            hiddenAppsPassword = hiddenPassword
        )
    }

    suspend fun addWidget(widgetId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.WIDGETS] ?: emptySet()
            prefs[Keys.WIDGETS] = current + widgetId.toString()
        }
    }

    suspend fun removeWidget(widgetId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.WIDGETS] ?: emptySet()
            prefs[Keys.WIDGETS] = current - widgetId.toString()
        }
    }

    suspend fun addWorkspaceItem(item: WorkspaceItem) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.WORKSPACE_ITEMS] ?: emptySet()
            // Remove any existing item at the same page/row/col
            val prefix = "${item.page}:${item.row}:${item.col}:"
            val filtered = current.filterNot { it.startsWith(prefix) }.toSet()
            prefs[Keys.WORKSPACE_ITEMS] = filtered + item.toPrefString()
        }
    }

    suspend fun removeWorkspaceItem(page: Int, row: Int, col: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.WORKSPACE_ITEMS] ?: emptySet()
            val prefix = "$page:$row:$col:"
            prefs[Keys.WORKSPACE_ITEMS] = current.filterNot { it.startsWith(prefix) }.toSet()
        }
    }

    suspend fun setIconShape(shape: AkoIconShape) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ICON_SHAPE] = shape.name
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }

    suspend fun setThemeMode(mode: AkoThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setIconSize(sizeDp: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ICON_SIZE] = sizeDp
        }
    }

    suspend fun toggleFavorite(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES] ?: emptySet()
            if (current.contains(packageName)) {
                prefs[Keys.FAVORITES] = current - packageName
            } else {
                prefs[Keys.FAVORITES] = current + packageName
            }
        }
    }

    suspend fun setFavorites(packages: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FAVORITES] = packages
        }
    }

    suspend fun toggleHidden(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.HIDDEN] ?: emptySet()
            if (current.contains(packageName)) {
                prefs[Keys.HIDDEN] = current - packageName
            } else {
                prefs[Keys.HIDDEN] = current + packageName
            }
        }
    }

    suspend fun setHiddenAppsPassword(password: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HIDDEN_PASSWORD] = password
        }
    }

    suspend fun setEnableLockScreen(enable: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LOCK_SCREEN] = enable
        }
    }
}
