package dev.chrisbanes.haze

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

data class HazeState(
    val blurEnabled: Boolean = true,
)

@Composable
fun rememberHazeState(
    blurEnabled: Boolean = true,
): HazeState = remember(blurEnabled) { HazeState(blurEnabled = blurEnabled) }

fun Modifier.hazeSource(state: HazeState): Modifier = this

fun Modifier.hazeEffect(state: HazeState): Modifier = this
