package com.rtools.superapp

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.rtools.superapp.ui.theme.ComposeEmptyActivityTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

data class InstalledAppItem(
    val appName: String,
    val packageName: String,
    val icon: Drawable?,
    val isSystem: Boolean,
    val sizeBytes: Long,
    val firstInstallTime: Long,
    val lastUpdateTime: Long
)

enum class SortMode {
    APP_NAME,
    PACKAGE_SIZE,
    INSTALL_TIME
}

private enum class RootGrantState {
    UNKNOWN,
    GRANTED,
    DENIED,
    UNAVAILABLE
}

class MainActivity : AppCompatActivity() {
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

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage(val tag: String) {
    SYSTEM("system"),
    ZH_CN("zh-CN"),
    EN("en")
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
    var appLanguageName by rememberSaveable {
        mutableStateOf(prefs.getString("app_language", AppLanguage.SYSTEM.name) ?: AppLanguage.SYSTEM.name)
    }

    val sortMode = SortMode.values().firstOrNull { it.name == sortModeName } ?: SortMode.INSTALL_TIME
    val themeMode = AppThemeMode.values().firstOrNull { it.name == themeModeName } ?: AppThemeMode.SYSTEM
    val appLanguage = AppLanguage.values().firstOrNull { it.name == appLanguageName } ?: AppLanguage.SYSTEM
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

    fun saveAppLanguage(value: AppLanguage) {
        if (appLanguage == value) return
        appLanguageName = value.name
        prefs.edit().putString("app_language", value.name).apply()
    }

    LaunchedEffect(appLanguage) {
        val locales = when (appLanguage) {
            AppLanguage.SYSTEM -> androidx.core.os.LocaleListCompat.getEmptyLocaleList()
            AppLanguage.ZH_CN -> androidx.core.os.LocaleListCompat.forLanguageTags("zh-CN")
            AppLanguage.EN -> androidx.core.os.LocaleListCompat.forLanguageTags("en")
        }
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(locales)
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
                            icon = { Icon(Icons.Filled.Apps, contentDescription = stringResource(R.string.tab_apps)) },
                            label = { Text(stringResource(R.string.tab_apps)) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.MODULES,
                            onClick = { selectedTab = MainTab.MODULES },
                            icon = { Icon(Icons.Filled.Extension, contentDescription = stringResource(R.string.tab_modules)) },
                            label = { Text(stringResource(R.string.tab_modules)) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.SETTINGS,
                            onClick = { selectedTab = MainTab.SETTINGS },
                            icon = { Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.tab_settings)) },
                            label = { Text(stringResource(R.string.tab_settings)) }
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
                        appLanguage = appLanguage,
                        onAppLanguageChange = { saveAppLanguage(it) },
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
    val coroutineScope = rememberCoroutineScope()
    val dragOffsetX = remember { Animatable(0f) }
    var pressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "floating-dock-scale"
    )
    val alpha by animateFloatAsState(targetValue = 1f, label = "floating-dock-alpha")
    val hiddenOffset by animateFloatAsState(targetValue = 0f, label = "floating-dock-offset")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .offset { IntOffset(dragOffsetX.value.roundToInt(), hiddenOffset.roundToInt()) }
            .padding(horizontal = 36.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .blur(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 1.dp else 0.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                            )
                        )
                    )
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                pressed = true
                            },
                            onDragEnd = {
                                pressed = false
                                coroutineScope.launch {
                                    dragOffsetX.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                            },
                            onDragCancel = {
                                pressed = false
                                coroutineScope.launch {
                                    dragOffsetX.animateTo(0f, spring())
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                dragOffsetX.snapTo((dragOffsetX.value + dragAmount.x).coerceIn(-120f, 120f))
                            }
                        }
                    }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppleBottomBarItem(
                    icon = Icons.Filled.Apps,
                    label = stringResource(R.string.tab_apps),
                    selected = selectedTab == MainTab.APPS,
                    onClick = {
                        onTabSelected(MainTab.APPS)
                    }
                )
                AppleBottomBarItem(
                    icon = Icons.Filled.Extension,
                    label = stringResource(R.string.tab_modules),
                    selected = selectedTab == MainTab.MODULES,
                    onClick = {
                        onTabSelected(MainTab.MODULES)
                    }
                )
                AppleBottomBarItem(
                    icon = Icons.Filled.Settings,
                    label = stringResource(R.string.tab_settings),
                    selected = selectedTab == MainTab.SETTINGS,
                    onClick = {
                        onTabSelected(MainTab.SETTINGS)
                    }
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
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
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
            loadError = e.message
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

    val filteredApps = apps
        .asSequence()
        .filter { showSystemApps || !it.isSystem }
        .filter {
            keyword.isBlank() || it.appName.contains(keyword, ignoreCase = true) ||
                it.packageName.contains(keyword, ignoreCase = true)
        }
        .sortedWith(appComparator(sortMode, descending))
        .toList()

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
                        CenterStateText(loadError ?: stringResource(R.string.load_failed))
                    }

                    filteredApps.isEmpty() -> {
                        CenterStateText(
                            if (keyword.isBlank()) {
                                stringResource(R.string.apps_empty)
                            } else {
                                stringResource(R.string.apps_no_match)
                            }
                        )
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
                            text = stringResource(R.string.root_permission_missing_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.root_permission_missing_message),
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
                            Text(
                                stringResource(R.string.confirm),
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            coroutineScope.launch {
                                ensureManagerRoot(force = true) 
                            }
                        }) {
                            Text(
                                stringResource(R.string.retry),
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
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
                            Toast.makeText(
                                context,
                                context.getString(R.string.toast_root_revoked, app.appName),
                                Toast.LENGTH_SHORT
                            ).show()
                            selectedApp = null
                        }
                    },
                    onAllow = {
                        coroutineScope.launch {
                            setAppRootPermission(app.packageName, grant = true)
                            Toast.makeText(
                                context,
                                context.getString(R.string.toast_root_granted, app.appName),
                                Toast.LENGTH_SHORT
                            ).show()
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .blur(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 1.dp else 0.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.70f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.42f)
                        )
                    )
                )
                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
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
                        contentDescription = stringResource(R.string.menu),
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
                            text = stringResource(R.string.sort_mode),
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
                    text = stringResource(R.string.show_system_apps),
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
                    text = stringResource(R.string.sort_app_name),
                    selected = sortMode == SortMode.APP_NAME,
                    onClick = { onSortModeChange(SortMode.APP_NAME) }
                )
                MenuItemRow(
                    text = stringResource(R.string.sort_package_size),
                    selected = sortMode == SortMode.PACKAGE_SIZE,
                    onClick = { onSortModeChange(SortMode.PACKAGE_SIZE) }
                )
                MenuItemRow(
                    text = stringResource(R.string.sort_install_time),
                    selected = sortMode == SortMode.INSTALL_TIME,
                    onClick = { onSortModeChange(SortMode.INSTALL_TIME) }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                    thickness = 0.8.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                MenuItemRow(
                    text = stringResource(R.string.sort_descending),
                    selected = descending,
                    onClick = { onDescendingChange(!descending) }
                )
            }
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
                contentDescription = stringResource(R.string.search),
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
                                    text = stringResource(R.string.search),
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
                        text = stringResource(R.string.clear),
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
                text = stringResource(R.string.root_action_message),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text(
                    text = stringResource(R.string.deny),
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAllow) {
                Text(
                    text = stringResource(R.string.allow),
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
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        val stdout = process.inputStream.bufferedReader().use { it.readText() }
        val stderr = process.errorStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()

        when {
            exitCode == 0 && stdout.contains("uid=0") -> RootGrantState.GRANTED
            stderr.contains("not found", ignoreCase = true) -> RootGrantState.UNAVAILABLE
            else -> RootGrantState.DENIED
        }
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
