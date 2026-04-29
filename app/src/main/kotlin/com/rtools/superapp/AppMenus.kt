package com.rtools.superapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppMenus(
    currentLanguage: AppLanguage,
    mainMenuExpanded: Boolean,
    onMainMenuExpandedChange: (Boolean) -> Unit,
    sortMenuExpanded: Boolean,
    onSortMenuExpandedChange: (Boolean) -> Unit,
    showSystemApps: Boolean,
    onShowSystemAppsChange: (Boolean) -> Unit,
    sortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit,
    descending: Boolean,
    onDescendingChange: (Boolean) -> Unit,
) {
    Box {
        WhiteCircleMenuButton(
            onClick = {
                onSortMenuExpandedChange(false)
                onMainMenuExpandedChange(true)
            },
        )

        DropdownMenu(
            expanded = mainMenuExpanded,
            onDismissRequest = { onMainMenuExpandedChange(false) },
            modifier = Modifier.width(188.dp),
            offset = DpOffset(x = 0.dp, y = 8.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = I18n.sortMode(currentLanguage),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                },
                onClick = {
                    onMainMenuExpandedChange(false)
                    onSortMenuExpandedChange(true)
                },
            )
            MenuItemRow(
                text = I18n.showSystemApps(currentLanguage),
                selected = showSystemApps,
                onClick = { onShowSystemAppsChange(!showSystemApps) },
            )
        }

        DropdownMenu(
            expanded = sortMenuExpanded,
            onDismissRequest = { onSortMenuExpandedChange(false) },
            modifier = Modifier.width(196.dp),
            offset = DpOffset(x = 0.dp, y = 8.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            MenuItemRow(
                text = I18n.sortLabel(SortMode.APP_NAME, currentLanguage),
                selected = sortMode == SortMode.APP_NAME,
                onClick = { onSortModeChange(SortMode.APP_NAME) },
            )
            MenuItemRow(
                text = I18n.sortLabel(SortMode.PACKAGE_SIZE, currentLanguage),
                selected = sortMode == SortMode.PACKAGE_SIZE,
                onClick = { onSortModeChange(SortMode.PACKAGE_SIZE) },
            )
            MenuItemRow(
                text = I18n.sortLabel(SortMode.INSTALL_TIME, currentLanguage),
                selected = sortMode == SortMode.INSTALL_TIME,
                onClick = { onSortModeChange(SortMode.INSTALL_TIME) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                thickness = 0.8.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            MenuItemRow(
                text = I18n.descending(currentLanguage),
                selected = descending,
                onClick = { onDescendingChange(!descending) },
            )
        }
    }
}
