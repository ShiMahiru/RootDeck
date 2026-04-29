package com.rtools.superapp

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "settings"
private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_APP_LANGUAGE = "app_language"
private const val KEY_APPS_SORT_MODE = "apps_sort_mode"
private const val KEY_APPS_DESCENDING = "apps_descending"
private const val KEY_APPS_SHOW_SYSTEM = "apps_show_system"
private const val KEY_FLOATING_BOTTOM_BAR = "floating_bottom_bar"
private const val KEY_SHOW_DISABLED = "show_disabled"
private const val KEY_ENABLE_WEB_DEBUGGING = "enable_web_debugging"

enum class AppLanguage(val label: String) {
    SYSTEM("跟随系统"),
    ZH_CN("简体中文"),
    ENGLISH("English"),
    JAPANESE("日本語"),
}

object UiPrefs {
    fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getThemeMode(context: Context): AppThemeMode {
        val saved = prefs(context).getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        return AppThemeMode.entries.firstOrNull { it.name == saved } ?: AppThemeMode.SYSTEM
    }

    fun setThemeMode(context: Context, value: AppThemeMode) {
        prefs(context).edit().putString(KEY_THEME_MODE, value.name).apply()
    }

    fun getLanguage(context: Context): AppLanguage {
        val saved = prefs(context).getString(KEY_APP_LANGUAGE, AppLanguage.SYSTEM.name)
        return AppLanguage.entries.firstOrNull { it.name == saved } ?: AppLanguage.SYSTEM
    }

    fun setLanguage(context: Context, value: AppLanguage) {
        prefs(context).edit().putString(KEY_APP_LANGUAGE, value.name).apply()
    }

    fun getSortMode(context: Context): SortMode {
        val saved = prefs(context).getString(KEY_APPS_SORT_MODE, SortMode.INSTALL_TIME.name)
        return SortMode.entries.firstOrNull { it.name == saved } ?: SortMode.INSTALL_TIME
    }

    fun setSortMode(context: Context, value: SortMode) {
        prefs(context).edit().putString(KEY_APPS_SORT_MODE, value.name).apply()
    }

    fun isDescending(context: Context): Boolean = prefs(context).getBoolean(KEY_APPS_DESCENDING, true)

    fun setDescending(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_APPS_DESCENDING, value).apply()
    }

    fun showSystemApps(context: Context): Boolean = prefs(context).getBoolean(KEY_APPS_SHOW_SYSTEM, false)

    fun setShowSystemApps(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_APPS_SHOW_SYSTEM, value).apply()
    }

    fun useFloatingBottomBar(context: Context): Boolean = prefs(context).getBoolean(KEY_FLOATING_BOTTOM_BAR, false)

    fun setFloatingBottomBar(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_FLOATING_BOTTOM_BAR, value).apply()
    }

    fun showDisabledModules(context: Context): Boolean = prefs(context).getBoolean(KEY_SHOW_DISABLED, false)

    fun setShowDisabledModules(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_SHOW_DISABLED, value).apply()
    }

    fun enableWebDebugging(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLE_WEB_DEBUGGING, false)

    fun setEnableWebDebugging(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLE_WEB_DEBUGGING, value).apply()
    }
}

object I18n {
    fun appTitle(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Super App List"
        AppLanguage.JAPANESE -> "スーパーアプリ一覧"
        else -> "超级应用列表"
    }

    fun moduleTitle(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Modules"
        AppLanguage.JAPANESE -> "モジュール"
        else -> "模块"
    }

    fun settingsTitle(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Settings"
        AppLanguage.JAPANESE -> "設定"
        else -> "设置"
    }

    fun appTab(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Apps"
        AppLanguage.JAPANESE -> "アプリ"
        else -> "应用"
    }

    fun moduleTab(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Modules"
        AppLanguage.JAPANESE -> "モジュール"
        else -> "模块"
    }

    fun settingsTab(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Settings"
        AppLanguage.JAPANESE -> "設定"
        else -> "设置"
    }

    fun search(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Search"
        AppLanguage.JAPANESE -> "検索"
        else -> "搜索"
    }

    fun clear(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Clear"
        AppLanguage.JAPANESE -> "クリア"
        else -> "清除"
    }

    fun sortMode(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Sort"
        AppLanguage.JAPANESE -> "並び替え"
        else -> "排序方式"
    }

    fun showSystemApps(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Show system apps"
        AppLanguage.JAPANESE -> "システムアプリを表示"
        else -> "显示系统应用"
    }

    fun descending(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Descending"
        AppLanguage.JAPANESE -> "降順"
        else -> "倒序排列"
    }

    fun sortLabel(mode: SortMode, language: AppLanguage): String = when (mode) {
        SortMode.APP_NAME -> when (language) {
            AppLanguage.ENGLISH -> "App name"
            AppLanguage.JAPANESE -> "アプリ名"
            else -> "应用名称"
        }
        SortMode.PACKAGE_SIZE -> when (language) {
            AppLanguage.ENGLISH -> "Package size"
            AppLanguage.JAPANESE -> "パッケージサイズ"
            else -> "安装包大小"
        }
        SortMode.INSTALL_TIME -> when (language) {
            AppLanguage.ENGLISH -> "Install time"
            AppLanguage.JAPANESE -> "インストール日時"
            else -> "安装时间"
        }
    }

    fun noApps(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "No apps to display"
        AppLanguage.JAPANESE -> "表示できるアプリがありません"
        else -> "没有可显示的应用"
    }

    fun noMatch(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "No matching apps"
        AppLanguage.JAPANESE -> "一致するアプリがありません"
        else -> "没有匹配的应用"
    }

    fun rootDeniedTitle(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Root permission not granted"
        AppLanguage.JAPANESE -> "Root 権限を取得できませんでした"
        else -> "抱歉，没有获取到 Root 权限"
    }

    fun rootDeniedText(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "This app requires superuser permission to work properly."
        AppLanguage.JAPANESE -> "このアプリを正常に動作させるには superuser 権限が必要です。"
        else -> "本应用需要超级用户权限才能正常工作。"
    }

    fun retry(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Retry"
        AppLanguage.JAPANESE -> "再試行"
        else -> "重试"
    }

    fun confirm(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Confirm"
        AppLanguage.JAPANESE -> "確認"
        else -> "确认"
    }

    fun revoke(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Revoke"
        AppLanguage.JAPANESE -> "拒否"
        else -> "撤销"
    }

    fun allow(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Allow"
        AppLanguage.JAPANESE -> "許可"
        else -> "允许"
    }

    fun rootQuestion(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Allow this app to obtain superuser permission?"
        AppLanguage.JAPANESE -> "このアプリにスーパーユーザー権限を許可しますか？"
        else -> "是否允许该应用获取超级用户权限？"
    }

    fun settingsTheme(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Theme"
        AppLanguage.JAPANESE -> "テーマ"
        else -> "主题"
    }

    fun settingsLanguage(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Language"
        AppLanguage.JAPANESE -> "言語"
        else -> "语言"
    }

    fun settingsFloatingBar(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Floating bottom bar"
        AppLanguage.JAPANESE -> "フローティング下部バー"
        else -> "悬浮底栏"
    }

    fun settingsScale(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "UI scale"
        AppLanguage.JAPANESE -> "UI スケール"
        else -> "界面缩放"
    }

    fun settingsThemeSummary(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Choose the app theme mode"
        AppLanguage.JAPANESE -> "アプリのテーマモードを選択します"
        else -> "选择应用的主题模式"
    }

    fun settingsLanguageSummary(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Switch the current app display language"
        AppLanguage.JAPANESE -> "このアプリの表示言語を切り替えます"
        else -> "切换当前应用显示语言"
    }

    fun settingsFloatingSummary(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Use the current white floating capsule bottom bar."
        AppLanguage.JAPANESE -> "現在の白いカプセル型フローティングバーを使用します。"
        else -> "使用当前这套白色悬浮胶囊底栏。"
    }

    fun settingsScaleSummary(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Adjust the global display scale"
        AppLanguage.JAPANESE -> "全体の表示倍率を調整します"
        else -> "调整全局显示比例"
    }

    fun scaleRange(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "80%, 90%, 100%, 110%"
        AppLanguage.JAPANESE -> "80%、90%、100%、110%"
        else -> "80%、90%、100%、110%"
    }

    fun ratio(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Scale"
        AppLanguage.JAPANESE -> "倍率"
        else -> "比例"
    }

    fun cancel(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Cancel"
        AppLanguage.JAPANESE -> "キャンセル"
        else -> "取消"
    }

    fun themeLabel(mode: AppThemeMode, language: AppLanguage): String = when (mode) {
        AppThemeMode.SYSTEM -> when (language) {
            AppLanguage.ENGLISH -> "Follow system"
            AppLanguage.JAPANESE -> "システムに従う"
            else -> "跟随系统"
        }
        AppThemeMode.LIGHT -> when (language) {
            AppLanguage.ENGLISH -> "Light"
            AppLanguage.JAPANESE -> "ライト"
            else -> "浅色"
        }
        AppThemeMode.DARK -> when (language) {
            AppLanguage.ENGLISH -> "Dark"
            AppLanguage.JAPANESE -> "ダーク"
            else -> "深色"
        }
    }
}
