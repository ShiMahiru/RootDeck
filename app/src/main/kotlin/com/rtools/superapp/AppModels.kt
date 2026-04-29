package com.rtools.superapp

import android.graphics.drawable.Drawable

data class InstalledAppItem(
    val appName: String,
    val packageName: String,
    val icon: Drawable?,
    val isSystem: Boolean,
    val sizeBytes: Long,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
)

enum class SortMode(val label: String) {
    APP_NAME("应用名称"),
    PACKAGE_SIZE("安装包大小"),
    INSTALL_TIME("安装时间"),
}

enum class RootGrantState {
    UNKNOWN,
    GRANTED,
    DENIED,
    UNAVAILABLE,
}

enum class MainTab {
    APPS,
    MODULES,
    SETTINGS,
}

enum class AppThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色"),
}
