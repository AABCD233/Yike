package com.yike.focus.ui.screen.finish

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.yike.focus.data.FinishEvent
import com.yike.focus.data.FocusTimer
import com.yike.focus.data.PomodoroSettings
import com.yike.focus.data.TodoStore
import com.yike.focus.ui.component.miuix.ConfirmDialog
import com.yike.focus.ui.navigation3.Navigator
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

/** 在后台完成时，被拉起来显示的那一屏：只负责弹完成框 */
@Composable
fun FinishScreen(navController: Navigator) {
    var finished by remember { mutableStateOf(FinishEvent.pending) }

    LaunchedEffect(Unit) {
        if (finished == null) navController.pop()
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize().background(colorScheme.background),
    ) {
        finished?.let { info ->
            ConfirmDialog(
                show = true,
                title = if (info.isRest) "休息结束" else "完成一个番茄",
                summary = if (info.isRest) {
                    "回来继续吧，今天已经完成 ${PomodoroSettings.roundIndex} 个番茄"
                } else {
                    "「${info.title}」专注 ${info.seconds / 60} 分钟，起来活动一下？"
                },
                confirmText = if (info.isRest) "再来一个" else "休息一下",
                dismissText = "关闭",
                onConfirm = {
                    finished = null
                    FinishEvent.pending = null
                    if (info.isRest) {
                        TodoStore.todo(PomodoroSettings.lastTodoId)?.let { FocusTimer.start(it) }
                        navController.replace(com.yike.focus.ui.navigation3.Route.Focus)
                    } else {
                        FocusTimer.startRest(PomodoroSettings.breakMinutes)
                        navController.replace(com.yike.focus.ui.navigation3.Route.Focus)
                    }
                },
                onDismiss = {
                    finished = null
                    FinishEvent.pending = null
                    navController.pop()
                },
            )
        }
    }
}
