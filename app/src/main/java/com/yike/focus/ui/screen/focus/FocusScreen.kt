package com.yike.focus.ui.screen.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.audio.NoisePlayer
import com.yike.focus.data.BoostSettings
import com.yike.focus.data.FocusLook
import com.yike.focus.data.FocusTimer
import com.yike.focus.ui.component.miuix.ConfirmDialog
import com.yike.focus.ui.navigation3.Navigator
import com.yike.focus.ui.util.Notify
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import kotlinx.coroutines.delay

private val QUOTES = listOf(
    "珍惜一切的好办法是意识到你可能会失去它。",
    "每一个不曾起舞的日子，都是对生命的辜负。",
    "把注意力放在你能控制的事情上。",
    "今天的专注，是明天的底气。",
    "臣书刷字墨淋漓，舒卷烟云势最奇。",
)

private val GRADIENTS = listOf(
    listOf(Color(0xFF8AC5F2), Color(0xFFB9A6F0)), // 晨雾
    listOf(Color(0xFF5C7FA8), Color(0xFF3D5A80)), // 暮色
    listOf(Color(0xFF7FC8A9), Color(0xFF3E8C6A)), // 林间
    listOf(Color(0xFF6FC6DA), Color(0xFF3F8FB0)), // 海边
    listOf(Color(0xFF2B3245), Color(0xFF141A26)), // 夜灯
    listOf(Color(0xFFE8EDF2), Color(0xFFCBD6DF)), // 素白
)

private val NOISE_OPTIONS = listOf(
    "none" to "不放声音",
    "white" to "白噪音",
    "pink" to "粉噪音（低频更厚）",
    "brown" to "棕噪音（更闷）",
)

@Composable
fun FocusScreen(navController: Navigator) {
    val state by FocusTimer.state.collectAsState()
    var tick by remember { mutableIntStateOf(0) }
    var finishedTitle by remember { mutableStateOf<String?>(null) }
    var finishedSeconds by remember { mutableIntStateOf(0) }
    var showBackground by remember { mutableStateOf(false) }
    var showMusic by remember { mutableStateOf(false) }
    var showWhitelistTip by remember { mutableStateOf(false) }
    var bgPreset by remember { mutableStateOf(FocusLook.backgroundPreset) }
    var noiseKind by remember { mutableStateOf(FocusLook.noiseKind) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            delay(250)
            tick++
        }
    }

    // 离开页面就把噪音停掉
    DisposableEffect(Unit) {
        onDispose { NoisePlayer.stop() }
    }

    val s = state

    if (s == null) {
        if (finishedTitle == null) {
            LaunchedEffect(Unit) { navController.pop() }
        } else {
            ConfirmDialog(
                show = true,
                title = "完成一个番茄",
                summary = "「$finishedTitle」专注 ${finishedSeconds / 60} 分钟，起来活动一下？",
                confirmText = "休息 5 分钟",
                dismissText = "先不休息",
                onConfirm = {
                    finishedTitle = null
                    FocusTimer.startRest(5)
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
    val quote = remember(s.startWall) { QUOTES[(s.startWall / 1000 % QUOTES.size).toInt()] }
    val isRest = s.kind == "relax"
    val strictMode = !isRest && BoostSettings.strict
    val gradient = GRADIENTS[bgPreset.coerceIn(GRADIENTS.indices)]

    LaunchedEffect(s.isCountdown, remaining, s.isPaused) {
        if (s.isCountdown && remaining == 0 && !s.isPaused) {
            val done = FocusTimer.stop(abandoned = false)
            NoisePlayer.stop()
            if (!isRest) {
                Notify.focusDone(context, s.title, done?.seconds ?: s.totalSeconds)
            }
            finishedTitle = if (isRest) "休息" else s.title
            finishedSeconds = done?.seconds ?: (s.totalSeconds - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradient)),
    ) {
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
                        quote,
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    )
                }
                FocusIconButton(Icons.Rounded.Palette, "背景") { showBackground = true }
                FocusIconButton(Icons.Rounded.MusicNote, "音乐") { showMusic = true }
                FocusIconButton(Icons.Rounded.Apps, "白名单") { showWhitelistTip = true }
            }

            Spacer(Modifier.weight(1f))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val ringColor = Color.White
                val trackColor = Color.White.copy(alpha = 0.28f)
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(240.dp)) {
                        val stroke = 6.dp.toPx()
                        val inset = stroke / 2
                        val arcSize = Size(size.width - stroke, size.height - stroke)
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
                            color = ringColor,
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
                        color = Color.White,
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(s.title, color = Color.White, fontSize = 17.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (s.isPaused) "已暂停" else if (isRest) "休息中" else "进行中",
                    color = Color.White.copy(alpha = 0.75f),
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
                        onClick = { if (s.isPaused) FocusTimer.resume() else FocusTimer.pause() },
                    )
                    Spacer(Modifier.size(12.dp))
                    ActionPill(text = "重开") { FocusTimer.restart() }
                    Spacer(Modifier.size(12.dp))
                }
                ActionPill(
                    text = if (strictMode && remaining > 0) "严格模式" else "停止",
                    primary = true,
                    onClick = {
                        if (strictMode && remaining > 0) return@ActionPill
                        NoisePlayer.stop()
                        FocusTimer.stop(abandoned = false)
                        navController.pop()
                    },
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }

    if (showBackground) {
        OverlayDialog(
            show = true,
            title = "背景",
            summary = "选一个专注时看的底色",
            onDismissRequest = { showBackground = false },
        ) {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GRADIENTS.forEachIndexed { index, colors ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.verticalGradient(colors))
                                .then(
                                    if (index == bgPreset) Modifier.border(2.dp, colorScheme.primary, RoundedCornerShape(12.dp))
                                    else Modifier
                                )
                                .clickable {
                                    bgPreset = index
                                    FocusLook.backgroundPreset = index
                                },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(FocusLook.backgroundNames.getOrElse(bgPreset) { "" }, fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    ActionPill(text = "关闭", onClick = { showBackground = false })
                }
            }
        }
    }

    if (showWhitelistTip) {
        ConfirmDialog(
            show = true,
            title = "白名单",
            summary = "白名单应用管理还没做，等硬核模式接好一起放出来。",
            confirmText = "知道了",
            dismissText = null,
            onConfirm = { showWhitelistTip = false },
            onDismiss = { showWhitelistTip = false },
        )
    }

    if (showMusic) {
        OverlayDialog(
            show = true,
            title = "背景音",
            summary = "实时生成的噪音，不占安装包体积",
            onDismissRequest = { showMusic = false },
        ) {
            Column {
                NOISE_OPTIONS.forEach { (kind, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (kind == noiseKind) colorScheme.primaryContainer else Color.Transparent)
                            .clickable {
                                noiseKind = kind
                                FocusLook.noiseKind = kind
                                if (kind == "none") NoisePlayer.stop() else NoisePlayer.start(kind)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(label, fontSize = 14.sp, color = colorScheme.onSurface)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    ActionPill(text = "关闭", onClick = { showMusic = false })
                }
            }
        }
    }
}

@Composable
private fun FocusIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = description, tint = Color.White.copy(alpha = 0.9f))
    }
}

@Composable
private fun ActionPill(text: String, primary: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (primary) Color.White else Color.White.copy(alpha = 0.18f))
            .clickable { onClick() }
            .padding(horizontal = 22.dp, vertical = 12.dp),
    ) {
        Text(
            text,
            color = if (primary) Color(0xFF2A3244) else Color.White,
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
