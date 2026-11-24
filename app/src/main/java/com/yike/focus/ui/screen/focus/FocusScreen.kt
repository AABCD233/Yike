package com.yike.focus.ui.screen.focus

import androidx.activity.compose.BackHandler
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.BoostSettings
import com.yike.focus.data.FocusLook
import com.yike.focus.data.FocusTimer
import com.yike.focus.data.MusicPlayer
import com.yike.focus.data.PomodoroSettings
import com.yike.focus.data.TodoStore
import com.yike.focus.data.Quotes
import com.yike.focus.ui.component.FilePickerDialog
import com.yike.focus.ui.component.miuix.ConfirmDialog
import com.yike.focus.ui.navigation3.Navigator
import com.yike.focus.ui.util.Notify
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import kotlinx.coroutines.delay

@Composable
fun FocusScreen(navController: Navigator) {
    val state by FocusTimer.state.collectAsState()
    var tick by remember { mutableIntStateOf(0) }
    var finishedTitle by remember { mutableStateOf<String?>(null) }
    var finishedSeconds by remember { mutableIntStateOf(0) }
    var showBackground by remember { mutableStateOf(false) }
    var showWhitelistTip by remember { mutableStateOf(false) }
    var showMusicPicker by remember { mutableStateOf(false) }
    var musicPath by remember { mutableStateOf(FocusLook.musicPath) }
    var aodMode by remember { mutableStateOf(false) }
    var aodOffset by remember { mutableStateOf(androidx.compose.ui.unit.IntOffset(0, 0)) }
    var bgPreset by remember { mutableStateOf(FocusLook.backgroundPreset) }
    var bgImage by remember { mutableStateOf(FocusLook.backgroundImage) }
    val bgBitmap = remember(bgImage) {
        bgImage?.let { path -> runCatching {
            BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = 2 })
        }.getOrNull()?.asImageBitmap() }
    }
    var quote by remember { mutableStateOf(Quotes.cached()) }
    var quoteFrom by remember { mutableStateOf(Quotes.cachedFrom()) }
    val context = LocalContext.current
    val pickBg = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val file = java.io.File(context.filesDir, "focus_bg.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { out -> input.copyTo(out) }
                }
                FocusLook.backgroundImage = file.absolutePath
                bgImage = file.absolutePath
                bgPreset = -1
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(250)
            tick++
        }
    }

    LaunchedEffect(Unit) {
        Quotes.fetch()?.let { (text, from) ->
            quote = text
            quoteFrom = from
        }
    }

    LaunchedEffect(aodMode) {
        while (aodMode) {
            delay(40_000)
            val x = (-20..20).random()
            val y = (-32..32).random()
            aodOffset = androidx.compose.ui.unit.IntOffset(x, y)
        }
    }

    val s = state

    if (s == null) {
        if (finishedTitle == null) {
            LaunchedEffect(Unit) { navController.pop() }
        } else {
            val wasRest = finishedTitle == "休息"
            ConfirmDialog(
                show = true,
                title = if (wasRest) "休息结束" else "完成一个番茄",
                summary = if (wasRest) {
                    "回来继续吧，今天已经完成 ${PomodoroSettings.roundIndex} 个番茄"
                } else {
                    "「$finishedTitle」专注 ${finishedSeconds / 60} 分钟，起来活动一下？"
                },
                confirmText = if (wasRest) "再来一个" else "休息一下",
                dismissText = "结束",
                onConfirm = {
                    val lastId = PomodoroSettings.lastTodoId
                    finishedTitle = null
                    if (wasRest) {
                        val todo = TodoStore.todo(lastId)
                        if (todo != null) {
                            FocusTimer.start(todo)
                        } else {
                            navController.pop()
                        }
                    } else {
                        val active = TodoStore.todo(lastId)
                        val rounds = active?.rounds ?: PomodoroSettings.roundsBeforeLong
                        val long = rounds > 0 && PomodoroSettings.roundIndex % rounds == 0
                        FocusTimer.startRest(
                            if (long) (active?.longBreakMin ?: PomodoroSettings.longBreakMinutes)
                            else (active?.breakMin ?: PomodoroSettings.breakMinutes),
                            long = long,
                        )
                    }
                },
                onDismiss = { navController.pop() },
            )
        }
        return
    }

    val elapsed = remember(tick, s) { FocusTimer.elapsedSeconds(s) }
    val remaining = remember(tick, s) { FocusTimer.remainingSeconds(s) }
    val total = if (s.isCountdown) s.totalSeconds.coerceAtLeast(1) else 0
    val progress = if (s.isCountdown) {
        (1f - remaining.toFloat() / total).coerceIn(0f, 1f)
    } else {
        ((elapsed % 3600).toFloat() / 3600f)
    }
    val isRest = s.kind == "relax" || s.kind == "rest_long"
    val strictMode = !isRest && BoostSettings.strict

    val presetColor = if (bgPreset in FocusLook.backgroundColors.indices) {
        Color(FocusLook.backgroundColors[bgPreset])
    } else Color.Transparent
    val background = when {
        bgBitmap != null -> Color.Black
        presetColor.alpha == 0f -> colorScheme.background
        else -> presetColor
    }
    val onBackground = if (bgBitmap != null) Color.White
    else if (background.luminance() > 0.55f) Color(0xFF1B1F24) else Color.White

    // 严格模式：返回手势也不能退出
    BackHandler(enabled = strictMode) { }

    LaunchedEffect(s.isCountdown, remaining, s.isPaused) {
        if (s.isCountdown && remaining == 0 && !s.isPaused) {
            val done = FocusTimer.stop(abandoned = false)
            MusicPlayer.stop()
            if (!isRest) {
                Notify.focusDone(context, s.title, done?.seconds ?: s.totalSeconds)
                PomodoroSettings.roundIndex = PomodoroSettings.roundIndex + 1
                finishedTitle = s.title
                finishedSeconds = done?.seconds ?: (s.totalSeconds - 1)
                if (PomodoroSettings.autoBreak) {
                    val active = TodoStore.todo(PomodoroSettings.lastTodoId)
                    val rounds = active?.rounds ?: PomodoroSettings.roundsBeforeLong
                    val long = rounds > 0 && PomodoroSettings.roundIndex % rounds == 0
                    val breakMin = if (long) {
                        active?.longBreakMin ?: PomodoroSettings.longBreakMinutes
                    } else {
                        active?.breakMin ?: PomodoroSettings.breakMinutes
                    }
                    FocusTimer.startRest(breakMin, long = long)
                    finishedTitle = null
                }
            } else {
                // 休息结束：开了「自动开始下一个」就直接接着上一个任务
                val lastTodo = TodoStore.todo(PomodoroSettings.lastTodoId)
                if (PomodoroSettings.autoNext && lastTodo != null) {
                    FocusTimer.start(lastTodo)
                } else {
                    finishedTitle = "休息"
                    finishedSeconds = done?.seconds ?: (s.totalSeconds - 1)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
    ) {
        bgBitmap?.let { bmp ->
            Image(
                bitmap = bmp,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            Box(
                modifier = Modifier.matchParentSize().background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.20f), Color.Black.copy(alpha = 0.55f))
                    )
                ),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f).padding(top = 6.dp)) {
                    Text(
                        "“",
                        color = onBackground.copy(alpha = 0.5f),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        quote,
                        color = onBackground.copy(alpha = 0.9f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    )
                    quoteFrom?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            "—— $it",
                            color = onBackground.copy(alpha = 0.55f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                IconButton(onClick = { showBackground = true }) {
                    Icon(Icons.Rounded.Palette, contentDescription = "背景", tint = onBackground.copy(alpha = 0.9f))
                }
                IconButton(onClick = { navController.push(com.yike.focus.ui.navigation3.Route.Music) }) {
                    Icon(Icons.Rounded.MusicNote, contentDescription = "背景音乐", tint = onBackground.copy(alpha = 0.9f))
                }
                IconButton(onClick = { navController.push(com.yike.focus.ui.navigation3.Route.WhitelistOpen) }) {
                    Icon(Icons.Rounded.Apps, contentDescription = "白名单", tint = onBackground.copy(alpha = 0.9f))
                }
            }

            Spacer(Modifier.weight(1f))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(240.dp)) {
                        val stroke = 6.dp.toPx()
                        val inset = stroke / 2
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        val trackColor = onBackground.copy(alpha = 0.22f)
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                        drawArc(
                            color = onBackground,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                    }
                    Text(
                        formatClock(if (s.isCountdown) remaining else elapsed),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Medium,
                        color = onBackground,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { aodMode = true }
                            .padding(8.dp),
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(s.title, color = onBackground, fontSize = 17.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    when {
                        s.isPaused -> "已暂停"
                        s.kind == "rest_long" -> "长休息"
                        isRest -> "短休息"
                        else -> "第 ${PomodoroSettings.roundIndex + 1} 个番茄"
                    },
                    color = onBackground.copy(alpha = 0.65f),
                    fontSize = 13.sp,
                )
            }
            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!strictMode) {
                    ActionPill(
                        text = if (s.isPaused) "继续" else "暂停",
                        onBackground = onBackground,
                        onClick = { if (s.isPaused) FocusTimer.resume() else FocusTimer.pause() },
                    )
                    Spacer(Modifier.size(12.dp))
                    ActionPill(text = "重开", onBackground = onBackground) { FocusTimer.restart() }
                    Spacer(Modifier.size(12.dp))
                }
                ActionPill(
                    text = if (strictMode && remaining > 0) "严格模式中" else "停止",
                    onBackground = onBackground,
                    primary = true,
                    onClick = {
                        if (strictMode && remaining > 0) return@ActionPill
                        MusicPlayer.stop()
                        FocusTimer.stop(abandoned = false)
                        navController.pop()
                    },
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }

    if (aodMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { aodMode = false },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                formatClock(if (s.isCountdown) remaining else elapsed),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 96.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.offset { aodOffset },
            )
        }
    }

    if (showMusicPicker) {
        FilePickerDialog(
            title = "背景音乐",
            onPick = { path ->
                musicPath = path
                FocusLook.musicPath = path
                MusicPlayer.start(path)
                showMusicPicker = false
            },
            onDismiss = { showMusicPicker = false },
        )
    }

    if (showBackground) {
        OverlayDialog(
            show = true,
            title = "背景",

            onDismissRequest = { showBackground = false },
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (bgBitmap != null) colorScheme.primaryContainer else Color.Transparent)
                        .clickable {
                            pickBg.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("从相册选一张图", fontSize = 14.sp, color = colorScheme.onSurface)
                }
                FocusLook.backgroundNames.forEachIndexed { index, name ->
                    val color = Color(FocusLook.backgroundColors[index])
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (index == bgPreset) colorScheme.primaryContainer else Color.Transparent)
                            .clickable {
                                bgPreset = index
                                FocusLook.backgroundPreset = index
                                FocusLook.backgroundImage = null
                                bgImage = null
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (color.alpha == 0f) colorScheme.surfaceVariant else color)
                                .border(1.dp, colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(name, fontSize = 14.sp, color = colorScheme.onSurface)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    ActionPill(text = "关闭", onBackground = colorScheme.onSurface) { showBackground = false }
                }
            }
        }
    }

}

@Composable
private fun ActionPill(
    text: String,
    onBackground: Color,
    primary: Boolean = false,
    onClick: () -> Unit,
) {
    val light = onBackground.luminance() < 0.5f
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (primary) onBackground else onBackground.copy(alpha = 0.14f))
            .clickable { onClick() }
            .padding(horizontal = 22.dp, vertical = 12.dp),
    ) {
        Text(
            text,
            color = if (primary) (if (light) Color.White else Color(0xFF1B1F24)) else onBackground,
            fontSize = 15.sp,
        )
    }
}

private fun formatClock(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
}
