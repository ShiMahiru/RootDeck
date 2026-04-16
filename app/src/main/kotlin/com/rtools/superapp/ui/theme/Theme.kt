package com.rtools.superapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 原有的亮色模式 (MIUI 风格)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2F80ED),          // MenuBlue
    background = Color(0xFFF5F6FA),       // MiuiBackground
    surface = Color(0xFFFFFFFF),          // MiuiSurface
    onBackground = Color(0xFF10131A),     // MiuiTextPrimary
    onSurfaceVariant = Color(0xFF7A8291), // MiuiTextSecondary
    surfaceVariant = Color(0xFFF1F3F7),   // MiuiSearchFill
    outlineVariant = Color(0xFFE7EAF0)    // MiuiCardBorder
)

// 新增的暗色模式
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2F80ED),          // 保持蓝色强调色
    background = Color(0xFF000000),       // 纯黑背景
    surface = Color(0xFF1C1C1D),          // 深灰卡片
    onBackground = Color(0xFFE3E3E3),     // 亮色文字
    onSurfaceVariant = Color(0xFF8A8A8E), // 次要文字
    surfaceVariant = Color(0xFF2C2C2E),   // 深色搜索框/图标底色
    outlineVariant = Color(0xFF3A3A3C)    // 边框颜色
)

@Composable
fun ComposeEmptyActivityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 设为 false 以确保优先使用我们自定义的配色，如果想要 Android 12+ 壁纸取色可设为 true
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
