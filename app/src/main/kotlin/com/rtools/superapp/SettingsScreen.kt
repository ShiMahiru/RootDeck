package com.rtools.superapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val ScaleStops = listOf(80, 90, 100, 110)

@Composable
fun SettingsScreen(
    themeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    floatingBottomBar: Boolean,
    onFloatingBottomBarChange: (Boolean) -> Unit,
    uiScalePercent: Int,
    onUiScaleChange: (Int) -> Unit,
    bottomPadding: Dp,
) {
    var themeExpanded by rememberSaveable { mutableStateOf(false) }
    var languageExpanded by rememberSaveable { mutableStateOf(false) }
    var scaleDialogVisible by rememberSaveable { mutableStateOf(false) }
    var aboutVisible by rememberSaveable { mutableStateOf(false) }

    if (aboutVisible) {
        BackHandler { aboutVisible = false }
        AboutScreen(
            bottomPadding = bottomPadding,
            onBack = { aboutVisible = false },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        TopBar(title = I18n.settingsTitle(currentLanguage))

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    MiuiAnchoredSelectRow(
                        title = I18n.settingsTheme(currentLanguage),
                        summary = I18n.settingsThemeSummary(currentLanguage),
                        value = I18n.themeLabel(themeMode, currentLanguage),
                        expanded = themeExpanded,
                        onExpandedChange = {
                            themeExpanded = it
                            if (it) languageExpanded = false
                        },
                        options = AppThemeMode.entries.map { I18n.themeLabel(it, currentLanguage) },
                        selectedValue = I18n.themeLabel(themeMode, currentLanguage),
                        onOptionClick = { selected ->
                            AppThemeMode.entries.firstOrNull {
                                I18n.themeLabel(it, currentLanguage) == selected
                            }?.let(onThemeModeChange)
                            themeExpanded = false
                        },
                    )

                    HorizontalDivider(
                        thickness = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 14.dp),
                    )

                    MiuiAnchoredSelectRow(
                        title = I18n.settingsLanguage(currentLanguage),
                        summary = I18n.settingsLanguageSummary(currentLanguage),
                        value = currentLanguage.label,
                        expanded = languageExpanded,
                        onExpandedChange = {
                            languageExpanded = it
                            if (it) themeExpanded = false
                        },
                        options = AppLanguage.entries.map { it.label },
                        selectedValue = currentLanguage.label,
                        onOptionClick = { selected ->
                            AppLanguage.entries.firstOrNull { it.label == selected }?.let(onLanguageChange)
                            languageExpanded = false
                        },
                    )

                    HorizontalDivider(
                        thickness = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 14.dp),
                    )

                    SettingSwitchRow(
                        title = I18n.settingsFloatingBar(currentLanguage),
                        summary = I18n.settingsFloatingSummary(currentLanguage),
                        checked = floatingBottomBar,
                        onCheckedChange = onFloatingBottomBarChange,
                    )

                    HorizontalDivider(
                        thickness = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 14.dp),
                    )

                    SettingSelectRow(
                        title = I18n.settingsScale(currentLanguage),
                        summary = I18n.settingsScaleSummary(currentLanguage),
                        value = "${uiScalePercent}%",
                        onClick = { scaleDialogVisible = true },
                    )

                    HorizontalDivider(
                        thickness = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 14.dp),
                    )

                    SettingSelectRow(
                        title = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "About"
                            AppLanguage.JAPANESE -> "このアプリについて"
                            else -> "关于"
                        },
                        summary = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Project links and acknowledgements"
                            AppLanguage.JAPANESE -> "プロジェクトのリンクと謝辞"
                            else -> "项目链接与致谢信息"
                        },
                        value = "",
                        onClick = { aboutVisible = true },
                        showValue = false,
                    )
                }
            }
        }
    }

    if (scaleDialogVisible) {
        var sliderValue by remember(uiScalePercent) { mutableFloatStateOf(uiScalePercent.toFloat()) }
        var inputValue by remember(uiScalePercent) { mutableStateOf(uiScalePercent.toString()) }

        AlertDialog(
            onDismissRequest = { scaleDialogVisible = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = I18n.settingsScale(currentLanguage),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            },
            text = {
                Column {
                    Text(
                        text = I18n.scaleRange(currentLanguage),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { changed ->
                            val filtered = changed.filter { it.isDigit() }.take(3)
                            inputValue = filtered
                            filtered.toIntOrNull()?.let { typed ->
                                val clamped = typed.coerceIn(80, 110)
                                sliderValue = magnetScale(clamped.toFloat())
                                inputValue = sliderValue.roundToInt().toString()
                            }
                        },
                        singleLine = true,
                        label = { Text(I18n.ratio(currentLanguage)) },
                        suffix = { Text("%") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ScaleStops.forEach { stop ->
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = if (kotlin.math.abs(stop - sliderValue) < 1f) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.48f)
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.26f)
                                            },
                                            shape = CircleShape,
                                        ),
                                )
                            }
                        }

                        Slider(
                            value = sliderValue,
                            onValueChange = { raw ->
                                sliderValue = magnetScale(raw.coerceIn(80f, 110f))
                                inputValue = sliderValue.roundToInt().toString()
                            },
                            onValueChangeFinished = {
                                sliderValue = settleScale(sliderValue)
                                inputValue = sliderValue.roundToInt().toString()
                            },
                            valueRange = 80f..110f,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { scaleDialogVisible = false }) {
                    Text(I18n.cancel(currentLanguage))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val typed = inputValue.toIntOrNull()?.coerceIn(80, 110) ?: sliderValue.roundToInt()
                        val finalValue = settleScale(typed.toFloat()).roundToInt()
                        onUiScaleChange(finalValue)
                        scaleDialogVisible = false
                    },
                ) {
                    Text(I18n.confirm(currentLanguage))
                }
            },
        )
    }
}

@Composable
private fun AboutScreen(
    bottomPadding: Dp,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val versionText = remember(context) {
        runCatching {
            val pm = context.packageManager
            val pkg = context.packageName
            val info = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(pkg, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(pkg, 0)
            }
            val versionName = info.versionName ?: "1.0"
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) info.longVersionCode else @Suppress("DEPRECATION") info.versionCode.toLong()
            "${versionName} (${versionCode})"
        }.getOrDefault("1.0")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                bottom = bottomPadding,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Spacer(modifier = Modifier.height(36.dp))
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier.size(110.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "RootDeck",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = versionText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(34.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    AboutLinkRow(
                        title = "在 github 查看源码",
                        onClick = { openUrl(context, "https://github.com/ShiMahiru/RootDeck") },
                    )
                    HorizontalDivider(
                        thickness = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    AboutLinkRow(
                        title = "跳转coolapk查看主页",
                        onClick = { openCoolApk(context) },
                    )
                    HorizontalDivider(
                        thickness = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    AboutLinkRow(
                        title = "感谢 5ec1cff 提供的 KsuWebUI",
                        onClick = { openUrl(context, "https://github.com/5ec1cff/KsuWebUIStandalone") },
                    )
                    HorizontalDivider(
                        thickness = 0.8.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    AboutLinkRow(
                        title = "UI 框架基于 compose-miuix-ui/miuix",
                        onClick = { openUrl(context, "https://github.com/compose-miuix-ui/miuix") },
                    )
                }
            }
        }
    }
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

private fun openCoolApk(context: Context) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("coolmarket://u/35697189"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    } catch (_: ActivityNotFoundException) {
        openUrl(context, "https://www.coolapk.com/u/35697189")
    }
}

private fun magnetScale(value: Float): Float {
    val stops = ScaleStops.map { it.toFloat() }
    val nearest = stops.minByOrNull { kotlin.math.abs(it - value) } ?: 100f
    val distance = kotlin.math.abs(nearest - value)
    return if (distance <= 1.5f) nearest else value
}

private fun settleScale(value: Float): Float {
    val stops = ScaleStops.map { it.toFloat() }
    return stops.minByOrNull { kotlin.math.abs(it - value) } ?: 100f
}

@Composable
private fun MiuiAnchoredSelectRow(
    title: String,
    summary: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<String>,
    selectedValue: String,
    onOptionClick: (String) -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "dropdown_arrow_rotation",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandedChange(!expanded) }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = summary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Box {
            Row(
                modifier = Modifier
                    .clickable { onExpandedChange(!expanded) }
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotation),
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.widthIn(min = 220.dp, max = 280.dp),
                offset = DpOffset(x = 0.dp, y = 8.dp),
                shape = RoundedCornerShape(22.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                fontWeight = if (option == selectedValue) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (option == selectedValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                            )
                        },
                        trailingIcon = {
                            if (option == selectedValue) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                        onClick = { onOptionClick(option) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutLinkRow(
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingSelectRow(
    title: String,
    summary: String,
    value: String,
    onClick: () -> Unit,
    showValue: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = summary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showValue && value.isNotBlank()) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = summary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
