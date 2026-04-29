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


private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2F80ED),
    background = Color(0xFFF5F6FA),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF10131A),
    onSurfaceVariant = Color(0xFF7A8291),
    surfaceVariant = Color(0xFFF1F3F7),
    outlineVariant = Color(0xFFE7EAF0)
)


private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2F80ED),          
    background = Color(0xFF000000),       
    surface = Color(0xFF1C1C1D),
    onBackground = Color(0xFFE3E3E3),
    onSurfaceVariant = Color(0xFF8A8A8E),
    surfaceVariant = Color(0xFF2C2C2E),
    outlineVariant = Color(0xFF3A3A3C)
)

@Composable
fun ComposeEmptyActivityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
