package com.rtools.superapp

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.rtools.superapp.ui.theme.ComposeEmptyActivityTheme
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class InstalledAppItem(
    val appName: String,
    val packageName: String,
    val icon: Drawable?,
    val isSystem: Boolean,
    val sizeBytes: Long,
    val firstInstallTime: Long,
    val lastUpdateTime: Long
)

enum class SortMode(val label: String) {
    APP_NAME("应用名称"),
    PACKAGE_SIZE("安装包大小"),
    INSTALL_TIME("安装时间")
}

private enum class RootGrantState {
    UNKNOWN,
    GRANTED,
    DENIED,
    UNAVAILABLE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val pm = packageManager
        setContent {
            MainRootScreen(packageManager = pm)
        }
    }
}

private enum class MainTab {
    APPS,
    MODULES,
    SETTINGS
}

enum class AppThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色")
}

@Composable
private fun MainRootScreen(packageManager: PackageManager) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.APPS) }
    var keyword by rememberSaveable { mutableStateOf("") }
    var showSystemApps by rememberSaveable {
        mutableStateOf(prefs.getBoolean("apps_show_system", false))
    }
    var sortModeName by rememberSaveable {
        mutableStateOf(prefs.getString("apps_sort_mode", SortMode.INSTALL_TIME.name) ?: SortMode.INSTALL_TIME.name)
    }
    var descending by rememberSaveable {
        mutableStateOf(prefs.getBoolean("apps_descending", true))
    }
    var themeModeName by rememberSaveable {
        mutableStateOf(prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
    }
    var floatingBottomBar by rememberSaveable {
        mutableStateOf(prefs.getBoolean("floating_bottom_bar", false))
    }

    val sortMode = SortMode.values().firstOrNull { it.name == sortModeName } ?: SortMode.INSTALL_TIME
    val themeMode = AppThemeMode.values().firstOrNull { it.name == themeModeName } ?: AppThemeMode.SYSTEM
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    fun saveShowSystemApps(value: Boolean) {
        showSystemApps = value
        prefs.edit().putBoolean("apps_show_system", value).apply()
    }

    fun saveSortMode(value: SortMode) {
        sortModeName = value.name
        prefs.edit().putString("apps_sort_mode", value.name).apply()
    }

    fun saveDescending(value: Boolean) {
        descending = value
        prefs.edit().putBoolean("apps_descending", value).apply()
    }

    fun saveThemeMode(value: AppThemeMode) {
        themeModeName = value.name
        prefs.edit().putString("theme_mode", value.name).apply()
    }

    fun saveFloatingBottomBar(value: Boolean) {
        floatingBottomBar = value
        prefs.edit().putBoolean("floating_bottom_bar", value).apply()
    }

    ComposeEmptyActivityTheme(darkTheme = darkTheme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!floatingBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background, 
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == MainTab.APPS,
                            onClick = { selectedTab = MainTab.APPS },
                            icon = { Icon(Icons.Filled.Apps, contentDescription = "应用") },
                            label = { Text("应用") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.MODULES,
                            onClick = { selectedTab = MainTab.MODULES },
                            icon = { Icon(Icons.Filled.Extension, contentDescription = "模块") },
                            label = { Text("模块") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.SETTINGS,
                            onClick = { selectedTab = MainTab.SETTINGS },
                            icon = { Icon(Icons.Filled.Settings, contentDescription = "设置") },
                            label = { Text("设置") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val listBottomPadding = if (floatingBottomBar) 96.dp else 14.dp

                when (selectedTab) {
                    MainTab.APPS -> MiuiAppManagerScreen(
                        packageManager = packageManager,
                        keyword = keyword,
                        onKeywordChange = { keyword = it },
                        showSystemApps = showSystemApps,
                        onShowSystemAppsChange = { saveShowSystemApps(it) },
                        sortMode = sortMode,
                        onSortModeChange = { saveSortMode(it) },
                        descending = descending,
                        onDescendingChange = { saveDescending(it) },
                        bottomPadding = listBottomPadding
                    )

                    MainTab.MODULES -> ModuleScreen(bottomPadding = listBottomPadding)
                    
                    MainTab.SETTINGS -> SettingsScreen(
                        themeMode = themeMode,
                        onThemeModeChange = { saveThemeMode(it) },
                        floatingBottomBar = floatingBottomBar,
                        onFloatingBottomBarChange = { saveFloatingBottomBar(it) },
                        bottomPadding = listBottomPadding
                    )
                }

                if (floatingBottomBar) {
                    FloatingBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 48.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            // 修改点：改为实体背景色，去除透明度
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppleBottomBarItem(
                    icon = Icons.Filled.Apps,
                    label = "应用",
                    selected = selectedTab == MainTab.APPS,
                    onClick = { onTabSelected(MainTab.APPS) }
                )
                AppleBottomBarItem(
                    icon = Icons.Filled.Extension,
                    label = "模块",
                    selected = selectedTab == MainTab.MODULES,
                    onClick = { onTabSelected(MainTab.MODULES) }
                )
                AppleBottomBarItem(
                    icon = Icons.Filled.Settings,
                    label = "设置",
                    selected = selectedTab == MainTab.SETTINGS,
                    onClick = { onTabSelected(MainTab.SETTINGS) }
                )
            }
        }
    }
}

@Composable
private fun AppleBottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "color"
    )
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = iconColor
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MiuiAppManagerScreen(
    packageManager: PackageManager,
    keyword: String,
    onKeywordChange: (String) -> Unit,
    showSystemApps: Boolean,
    onShowSystemAppsChange: (Boolean) -> Unit,
    sortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit,
    descending: Boolean,
    onDescendingChange: (Boolean) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp
) {
    val context = LocalContext.current
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
        Shell.cmd(cmd).exec().isSuccess
    }

    suspend fun setAppRootPermission(packageName: String, grant: Boolean) = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val ai = pm.getApplicationInfo(packageName, 0)
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
        val state = requestManagerRoot()
        rootGrantState = state
    }

    LaunchedEffect(Unit) {
        ensureManagerRoot(force = false)
    }

    LaunchedEffect(refreshToken) {
        if (apps.isEmpty()) {
            loading = true
        } else {
            refreshing = true
        }
        loadApps()
        loading = false
        refreshing = false
        listState.scrollToItem(0)
    }

    val filteredApps = remember(apps, keyword, showSystemApps, sortMode, descending) {
        apps.asSequence()
            .filter { showSystemApps || !it.isSystem }
            .filter {
                keyword.isBlank() || it.appName.contains(keyword, ignoreCase = true) ||
                    it.packageName.contains(keyword, ignoreCase = true)
            }
            .sortedWith(appComparator(sortMode, descending))
            .toList()
    }

    LaunchedEffect(keyword, showSystemApps, sortMode, descending, filteredApps.size) {
        if (!loading && filteredApps.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { refreshToken++ }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                TopTitleRow(
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
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                SearchBar(
                    keyword = keyword,
                    onKeywordChange = onKeywordChange
                )

                Spacer(modifier = Modifier.height(6.dp))

                when {
                    loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.4.dp,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    loadError != null -> {
                        CenterStateText(loadError ?: "加载失败")
                    }

                    filteredApps.isEmpty() -> {
                        CenterStateText(if (keyword.isBlank()) "没有可显示的应用" else "没有匹配的应用")
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = bottomPadding),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredApps, key = { it.packageName }) { app ->
                                AppItemCard(
                                    app = app,
                                    onClick = { selectedApp = app }
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
                scale = true
            )

            if (rootGrantState == RootGrantState.DENIED || rootGrantState == RootGrantState.UNAVAILABLE) {
                AlertDialog(
                    onDismissRequest = { /* 拦截返回键和外部点击 */ },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    title = {
                        Text(
                            text = "抱歉，没有获取到ROOT权限",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    text = {
                        Text(
                            text = "本应用需要超级用户权限才能正常工作。",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            val activity = context as? Activity
                            activity?.finishAffinity()
                            android.os.Process.killProcess(android.os.Process.myPid())
                            kotlin.system.exitProcess(0)
                        }) {
                            Text("确认", fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            coroutineScope.launch {
                                ensureManagerRoot(force = true) 
                            }
                        }) {
                            Text("重试", fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }

            selectedApp?.let { app ->
                RootActionDialog(
                    appName = app.appName,
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
                    }
                )
            }
        }
    }
}

@Composable
private fun TopTitleRow(
    mainMenuExpanded: Boolean,
    onMainMenuExpandedChange: (Boolean) -> Unit,
    sortMenuExpanded: Boolean,
    onSortMenuExpandedChange: (Boolean) -> Unit,
    showSystemApps: Boolean,
    onShowSystemAppsChange: (Boolean) -> Unit,
    sortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit,
    descending: Boolean,
    onDescendingChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "RootDeck",
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            maxLines = 1
        )

        Box {
            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clickable {
                        onSortMenuExpandedChange(false)
                        onMainMenuExpandedChange(true)
                    },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "菜单",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = mainMenuExpanded,
                onDismissRequest = { onMainMenuExpandedChange(false) },
                modifier = Modifier.width(184.dp),
                offset = DpOffset(x = 0.dp, y = 8.dp),
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "排序方式",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        onMainMenuExpandedChange(false)
                        onSortMenuExpandedChange(true)
                    }
                )

                MenuItemRow(
                    text = "显示系统应用",
                    selected = showSystemApps,
                    onClick = {
                        onShowSystemAppsChange(!showSystemApps)
                    }
                )
            }

            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { onSortMenuExpandedChange(false) },
                modifier = Modifier.width(194.dp),
                offset = DpOffset(x = 0.dp, y = 8.dp),
                shape = RoundedCornerShape(18.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                MenuItemRow(
                    text = SortMode.APP_NAME.label,
                    selected = sortMode == SortMode.APP_NAME,
                    onClick = { onSortModeChange(SortMode.APP_NAME) }
                )
                MenuItemRow(
                    text = SortMode.PACKAGE_SIZE.label,
                    selected = sortMode == SortMode.PACKAGE_SIZE,
                    onClick = { onSortModeChange(SortMode.PACKAGE_SIZE) }
                )
                MenuItemRow(
                    text = SortMode.INSTALL_TIME.label,
                    selected = sortMode == SortMode.INSTALL_TIME,
                    onClick = { onSortModeChange(SortMode.INSTALL_TIME) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    thickness = 0.8.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                MenuItemRow(
                    text = "倒序排列",
                    selected = descending,
                    onClick = { onDescendingChange(!descending) }
                )
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = text,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        onClick = onClick
    )
}

@Composable
private fun SearchBar(
    keyword: String,
    onKeywordChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "搜索",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                BasicTextField(
                    value = keyword,
                    onValueChange = onKeywordChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = if (keyword.isNotBlank()) 40.dp else 0.dp),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (keyword.isBlank()) {
                                Text(
                                    text = "搜索",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                if (keyword.isNotBlank()) {
                    Text(
                        text = "清除",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            onKeywordChange("")
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterStateText(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun AppItemCard(
    app: InstalledAppItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(drawable = app.icon, fallbackText = app.appName)
            Spacer(modifier = Modifier.width(13.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = app.appName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = app.packageName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AppIcon(drawable: Drawable?, fallbackText: String) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant), 
        contentAlignment = Alignment.Center
    ) {
        if (drawable != null) {
            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    }
                },
                update = { imageView ->
                    imageView.setImageDrawable(drawable)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            )
        } else {
            Text(
                text = fallbackText.take(1),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RootActionDialog(
    appName: String,
    onDismiss: () -> Unit,
    onDeny: () -> Unit,
    onAllow: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = appName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Text(
                text = "是否允许该应用获取超级用户权限？",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text(
                    text = "撤销",
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAllow) {
                Text(
                    text = "允许",
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

private suspend fun requestManagerRoot(): RootGrantState = withContext(Dispatchers.IO) {
    try {
        if (Shell.getShell().isRoot) {
            RootGrantState.GRANTED
        } else {
            RootGrantState.DENIED
        }
    } catch (_: NoClassDefFoundError) {
        RootGrantState.UNAVAILABLE
    } catch (_: Exception) {
        RootGrantState.UNAVAILABLE
    }
}

private fun appComparator(sortMode: SortMode, descending: Boolean): Comparator<InstalledAppItem> {
    val comparator = when (sortMode) {
        SortMode.APP_NAME -> compareBy<InstalledAppItem, String>(String.CASE_INSENSITIVE_ORDER) { it.appName }
        SortMode.PACKAGE_SIZE -> compareBy<InstalledAppItem> { it.sizeBytes }
        SortMode.INSTALL_TIME -> compareBy<InstalledAppItem> { it.firstInstallTime }
    }
    return if (descending) comparator.reversed() else comparator
}

private suspend fun loadInstalledApps(packageManager: PackageManager): List<InstalledAppItem> = withContext(Dispatchers.IO) {
    val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getInstalledApplications(
            PackageManager.ApplicationInfoFlags.of(PackageManager.MATCH_ALL.toLong())
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
                        PackageManager.PackageInfoFlags.of(0)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(packageName, 0)
                }

                val flags = appInfo.flags
                val isSystemApp = (flags and ApplicationInfo.FLAG_SYSTEM) != 0 || (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                InstalledAppItem(
                    appName = packageManager.getApplicationLabel(appInfo)?.toString().orEmpty()
                        .ifBlank { packageName },
                    packageName = packageName,
                    icon = runCatching { packageManager.getApplicationIcon(appInfo) }.getOrNull(),
                    isSystem = isSystemApp,
                    sizeBytes = File(appInfo.sourceDir ?: "").takeIf { it.exists() }?.length() ?: 0L,
                    firstInstallTime = packageInfo.firstInstallTime,
                    lastUpdateTime = packageInfo.lastUpdateTime
                )
            }.getOrNull()
        }
        .toList()
}
