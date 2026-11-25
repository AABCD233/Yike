package com.yike.focus.ui.screen.lock

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.FocusTimer
import com.yike.focus.ui.navigation3.Navigator
import com.yike.focus.ui.util.Notify
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@Composable
fun LockActiveScreen(navController: Navigator) {
    val state by FocusTimer.state.collectAsState()
    val view = LocalView.current
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    var askQuit by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }

    // 锁机期间屏幕常亮，并进入系统「屏幕固定」，上滑回桌面会被拉回这个页面
    DisposableEffect(Unit) {
        val activity = view.context as? android.app.Activity
        runCatching { activity?.startLockTask() }
        onDispose { runCatching { activity?.stopLockTask() } }
    }

    LaunchedEffect(Unit) {
        view.keepScreenOn = true
        while (true) {
            delay(250)
            tick++
        }
    }

    // 返回键在这里不生效：要退出只能点「强行结束」
    BackHandler(enabled = true) { }

    val s = state
    if (s == null) {
        if (done) {
            AlertDialog(
                onDismissRequest = { navController.pop() },
                title = { Text("锁机结束") },
                text = { Text("时间到了，可以拿起手机了。") },
                confirmButton = { TextButton(onClick = { navController.pop() }) { Text("好") } },
            )
        } else {
            LaunchedEffect(Unit) { navController.pop() }
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
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(60.dp))
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
                "这期间放下手机；上滑回桌面也会被拉回这里",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
            )
            Spacer(Modifier.weight(1f))
            Text(
                "强行结束",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { askQuit = true }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
            Spacer(Modifier.height(40.dp))
        }
    }

    if (askQuit) {
        AlertDialog(
            onDismissRequest = { askQuit = false },
            title = { Text("确定要结束锁机吗") },
            text = { Text("提前结束会记为一次放弃，记录里能看到。") },
            confirmButton = {
                TextButton(onClick = {
                    askQuit = false
                    FocusTimer.stop(abandoned = true)
                    navController.pop()
                }) { Text("结束") }
            },
            dismissButton = {
                TextButton(onClick = { askQuit = false }) { Text("继续锁机") }
            },
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
