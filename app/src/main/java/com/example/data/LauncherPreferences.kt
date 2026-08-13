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
    val enableLockScreen: Boolean = true,
    val isDarkModeEnabled: Boolean = false
)

class LauncherPreferences(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ICON_SHAPE = stringPreferencesKey("icon_shape")
        val ICON_SIZE = intPreferencesKey("icon_size_dp")
        val FAVORITES = stringSetPreferencesKey("favorite_packages")
        val HIDDEN = stringSetPreferencesKey("hidden_packages")
        val LOCK_SCREEN = booleanPreferencesKey("enable_lock_screen")
        val DARK_MODE = booleanPreferencesKey("enable_dark_mode")
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
        val lockScreen = prefs[Keys.LOCK_SCREEN] ?: true
        val darkMode = prefs[Keys.DARK_MODE] ?: false

        AkoUserPreferences(
            themeMode = themeMode,
            iconShape = iconShape,
            iconSizeDp = iconSize,
            favoritePackages = favorites,
            hiddenPackages = hidden,
            enableLockScreen = lockScreen,
            isDarkModeEnabled = darkMode
        )
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

    suspend fun setEnableLockScreen(enable: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LOCK_SCREEN] = enable
        }
    }
}
