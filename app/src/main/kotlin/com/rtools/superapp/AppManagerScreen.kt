package com.rtools.superapp

import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppManagerScreen(
    packageManager: PackageManager,
    currentLanguage: AppLanguage,
    keyword: String,
    onKeywordChange: (String) -> Unit,
    showSystemApps: Boolean,
    onShowSystemAppsChange: (Boolean) -> Unit,
    sortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit,
    descending: Boolean,
    onDescendingChange: (Boolean) -> Unit,
    bottomPadding: Dp,
) {
    val context = LocalContext.current
    val appDisplayName = remember(context) {
        runCatching {
            context.packageManager.getApplicationLabel(context.applicationInfo).toString()
        }.getOrDefault(I18n.appTitle(currentLanguage))
    }
    var mainMenuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var refreshToken by remember { mutableIntStateOf(0) }
    val apps = remember { mutableStateListOf<InstalledAppItem>() }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var rootGrantState by remember { mutableStateOf(RootGrantState.UNKNOWN) }
    var selectedApp by remember { mutableStateOf<InstalledAppItem?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    suspend fun executeRootCommand(cmd: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            process.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }

    suspend fun setAppRootPermission(packageName: String, grant: Boolean) = withContext(Dispatchers.IO) {
        try {
            val ai = context.packageManager.getApplicationInfo(packageName, 0)
            val uid = ai.uid
            val policy = if (grant) 2 else 0
            val sqlCmd = "magisk --sqlite \"REPLACE INTO policies (uid,policy,until,logging,notification) VALUES ($uid,$policy,0,1,1);\""
            executeRootCommand(sqlCmd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadApps() {
        loadError = null
        try {
            val result = loadInstalledApps(packageManager)
            apps.clear()
            apps.addAll(result)
        } catch (e: Exception) {
            loadError = e.message ?: "加载失败"
            apps.clear()
        }
    }

    suspend fun ensureManagerRoot(force: Boolean = false) {
        if (!force && rootGrantState == RootGrantState.GRANTED) return
        rootGrantState = RootGrantState.UNKNOWN
        rootGrantState = requestManagerRoot()
    }

    LaunchedEffect(Unit) {
        ensureManagerRoot()
    }

    LaunchedEffect(refreshToken) {
        if (apps.isEmpty()) loading = true else refreshing = true
        loadApps()
        loading = false
        refreshing = false
        if (apps.isNotEmpty()) listState.scrollToItem(0)
    }

    val filteredApps = apps
        .asSequence()
        .filter { showSystemApps || !it.isSystem }
        .filter {
            keyword.isBlank() ||
                it.appName.contains(keyword, ignoreCase = true) ||
                it.packageName.contains(keyword, ignoreCase = true)
        }
        .sortedWith(appComparator(sortMode, descending))
        .toList()

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
                    title = appDisplayName,
                    trailingContent = {
                        AppMenus(
                            currentLanguage = currentLanguage,
                            mainMenuExpanded = mainMenuExpanded,
                            onMainMenuExpandedChange = { mainMenuExpanded = it },
                            sortMenuExpanded = sortMenuExpanded,
                            onSortMenuExpandedChange = { sortMenuExpanded = it },
                            showSystemApps = showSystemApps,
                            onShowSystemAppsChange = {
                                onShowSystemAppsChange(it)
                                mainMenuExpanded = false
                            },
                            sortMode = sortMode,
                            onSortModeChange = {
                                onSortModeChange(it)
                                sortMenuExpanded = false
                            },
                            descending = descending,
                            onDescendingChange = {
                                onDescendingChange(it)
                                sortMenuExpanded = false
                            },
                        )
                    },
                )

                Spacer(modifier = Modifier.height(1.dp))

                SearchBar(
                    keyword = keyword,
                    onKeywordChange = onKeywordChange,
                    currentLanguage = currentLanguage,
                )

                Spacer(modifier = Modifier.height(5.dp))

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

                    loadError != null -> CenterStateText(loadError ?: "加载失败")

                    filteredApps.isEmpty() -> {
                        CenterStateText(if (keyword.isBlank()) I18n.noApps(currentLanguage) else I18n.noMatch(currentLanguage))
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 14.dp,
                                end = 14.dp,
                                bottom = bottomPadding,
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(filteredApps, key = { it.packageName }) { app ->
                                AppItemCard(
                                    app = app,
                                    onClick = { selectedApp = app },
                                )
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

            if (rootGrantState == RootGrantState.DENIED || rootGrantState == RootGrantState.UNAVAILABLE) {
                AlertDialog(
                    onDismissRequest = {},
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    title = {
                        Text(
                            text = I18n.rootDeniedTitle(currentLanguage),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    text = {
                        Text(
                            text = I18n.rootDeniedText(currentLanguage),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                val activity = context as? Activity
                                activity?.finishAffinity()
                                android.os.Process.killProcess(android.os.Process.myPid())
                                kotlin.system.exitProcess(0)
                            },
                        ) {
                            Text(I18n.confirm(currentLanguage), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { coroutineScope.launch { ensureManagerRoot(force = true) } },
                        ) {
                            Text(I18n.retry(currentLanguage), color = MaterialTheme.colorScheme.primary)
                        }
                    },
                )
            }

            selectedApp?.let { app ->
                RootActionDialog(
                    appName = app.appName,
                    currentLanguage = currentLanguage,
                    onDismiss = { selectedApp = null },
                    onDeny = {
                        coroutineScope.launch {
                            setAppRootPermission(app.packageName, grant = false)
                            Toast.makeText(context, "已撤销 ${app.appName}", Toast.LENGTH_SHORT).show()
                            selectedApp = null
                        }
                    },
                    onAllow = {
                        coroutineScope.launch {
                            setAppRootPermission(app.packageName, grant = true)
                            Toast.makeText(context, "已授予 ${app.appName} Root 权限", Toast.LENGTH_SHORT).show()
                            selectedApp = null
                        }
                    },
                )
            }
        }
    }
}
