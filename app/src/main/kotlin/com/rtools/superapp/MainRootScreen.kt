package com.rtools.superapp

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.rtools.superapp.ui.theme.ComposeEmptyActivityTheme

@Composable
fun MainRootScreen(packageManager: PackageManager) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.APPS) }
    var keyword by rememberSaveable { mutableStateOf("") }
    var showSystemApps by rememberSaveable {
        mutableStateOf(prefs.getBoolean("apps_show_system", false))
    }
    var sortModeName by rememberSaveable {
        mutableStateOf(prefs.getString("apps_sort_mode", SortMode.INSTALL_TIME.name) ?: SortMode.INSTALL_TIME.name)
    }
    var descending by rememberSaveable {
        mutableStateOf(prefs.getBoolean("apps_descending", true))
    }
    var themeModeName by rememberSaveable {
        mutableStateOf(prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
    }
    var floatingBottomBar by rememberSaveable {
        mutableStateOf(prefs.getBoolean("floating_bottom_bar", false))
    }
    var uiScalePercent by rememberSaveable {
        mutableStateOf(prefs.getInt("ui_scale_percent", 100).coerceIn(80, 110))
    }
    var languageName by rememberSaveable {
        mutableStateOf(prefs.getString("app_language", AppLanguage.SYSTEM.name) ?: AppLanguage.SYSTEM.name)
    }

    val sortMode = SortMode.entries.firstOrNull { it.name == sortModeName } ?: SortMode.INSTALL_TIME
    val themeMode = AppThemeMode.entries.firstOrNull { it.name == themeModeName } ?: AppThemeMode.SYSTEM
    val currentLanguage = AppLanguage.entries.firstOrNull { it.name == languageName } ?: AppLanguage.SYSTEM
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    fun saveShowSystemApps(value: Boolean) {
        showSystemApps = value
        prefs.edit().putBoolean("apps_show_system", value).apply()
    }

    fun saveSortMode(value: SortMode) {
        sortModeName = value.name
        prefs.edit().putString("apps_sort_mode", value.name).apply()
    }

    fun saveDescending(value: Boolean) {
        descending = value
        prefs.edit().putBoolean("apps_descending", value).apply()
    }

    fun saveThemeMode(value: AppThemeMode) {
        themeModeName = value.name
        prefs.edit().putString("theme_mode", value.name).apply()
    }

    fun saveFloatingBottomBar(value: Boolean) {
        floatingBottomBar = value
        prefs.edit().putBoolean("floating_bottom_bar", value).apply()
    }

    fun saveUiScalePercent(value: Int) {
        val safeValue = value.coerceIn(80, 110)
        uiScalePercent = safeValue
        prefs.edit().putInt("ui_scale_percent", safeValue).apply()
    }

    fun saveLanguage(value: AppLanguage) {
        languageName = value.name
        prefs.edit().putString("app_language", value.name).apply()
    }

    val baseDensity = LocalDensity.current
    val scaleFactor = uiScalePercent.coerceIn(80, 110) / 100f

    ComposeEmptyActivityTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = baseDensity.density * scaleFactor,
                fontScale = baseDensity.fontScale * scaleFactor,
            ),
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    val listBottomPadding = 112.dp

                    when (selectedTab) {
                        MainTab.APPS -> AppManagerScreen(
                            packageManager = packageManager,
                            currentLanguage = currentLanguage,
                            keyword = keyword,
                            onKeywordChange = { keyword = it },
                            showSystemApps = showSystemApps,
                            onShowSystemAppsChange = ::saveShowSystemApps,
                            sortMode = sortMode,
                            onSortModeChange = ::saveSortMode,
                            descending = descending,
                            onDescendingChange = ::saveDescending,
                            bottomPadding = listBottomPadding,
                        )

                        MainTab.MODULES -> ModuleScreen(
                            currentLanguage = currentLanguage,
                            bottomPadding = listBottomPadding,
                        )

                        MainTab.SETTINGS -> SettingsScreen(
                            themeMode = themeMode,
                            onThemeModeChange = ::saveThemeMode,
                            currentLanguage = currentLanguage,
                            onLanguageChange = ::saveLanguage,
                            floatingBottomBar = floatingBottomBar,
                            onFloatingBottomBarChange = ::saveFloatingBottomBar,
                            uiScalePercent = uiScalePercent,
                            onUiScaleChange = ::saveUiScalePercent,
                            bottomPadding = listBottomPadding,
                        )
                    }

                    if (floatingBottomBar) {
                        FloatingBottomBar(
                            selectedTab = selectedTab,
                            onTabSelected = { tab -> selectedTab = tab },
                            currentLanguage = currentLanguage,
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    } else {
                        DefaultBottomBar(
                            selectedTab = selectedTab,
                            onTabSelected = { tab -> selectedTab = tab },
                            currentLanguage = currentLanguage,
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }
            }
        }
    }
}
