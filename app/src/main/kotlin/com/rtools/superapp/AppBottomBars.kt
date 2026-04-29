package com.rtools.superapp

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DefaultBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FloatingBottomBarItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Apps,
                label = I18n.appTab(currentLanguage),
                selected = selectedTab == MainTab.APPS,
                onClick = { onTabSelected(MainTab.APPS) },
            )
            FloatingBottomBarItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Extension,
                label = I18n.moduleTab(currentLanguage),
                selected = selectedTab == MainTab.MODULES,
                onClick = { onTabSelected(MainTab.MODULES) },
            )
            FloatingBottomBarItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Settings,
                label = I18n.settingsTab(currentLanguage),
                selected = selectedTab == MainTab.SETTINGS,
                onClick = { onTabSelected(MainTab.SETTINGS) },
            )
        }
    }
}

@Composable
fun FloatingBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 52.dp, end = 52.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FloatingBottomBarItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Apps,
                    label = I18n.appTab(currentLanguage),
                    selected = selectedTab == MainTab.APPS,
                    onClick = { onTabSelected(MainTab.APPS) },
                )
                FloatingBottomBarItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Extension,
                    label = I18n.moduleTab(currentLanguage),
                    selected = selectedTab == MainTab.MODULES,
                    onClick = { onTabSelected(MainTab.MODULES) },
                )
                FloatingBottomBarItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Settings,
                    label = I18n.settingsTab(currentLanguage),
                    selected = selectedTab == MainTab.SETTINGS,
                    onClick = { onTabSelected(MainTab.SETTINGS) },
                )
            }
        }
    }
}

@Composable
fun FloatingBottomBarItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "bottom_bar_content",
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor,
            )
        }
    }
}
