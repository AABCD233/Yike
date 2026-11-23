package com.tomato.focus.ui.screen.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.focus.data.FocusTimer
import com.tomato.focus.ui.navigation3.Navigator
import com.tomato.focus.ui.util.Notify
import kotlinx.coroutines.delay

private val QUOTES = listOf(
    "珍惜一切的好办法是意识到你可能会失去它。",
    "每一个不曾起舞的日子，都是对生命的辜负。",
    "把注意力放在你能控制的事情上。",
    "今天的专注，是明天的底气。",
)

@Composable
fun FocusScreen(navController: Navigator) {
    val state by FocusTimer.state.collectAsState()
    var tick by remember { mutableIntStateOf(0) }
    var finishedTitle by remember { mutableStateOf<String?>(null) }
    var finishedSeconds by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            delay(250)
            tick++
        }
    }

    val s = state

    if (s == null) {
        if (finishedTitle == null) {
            LaunchedEffect(Unit) { navController.pop() }
        } else {
            AlertDialog(
                onDismissRequest = { navController.pop() },
                title = { Text("完成一个番茄") },
                text = { Text("「$finishedTitle」专注 ${finishedSeconds / 60} 分钟，起来活动一下？") },
                confirmButton = {
                    TextButton(onClick = {
                        finishedTitle = null
                        FocusTimer.startRest(5)
                    }) { Text("休息 5 分钟") }
                },
                dismissButton = {
                    TextButton(onClick = { navController.pop() }) { Text("先不休息") }
                },
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

    // 倒计时归零：落库、发通知，然后停在完成提示
    LaunchedEffect(s.isCountdown, remaining, s.isPaused) {
        if (s.isCountdown && remaining == 0 && !s.isPaused) {
            val done = FocusTimer.stop(abandoned = false)
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
            .background(
                if (isRest) {
                    Brush.verticalGradient(listOf(Color(0xFF2F7D6B), Color(0xFF54A592), Color(0xFF8CC7B4)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF3D5A80), Color(0xFF5C7FA8), Color(0xFF87A8C8)))
                }
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.pop() }) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "返回", tint = Color.White)
                }
                Text(
                    if (isRest) "休息一下，等会儿继续" else quote,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f),
                )
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
                        drawArc(
                            color = Color.White.copy(alpha = 0.25f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                        drawArc(
                            color = Color.White,
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
                ActionPill(
                    text = if (s.isPaused) "继续" else "暂停",
                    onClick = { if (s.isPaused) FocusTimer.resume() else FocusTimer.pause() },
                )
                Spacer(Modifier.size(12.dp))
                ActionPill(
                    text = "放弃",
                    onClick = {
                        FocusTimer.stop(abandoned = true)
                        navController.pop()
                    },
                )
                Spacer(Modifier.size(12.dp))
                ActionPill(
                    text = "结束",
                    primary = true,
                    onClick = {
                        FocusTimer.stop(abandoned = false)
                        navController.pop()
                    },
                )
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ActionPill(text: String, primary: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (primary) Color.White else Color.White.copy(alpha = 0.18f))
            .clickable { onClick() }
            .padding(horizontal = 26.dp, vertical = 12.dp),
    ) {
        Text(
            text,
            color = if (primary) Color(0xFF3D5A80) else Color.White,
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
