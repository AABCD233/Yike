package com.yike.focus.ui.screen.cycle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.PomodoroSettings
import com.yike.focus.ui.navigation3.LocalNavigator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

@Composable
fun CycleScreen() {
    val navigator = LocalNavigator.current
    var breakMin by remember { mutableStateOf(PomodoroSettings.breakMinutes) }
    var longBreakMin by remember { mutableStateOf(PomodoroSettings.longBreakMinutes) }
    var rounds by remember { mutableStateOf(PomodoroSettings.roundsBeforeLong) }
    var autoBreak by remember { mutableStateOf(PomodoroSettings.autoBreak) }
    var autoNext by remember { mutableStateOf(PomodoroSettings.autoNext) }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 6.dp, end = 16.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navigator.pop() }) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "返回", tint = colorScheme.onBackground)
            }
            Text("番茄循环", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    StepperRow(
                        title = "短休息",
                        summary = "每完成一个番茄后休息多久",
                        value = "$breakMin 分钟",
                        onMinus = { breakMin = (breakMin - 1).coerceAtLeast(1); PomodoroSettings.breakMinutes = breakMin },
                        onPlus = { breakMin = (breakMin + 1).coerceAtMost(60); PomodoroSettings.breakMinutes = breakMin },
                    )
                    StepperRow(
                        title = "长休息",
                        summary = "完成若干轮之后的那次长休息",
                        value = "$longBreakMin 分钟",
                        onMinus = {
                            longBreakMin = (longBreakMin - 5).coerceAtLeast(5)
                            PomodoroSettings.longBreakMinutes = longBreakMin
                        },
                        onPlus = {
                            longBreakMin = (longBreakMin + 5).coerceAtMost(120)
                            PomodoroSettings.longBreakMinutes = longBreakMin
                        },
                    )
                    StepperRow(
                        title = "几轮后长休息",
                        summary = "默认 4 个番茄一轮",
                        value = "$rounds 个",
                        onMinus = { rounds = (rounds - 1).coerceAtLeast(1); PomodoroSettings.roundsBeforeLong = rounds },
                        onPlus = { rounds = (rounds + 1).coerceAtMost(12); PomodoroSettings.roundsBeforeLong = rounds },
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title = "自动开始休息",
                        summary = "番茄结束后直接进休息倒计时，不用点按钮",
                        checked = autoBreak,
                        onCheckedChange = { autoBreak = it; PomodoroSettings.autoBreak = it },
                    )
                    SwitchPreference(
                        title = "休息后自动开始下一个",
                        summary = "休息结束直接接着上一个任务计时",
                        checked = autoNext,
                        onCheckedChange = { autoNext = it; PomodoroSettings.autoNext = it },
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("当前进度", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                        Text(
                            "本次会话已完成 ${PomodoroSettings.roundIndex} 个番茄，下一轮休息是${
                                if (PomodoroSettings.nextIsLongBreak) "长休息" else "短休息"
                            }",
                            fontSize = 14.sp,
                            color = colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepperRow(
    title: String,
    summary: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, color = colorScheme.onSurface)
            Text(summary, fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
        }
        IconButton(onClick = onMinus, modifier = Modifier.size(34.dp)) {
            Icon(Icons.Rounded.Remove, contentDescription = "减少", tint = colorScheme.onSurfaceVariantSummary)
        }
        Text(value, fontSize = 14.sp, color = colorScheme.onSurface, modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = onPlus, modifier = Modifier.size(34.dp)) {
            Icon(Icons.Rounded.Add, contentDescription = "增加", tint = colorScheme.onSurfaceVariantSummary)
        }
    }
}
