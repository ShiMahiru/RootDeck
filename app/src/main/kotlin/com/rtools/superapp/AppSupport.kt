package com.rtools.superapp

import android.app.AlertDialog as PlatformAlertDialog
import android.view.ContextThemeWrapper
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun RootActionDialog(
    appName: String,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onDeny: () -> Unit,
    onAllow: () -> Unit,
) {
    val context = LocalContext.current
    val dialogTheme = if (isSystemInDarkTheme()) {
        android.R.style.Theme_DeviceDefault_Dialog_Alert
    } else {
        android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
    }

    DisposableEffect(appName, currentLanguage) {
        val themedContext = ContextThemeWrapper(context, dialogTheme)
        val dialog = PlatformAlertDialog.Builder(themedContext, dialogTheme)
            .setTitle(appName)
            .setMessage(I18n.rootQuestion(currentLanguage))
            .setNegativeButton(I18n.revoke(currentLanguage)) { _, _ -> onDeny() }
            .setPositiveButton(I18n.allow(currentLanguage)) { _, _ -> onAllow() }
            .setOnDismissListener { onDismiss() }
            .create()

        dialog.show()

        onDispose {
            dialog.setOnDismissListener(null)
            if (dialog.isShowing) dialog.dismiss()
        }
    }
}

suspend fun requestManagerRoot(): RootGrantState = withContext(Dispatchers.IO) {
    try {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        val stdout = process.inputStream.bufferedReader().use { it.readText() }
        val stderr = process.errorStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        when {
            exitCode == 0 && stdout.contains("uid=0") -> RootGrantState.GRANTED
            stderr.contains("not found", ignoreCase = true) -> RootGrantState.UNAVAILABLE
            else -> RootGrantState.DENIED
        }
    } catch (_: Exception) {
        RootGrantState.UNAVAILABLE
    }
}

fun appComparator(sortMode: SortMode, descending: Boolean): Comparator<InstalledAppItem> {
    val comparator = when (sortMode) {
        SortMode.APP_NAME -> compareBy<InstalledAppItem, String>(String.CASE_INSENSITIVE_ORDER) { it.appName }
        SortMode.PACKAGE_SIZE -> compareBy<InstalledAppItem> { it.sizeBytes }
        SortMode.INSTALL_TIME -> compareBy<InstalledAppItem> { it.firstInstallTime }
    }
    return if (descending) comparator.reversed() else comparator
}

suspend fun loadInstalledApps(packageManager: PackageManager): List<InstalledAppItem> = withContext(Dispatchers.IO) {
    val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getInstalledApplications(
            PackageManager.ApplicationInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.getInstalledApplications(PackageManager.MATCH_ALL)
    }

    installedApps
        .asSequence()
        .filterNot { it.packageName.isNullOrBlank() }
        .mapNotNull { appInfo ->
            runCatching {
                val packageName = appInfo.packageName
                val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(0),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(packageName, 0)
                }

                val flags = appInfo.flags
                val isSystemApp = (flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                    (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                InstalledAppItem(
                    appName = packageManager.getApplicationLabel(appInfo)?.toString().orEmpty().ifBlank { packageName },
                    packageName = packageName,
                    icon = runCatching { packageManager.getApplicationIcon(appInfo) }.getOrNull(),
                    isSystem = isSystemApp,
                    sizeBytes = File(appInfo.sourceDir ?: "").takeIf { it.exists() }?.length() ?: 0L,
                    firstInstallTime = packageInfo.firstInstallTime,
                    lastUpdateTime = packageInfo.lastUpdateTime,
                )
            }.getOrNull()
        }
        .toList()
}
