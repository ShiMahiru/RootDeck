package com.rtools.superapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    appLanguage: AppLanguage,
    onAppLanguageChange: (AppLanguage) -> Unit,
    floatingBottomBar: Boolean,
    onFloatingBottomBarChange: (Boolean) -> Unit,
    bottomPadding: Dp
) {
    var themeMenuExpanded by remember { mutableStateOf(false) }
    var languageMenuExpanded by remember { mutableStateOf(false) }


    @Composable
    fun themeLabel(mode: AppThemeMode): String = when (mode) {
        AppThemeMode.SYSTEM -> stringResource(R.string.theme_system)
        AppThemeMode.LIGHT -> stringResource(R.string.theme_light)
        AppThemeMode.DARK -> stringResource(R.string.theme_dark)
    }


    @Composable
    fun languageLabel(language: AppLanguage): String = when (language) {
        AppLanguage.SYSTEM -> stringResource(R.string.lang_system)
        AppLanguage.ZH_CN -> stringResource(R.string.lang_zh_cn)
        AppLanguage.EN -> stringResource(R.string.lang_en)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                maxLines = 1
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = bottomPadding)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { themeMenuExpanded = true }
                                    .padding(horizontal = 18.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_theme),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = themeLabel(themeMode),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = themeMenuExpanded,
                                onDismissRequest = { themeMenuExpanded = false },
                                modifier = Modifier.width(160.dp),
                                offset = DpOffset(x = 180.dp, y = 0.dp),
                                shape = RoundedCornerShape(16.dp),
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                AppThemeMode.values().forEach { mode ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = themeLabel(mode),
                                                color = if (themeMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                                fontSize = 15.sp
                                            )
                                        },
                                        onClick = {
                                            onThemeModeChange(mode)
                                            themeMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { languageMenuExpanded = true }
                                    .padding(horizontal = 18.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_language),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = languageLabel(appLanguage),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = languageMenuExpanded,
                                onDismissRequest = { languageMenuExpanded = false },
                                modifier = Modifier.width(180.dp),
                                offset = DpOffset(x = 160.dp, y = 0.dp),
                                shape = RoundedCornerShape(16.dp),
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                AppLanguage.values().forEach { language ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = languageLabel(language),
                                                color = if (appLanguage == language) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                                fontSize = 15.sp
                                            )
                                        },
                                        onClick = {
                                            onAppLanguageChange(language)
                                            languageMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFloatingBottomBarChange(!floatingBottomBar) }
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.settings_floating_bar),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Switch(
                                checked = floatingBottomBar,
                                onCheckedChange = null
                            )
                        }
                    }
                }
            }
        }
    }
}
