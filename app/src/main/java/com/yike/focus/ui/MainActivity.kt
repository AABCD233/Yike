package com.yike.focus.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults.flingBehavior
import androidx.compose.foundation.pager.PagerDefaults.pageNestedScrollConnection
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.flow.MutableStateFlow
import com.yike.focus.ui.component.bottombar.BottomBar
import com.yike.focus.ui.component.bottombar.MainPagerState
import com.yike.focus.ui.component.bottombar.SideRail
import com.yike.focus.ui.component.bottombar.rememberMainPagerState
import com.yike.focus.ui.component.bottombar.useNavigationRail
import com.yike.focus.ui.navigation3.LocalNavigator
import com.yike.focus.ui.navigation3.Navigator
import com.yike.focus.ui.navigation3.Route
import com.yike.focus.ui.navigation3.rememberNavigator
import com.yike.focus.ui.screen.about.AboutScreen
import com.yike.focus.ui.screen.colorpalette.ColorPaletteScreen
import com.yike.focus.ui.screen.boost.BoostScreen
import com.yike.focus.ui.screen.finish.FinishScreen
import com.yike.focus.ui.screen.focussettings.FocusSettingsScreen
import com.yike.focus.ui.screen.focus.FocusScreen
import com.yike.focus.ui.screen.history.HistoryScreen
import com.yike.focus.ui.screen.lock.LockActiveScreen
import com.yike.focus.ui.screen.lock.LockScreen
import com.yike.focus.ui.screen.me.MeScreen
import com.yike.focus.ui.screen.music.MusicScreen
import com.yike.focus.ui.screen.stats.StatsScreen
import com.yike.focus.ui.screen.whitelist.WhitelistOpenScreen
import com.yike.focus.ui.screen.whitelist.WhitelistScreen
import com.yike.focus.ui.screen.todo.TodoScreen
import com.yike.focus.ui.screen.todoset.TodoSetScreen
import com.yike.focus.ui.screen.permission.PermissionScreen
import com.yike.focus.ui.screen.settings.SettingPager
import com.yike.focus.ui.theme.TemplateTheme
import com.yike.focus.ui.theme.LocalColorMode
import com.yike.focus.ui.theme.LocalEnableBlur
import com.yike.focus.ui.theme.LocalEnableFloatingBottomBar
import com.yike.focus.ui.theme.LocalEnableFloatingBottomBarBlur
import com.yike.focus.ui.util.rememberBlurBackdrop
import com.yike.focus.ui.util.rememberContentReady
import com.yike.focus.ui.viewmodel.MainActivityViewModel
import com.yike.focus.data.FocusTimer
import com.yike.focus.ui.util.Notify
import com.yike.focus.ui.viewmodel.MainPagerConfig
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.utils.PagerGestureNestedScrollConnection
import top.yukonga.miuix.kmp.utils.PagerInterceptionMode
import top.yukonga.miuix.kmp.utils.PagerNavigationSpringSpec
import top.yukonga.miuix.kmp.utils.pagerGestureOverride

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_FOCUS = "open_focus"
        const val EXTRA_SHOW_FINISH = "show_finish"
    }

    private val intentState = MutableStateFlow(0)

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val appContext = LocalContext.current
            var onboarded by remember {
                mutableStateOf(
                    appContext.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
                        .getBoolean("onboarded", false)
                )
            }
            if (!onboarded) {
                com.yike.focus.ui.screen.setup.PermissionSetupScreen(
                    onDone = {
                        appContext.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
                            .edit().putBoolean("onboarded", true).apply()
                        onboarded = true
                    },
                )
                return@setContent
            }
            RequestNotificationPermission()
            FocusLeavingReminder()
            DesktopGuard()
            AppForegroundTracker()
            val viewModel = viewModel<MainActivityViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val selectedMainPage by viewModel.selectedMainPage.collectAsStateWithLifecycle()
            val appSettings = uiState.appSettings
            val uiMode = uiState.uiMode
            val darkMode = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && isSystemInDarkTheme())

            DisposableEffect(darkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkMode },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkMode },
                )
                window.isNavigationBarContrastEnforced = false
                onDispose { }
            }

            val navigator = rememberNavigator(Route.Main)
            val openFocus = intent?.getBooleanExtra(EXTRA_OPEN_FOCUS, false) == true
            LaunchedEffect(openFocus) {
                if (openFocus) navigator.push(Route.Focus)
            }
            val showFinish = intent?.getBooleanExtra(EXTRA_SHOW_FINISH, false) == true
            LaunchedEffect(showFinish) {
                if (showFinish) navigator.push(Route.Finish)
            }
            val runningFocus by FocusTimer.state.collectAsStateWithLifecycle()
            val context = LocalContext.current
            LaunchedEffect(runningFocus != null, runningFocus?.kind) {
                val running = runningFocus
                if (running != null && (running.kind == "focus" || running.kind == "lock")) {
                    if (com.yike.focus.data.BoostSettings.dnd) com.yike.focus.data.Dnd.enable()
                } else {
                    com.yike.focus.data.Dnd.disable()
                }
                val active = runningFocus?.kind == "focus"
                runCatching {
                    val am = context.getSystemService(android.app.ActivityManager::class.java)
                    am?.appTasks?.firstOrNull()
                        ?.setExcludeFromRecents(active && com.yike.focus.data.BoostSettings.hideRecents)
                }
                if (active && (com.yike.focus.data.BoostSettings.hardcore || com.yike.focus.data.BoostSettings.blockDesktop)) {
                    com.yike.focus.service.GuardService.start(context)
                } else {
                    com.yike.focus.service.GuardService.stop(context)
                }
            }
            val systemDensity = LocalDensity.current
            val density = remember(systemDensity, uiState.pageScale) {
                Density(systemDensity.density * uiState.pageScale, systemDensity.fontScale)
            }

            CompositionLocalProvider(
                LocalNavigator provides navigator,
                LocalDensity provides density,
                LocalColorMode provides appSettings.colorMode.value,
                LocalEnableBlur provides uiState.enableBlur,
                LocalEnableFloatingBottomBar provides uiState.enableFloatingBottomBar,
                LocalEnableFloatingBottomBarBlur provides uiState.enableFloatingBottomBarBlur,
                LocalUiMode provides uiMode,
            ) {
                TemplateTheme(appSettings = appSettings, uiMode = uiMode) {
                    val swipeDismiss = if (uiState.enableSwipeDismiss) {
                        if (LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl) {
                            NavSwipeDirection.RightToLeft
                        } else {
                            NavSwipeDirection.LeftToRight
                        }
                    } else {
                        NavSwipeDirection.None
                    }
                    val mainScreenEntry = @Composable {
                        MainScreen(
                            initialPage = selectedMainPage,
                            pagerInterceptionMode = uiState.pagerInterceptionMode,
                            onPageChanged = viewModel::setSelectedMainPage,
                        )
                    }

                    val navDisplay = @Composable {
                        NavDisplay(
                            backStack = navigator.backStack,
                            effects = NavDisplayEffects(cornerClipRadius = rememberNavSystemCornerRadius()),
                            onBack = {
                                navigator.pop()
                            },
                        ) {
                            entry<Route.Main>(swipeDismiss = swipeDismiss) { mainScreenEntry() }
                            entry<Route.About>(swipeDismiss = swipeDismiss) { AboutScreen() }
                            entry<Route.ColorPalette>(swipeDismiss = swipeDismiss) { ColorPaletteScreen() }
                            entry<Route.Permissions>(swipeDismiss = swipeDismiss) { PermissionScreen() }
                            entry<Route.Home>(swipeDismiss = swipeDismiss) { mainScreenEntry() }
                            entry<Route.Settings>(swipeDismiss = swipeDismiss) { SettingPager(navigator, androidx.compose.ui.unit.Dp(0f)) }
                            entry<Route.Focus>(swipeDismiss = NavSwipeDirection.None) { FocusScreen(navigator) }
                            entry<Route.History>(swipeDismiss = swipeDismiss) { HistoryScreen(navigator) }
                            entry<Route.LockActive>(swipeDismiss = NavSwipeDirection.None) { LockActiveScreen(onExit = { navigator.pop() }) }
                            entry<Route.Whitelist>(swipeDismiss = swipeDismiss) { WhitelistScreen(navigator) }
                            entry<Route.WhitelistOpen>(swipeDismiss = swipeDismiss) { WhitelistOpenScreen(navigator) }
                            entry<Route.Boost>(swipeDismiss = swipeDismiss) { BoostScreen() }
                            entry<Route.Music>(swipeDismiss = swipeDismiss) { MusicScreen(navigator) }
                            entry<Route.Finish>(swipeDismiss = NavSwipeDirection.None) { FinishScreen(navigator) }
                            entry<Route.FocusSettings>(swipeDismiss = swipeDismiss) { FocusSettingsScreen() }
                        }
                    }

                    when (uiMode) {
                        UiMode.Material -> androidx.compose.material3.Scaffold { navDisplay() }
                        UiMode.Miuix -> Scaffold { navDisplay() }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Increment intentState to trigger LaunchedEffect re-execution
        intentState.value += 1
    }
}

@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@Composable
private fun AppForegroundTracker() {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> com.yike.focus.data.AppState.foreground = true
                Lifecycle.Event.ON_STOP -> com.yike.focus.data.AppState.foreground = false
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        com.yike.focus.data.AppState.foreground = true
        onDispose { lifecycle.removeObserver(observer) }
    }
}

/**
 * 「禁止桌面和多任务」：页面一停就延迟 800ms 看一眼前台，
 * 不是白名单应用就把自己拉回来。依赖悬浮窗权限，Android 10+ 才允许后台启动 Activity。
 */
@Composable
private fun DesktopGuard() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_STOP) return@LifecycleEventObserver
            val running = FocusTimer.state.value ?: return@LifecycleEventObserver
            if (running.kind != "focus" || running.isPaused) return@LifecycleEventObserver
            if (!com.yike.focus.data.BoostSettings.blockDesktop) return@LifecycleEventObserver
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val foreground = com.yike.focus.data.AppGuard.foregroundApp() ?: return@postDelayed
                val pkg = foreground.first
                val onHome = pkg == com.yike.focus.data.AppGuard.homePackage()
                val allowed = pkg == context.packageName ||
                    (com.yike.focus.data.AppGuard.isExempt(pkg) && !onHome) ||
                    com.yike.focus.data.AppGuard.whitelist().contains(pkg)
                if (allowed) return@postDelayed
                runCatching {
                    context.startActivity(
                        android.content.Intent(context, MainActivity::class.java).apply {
                            addFlags(
                                android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                                    android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                                    android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                            )
                        }
                    )
                }
            }, 800)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}

/** 学霸模式的「提醒模式」：计时中离开应用就推一条通知 */
@Composable
private fun FocusLeavingReminder() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                val running = FocusTimer.state.value
                if (running != null && running.kind == "focus" && !running.isPaused) {
                    // 20 秒后再看一眼前台应用：如果用户在白名单应用里，就别打扰
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        val pkg = com.yike.focus.data.AppGuard.foregroundPackage()
                        val allowed = pkg == null ||
                            com.yike.focus.data.AppGuard.isExempt(pkg) ||
                            com.yike.focus.data.AppGuard.whitelist().contains(pkg)
                        if (!allowed) Notify.leavingFocus(context, running.title)
                    }, 20_000)
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}

val LocalMainPagerState = staticCompositionLocalOf<MainPagerState> { error("LocalMainPagerState not provided") }

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    initialPage: Int = 0,
    pagerInterceptionMode: Int = PagerInterceptionMode.CrossAxisInterceptor.ordinal,
    onPageChanged: (Int) -> Unit = {},
) {
    val navController = LocalNavigator.current
    val enableBlur = LocalEnableBlur.current
    val enableFloatingBottomBar = LocalEnableFloatingBottomBar.current
    val enableFloatingBottomBarBlur = LocalEnableFloatingBottomBarBlur.current
    val useNavigationRail = useNavigationRail(enableFloatingBottomBar)
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { MainPagerConfig.PAGE_COUNT })
    val mainPagerState = rememberMainPagerState(pagerState, animatePageChanges = !useNavigationRail)
    val pagerMode = PagerInterceptionMode.entries.getOrElse(pagerInterceptionMode) { PagerInterceptionMode.Native }
    val interceptPagerGestures = pagerMode == PagerInterceptionMode.CrossAxisInterceptor
    val uiMode = LocalUiMode.current
    val surfaceColor = when (uiMode) {
        UiMode.Material -> MaterialTheme.colorScheme.surface // Blur is not used in Material, this is just a placeholder
        UiMode.Miuix -> MiuixTheme.colorScheme.surface
    }
    val blurBackdrop = rememberBlurBackdrop(enableBlur)

    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }

    LaunchedEffect(com.yike.focus.data.RoundEvent.requestPage) {
        com.yike.focus.data.RoundEvent.requestPage?.let { page ->
            mainPagerState.animateToPage(page)
            com.yike.focus.data.RoundEvent.requestPage = null
        }
    }

    val settledPage = mainPagerState.pagerState.settledPage
    LaunchedEffect(settledPage) {
        onPageChanged(settledPage)
    }

    val currentPage = mainPagerState.pagerState.currentPage
    LaunchedEffect(currentPage) {
        mainPagerState.syncPage()
    }

    MainScreenBackHandler(mainPagerState, navController)

    CompositionLocalProvider(
        LocalMainPagerState provides mainPagerState
    ) {
        val contentReady = rememberContentReady()
        val pagerContent = @Composable { bottomInnerPadding: Dp ->
            Box(modifier = if (blurBackdrop != null) Modifier.layerBackdrop(blurBackdrop) else Modifier) {
                HorizontalPager(
                    modifier = Modifier
                        .pagerGestureOverride(pagerState = mainPagerState.pagerState, mode = pagerMode)
                        .then(if (enableFloatingBottomBar && enableFloatingBottomBarBlur) Modifier.layerBackdrop(backdrop) else Modifier),
                    state = mainPagerState.pagerState,
                    beyondViewportPageCount = if (contentReady) 1 else 0,
                    overscrollEffect = null,
                    userScrollEnabled = !interceptPagerGestures,
                    pageNestedScrollConnection = if (interceptPagerGestures) {
                        PagerGestureNestedScrollConnection
                    } else {
                        pageNestedScrollConnection(
                            state = mainPagerState.pagerState,
                            orientation = androidx.compose.foundation.gestures.Orientation.Horizontal,
                        )
                    },
                    flingBehavior = flingBehavior(
                        state = mainPagerState.pagerState,
                        snapAnimationSpec = PagerNavigationSpringSpec,
                    ),
                ) { page ->
                    val isCurrentPage = page == settledPage
                    when (page) {
                        0 -> if (isCurrentPage || contentReady) TodoScreen(navController, bottomInnerPadding)
                        1 -> if (isCurrentPage || contentReady) TodoSetScreen(navController, bottomInnerPadding)
                        2 -> if (isCurrentPage || contentReady) LockScreen(navController, bottomInnerPadding)
                        3 -> if (isCurrentPage || contentReady) StatsScreen(navController, bottomInnerPadding)
                        4 -> if (isCurrentPage || contentReady) MeScreen(navController, bottomInnerPadding)
                    }
                }
            }
        }

        if (useNavigationRail) {
            val startInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                .only(WindowInsetsSides.Start)
            val navBarBottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

            when (uiMode) {
                UiMode.Material -> androidx.compose.material3.Scaffold {
                    Row {
                        SideRail()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(startInsets)
                        ) {
                            pagerContent(navBarBottomPadding)
                        }
                    }
                }

                UiMode.Miuix -> Scaffold { _ ->
                    Row {
                        SideRail()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(startInsets)
                        ) {
                            pagerContent(navBarBottomPadding)
                        }
                    }
                }
            }
        } else {
            val bottomBar = @Composable {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BottomBar(
                        blurBackdrop = blurBackdrop,
                        backdrop = backdrop,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }

            when (uiMode) {
                UiMode.Material -> androidx.compose.material3.Scaffold(bottomBar = bottomBar) { innerPadding ->
                    pagerContent(innerPadding.calculateBottomPadding())
                }

                UiMode.Miuix -> Scaffold(bottomBar = bottomBar) { innerPadding ->
                    pagerContent(innerPadding.calculateBottomPadding())
                }
            }
        }
    }
}

@Composable
private fun MainScreenBackHandler(
    mainState: MainPagerState,
    navController: Navigator,
) {
    val isPagerBackHandlerEnabled by remember {
        derivedStateOf {
            navController.current() is Route.Main && navController.backStackSize() == 1 && mainState.selectedPage != 0
        }
    }

    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = isPagerBackHandlerEnabled,
        onBackCompleted = {
            mainState.animateToPage(0)
        }
    )
}
