package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val preferences: LauncherPreferences
) {
    private val rawAppsFlow = MutableStateFlow<List<AppModel>>(emptyList())
    private val packageManager: PackageManager = context.packageManager

    suspend fun reloadApps() = withContext(Dispatchers.IO) {
        val loadedApps = mutableListOf<AppModel>()
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(mainIntent, 0)
        }

        // Exclude Akɔ̀ launcher itself from the app list
        val selfPackageName = context.packageName

        for (ri in resolveInfos) {
            val pkgName = ri.activityInfo.packageName
            if (pkgName == selfPackageName) continue

            val label = ri.loadLabel(packageManager).toString().ifBlank { pkgName }
            val icon = try {
                ri.loadIcon(packageManager)
            } catch (e: Exception) {
                null
            }

            loadedApps.add(
                AppModel(
                    label = label,
                    packageName = pkgName,
                    activityName = ri.activityInfo.name,
                    iconDrawable = icon
                )
            )
        }

        // Sort alphabetically, ignoring case
        loadedApps.sortBy { it.label.lowercase() }
        rawAppsFlow.value = loadedApps
    }

    val appsFlow: Flow<List<AppModel>> = combine(
        rawAppsFlow,
        preferences.preferencesFlow
    ) { rawApps, prefs ->
        rawApps.map { app ->
            app.copy(
                isFavorite = prefs.favoritePackages.contains(app.packageName),
                isHidden = prefs.hiddenPackages.contains(app.packageName)
            )
        }
    }
}
