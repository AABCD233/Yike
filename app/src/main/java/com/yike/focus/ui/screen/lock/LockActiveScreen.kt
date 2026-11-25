package com.yike.focus.ui.screen.lock

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.FocusTimer
import com.yike.focus.ui.util.Notify
import kotlinx.coroutines.delay

/** 锁机页：到时间才结束，不能提前退出 */
@Composable
fun LockActiveScreen(onExit: () -> Unit) {
    val state by FocusTimer.state.collectAsState()
    val view = LocalView.current
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }

    // 屏幕常亮；锁机期间不显示状态栏/导航栏
    LaunchedEffect(Unit) {
        view.keepScreenOn = true
        while (true) {
            delay(250)
            tick++
        }
    }

    BackHandler(enabled = true) { }

    val s = state
    if (s == null) {
        if (done) {
            AlertDialog(
                onDismissRequest = onExit,
                title = { Text("锁机结束") },
                text = { Text("时间到了，可以拿起手机了。") },
                confirmButton = { TextButton(onClick = onExit) { Text("好") } },
            )
        } else {
            LaunchedEffect(Unit) { onExit() }
        }
        return
    }

    val remaining = remember(tick, s) { FocusTimer.remainingSeconds(s) }

    LaunchedEffect(remaining, s.isCountdown) {
        if (s.isCountdown && remaining == 0) {
            FocusTimer.stop(abandoned = false)
            Notify.focusDone(context, "锁机", s.totalSeconds)
            done = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF10161E), Color(0xFF1C2836)))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(80.dp))
            Text("锁机中", fontSize = 16.sp, color = Color.White.copy(alpha = 0.6f))
            Spacer(Modifier.height(24.dp))
            Text(
                formatClock(remaining),
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "放下手机，做点别的事；退出会回到这里",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

private fun formatClock(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
}
