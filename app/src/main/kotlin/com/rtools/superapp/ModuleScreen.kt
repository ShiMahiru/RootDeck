package com.rtools.superapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import com.rtools.superapp.ksuwebui.FileSystemService
import com.rtools.superapp.ksuwebui.WebUIActivity

private data class ModuleItem(
    val name: String,
    val id: String,
    val desc: String,
    val author: String,
    val version: String,
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ModuleScreen(currentLanguage: AppLanguage, bottomPadding: Dp = 16.dp) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var refreshToken by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    val modules = remember { mutableStateListOf<ModuleItem>() }
    var showDisabled by rememberSaveable { mutableStateOf(prefs.getBoolean("show_disabled", false)) }
    var enableWebDebugging by rememberSaveable { mutableStateOf(prefs.getBoolean("enable_web_debugging", false)) }

    fun saveShowDisabled(value: Boolean) {
        showDisabled = value
        prefs.edit().putBoolean("show_disabled", value).apply()
        refreshToken++
    }

    fun saveEnableWebDebugging(value: Boolean) {
        enableWebDebugging = value
        prefs.edit().putBoolean("enable_web_debugging", value).apply()
    }

    suspend fun loadModules() {
        loadError = null
        val listener = object : FileSystemService.Listener {
            override fun onServiceAvailable(fs: com.topjohnwu.superuser.nio.FileSystemManager) {
                try {
                    val mods = mutableListOf<ModuleItem>()
                    fs.getFile("/data/adb/modules").listFiles()?.forEach { f ->
                        if (!f.isDirectory) return@forEach
                        if (!fs.getFile(f, "webroot").isDirectory) return@forEach
                        if (fs.getFile(f, "disable").exists() && !showDisabled) return@forEach

                        var name = f.name
                        val id = f.name
                        var author = "?"
                        var version = "?"
                        var desc = ""

                        fs.getFile(f, "module.prop").newInputStream().bufferedReader().use { reader ->
                            reader.lines().forEach { line ->
                                val parts = line.split("=", limit = 2)
                                if (parts.size == 2) {
                                    when (parts[0]) {
                                        "name" -> name = parts[1]
                                        "description" -> desc = parts[1]
                                        "author" -> author = parts[1]
                                        "version" -> version = parts[1]
                                    }
                                }
                            }
                        }

                        mods.add(ModuleItem(name, id, desc, author, version))
                    }
                    modules.clear()
                    modules.addAll(mods)
                    loading = false
                    refreshing = false
                } catch (e: Exception) {
                    modules.clear()
                    loadError = e.message ?: "加载失败"
                    loading = false
                    refreshing = false
                } finally {
                    FileSystemService.removeListener(this)
                }
            }

            override fun onLaunchFailed() {
                modules.clear()
                loadError = "请授予 Root 权限"
                loading = false
                refreshing = false
                FileSystemService.removeListener(this)
            }
        }
        FileSystemService.start(listener)
    }

    LaunchedEffect(refreshToken, showDisabled) {
        if (modules.isEmpty()) loading = true else refreshing = true
        loadModules()
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { refreshToken++ },
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            ) {
                TopBar(
                    title = I18n.moduleTitle(currentLanguage),
                    trailingContent = {
                        Box {
                            WhiteCircleMenuButton(onClick = { menuExpanded = true })

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.widthIn(min = 188.dp, max = 220.dp),
                                offset = DpOffset(x = 0.dp, y = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            ) {
                                ModuleMenuItem(
                                    text = when (currentLanguage) {
                                        AppLanguage.ENGLISH -> "Enable WebView debugging"
                                        AppLanguage.JAPANESE -> "WebView デバッグを有効化"
                                        else -> "启用 WebView 调试"
                                    },
                                    selected = enableWebDebugging,
                                    onClick = {
                                        saveEnableWebDebugging(!enableWebDebugging)
                                        menuExpanded = false
                                    },
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                                    thickness = 0.8.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                                ModuleMenuItem(
                                    text = when (currentLanguage) {
                                        AppLanguage.ENGLISH -> "Show disabled modules"
                                        AppLanguage.JAPANESE -> "無効モジュールを表示"
                                        else -> "显示禁用模块"
                                    },
                                    selected = showDisabled,
                                    onClick = {
                                        saveShowDisabled(!showDisabled)
                                        menuExpanded = false
                                    },
                                )
                            }
                        }
                    },
                )

                Spacer(modifier = Modifier.height(14.dp))

                when {
                    loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.4.dp,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }

                    loadError != null -> CenterStateText(
                        loadError ?: when (currentLanguage) {
                            AppLanguage.ENGLISH -> "Load failed"
                            AppLanguage.JAPANESE -> "読み込みに失敗しました"
                            else -> "加载失败"
                        },
                    )

                    modules.isEmpty() -> CenterStateText(
                        when (currentLanguage) {
                            AppLanguage.ENGLISH -> "No modules"
                            AppLanguage.JAPANESE -> "利用可能なモジュールがありません"
                            else -> "无可用模块"
                        },
                    )

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 14.dp,
                                end = 14.dp,
                                bottom = bottomPadding,
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(modules, key = { it.id }) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            try {
                                                context.startActivity(
                                                    Intent(context, WebUIActivity::class.java)
                                                        .setData(Uri.parse("ksuwebui://webui/${item.id}"))
                                                        .putExtra("id", item.id)
                                                        .putExtra("name", item.name),
                                                )
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    e.message ?: "打开失败",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                        },
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 14.dp),
                                    ) {
                                        Text(
                                            text = item.name,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "作者：${item.author}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Spacer(modifier = Modifier.height(5.dp))
                                        Text(
                                            text = "版本：${item.version}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        if (item.desc.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = item.desc,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                scale = true,
            )
        }
    }
}

@Composable
private fun ModuleMenuItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = text,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
        onClick = onClick,
    )
}
