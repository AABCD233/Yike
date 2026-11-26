package com.yike.focus.ui.screen.focussettings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
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
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

@Composable
fun FocusSettingsScreen() {
    val navigator = LocalNavigator.current
    var autoBreak by remember { mutableStateOf(PomodoroSettings.autoBreak) }
    var autoNext by remember { mutableStateOf(PomodoroSettings.autoNext) }
    var reminderService by remember { mutableStateOf(PomodoroSettings.reminderService) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var antiBurn by remember { mutableStateOf(com.yike.focus.data.AodSettings.antiBurn) }
    var shiftMinutes by remember { mutableStateOf(com.yike.focus.data.AodSettings.shiftMinutes) }
    var weight by remember { mutableStateOf(com.yike.focus.data.AodSettings.fontWeight) }
    var showShift by remember { mutableStateOf(false) }
    var showWeight by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 6.dp, end = 16.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navigator.pop() }) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "返回", tint = colorScheme.onBackground)
            }
            Text("专注设置", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
        }

        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            SwitchPreference(
                title = "常驻提醒服务",
                summary = "默认用系统闹钟，不需要常驻。开这个会常驻一个轻量进程，提醒更准时，代价是通知栏有一条静音通知",
                checked = reminderService,
                onCheckedChange = { on ->
                    reminderService = on
                    PomodoroSettings.reminderService = on
                    if (on) com.yike.focus.service.ReminderService.start(context)
                    else com.yike.focus.service.ReminderService.stop(context)
                },
            )
            SwitchPreference(
                title = "自动开始休息",
                summary = "番茄结束直接进休息倒计时，不用再点一下",
                checked = autoBreak,
                onCheckedChange = { autoBreak = it; PomodoroSettings.autoBreak = it },
            )
            SwitchPreference(
                title = "休息后自动继续",
                summary = "休息结束直接接着上一个任务开始下一个番茄",
                checked = autoNext,
                onCheckedChange = { autoNext = it; PomodoroSettings.autoNext = it },
            )
        }

        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column {
                SwitchPreference(
                    title = "熄屏时钟防烧屏",
                    summary = "开着时钟会慢慢移动位置，避免屏幕烧出痕迹",
                    checked = antiBurn,
                    onCheckedChange = { antiBurn = it; com.yike.focus.data.AodSettings.antiBurn = it },
                )
                ArrowPreference(
                    title = "移位间隔",
                    summary = "${shiftMinutes} 分钟移动一次",
                    onClick = { showShift = true },
                )
                ArrowPreference(
                    title = "时钟字体粗细",
                    summary = com.yike.focus.data.AodSettings.weightNames[weight],
                    onClick = { showWeight = true },
                )
            }
        }

        Text(
            "待办的计划轮数在每条待办的高级设置里改；轮数完成后会回到待办页弹完成提示。",
            fontSize = 12.sp,
            color = colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }

    if (showShift) {
        top.yukonga.miuix.kmp.overlay.OverlayDialog(
            show = true,
            title = "移位间隔",
            summary = "间隔越短越不容易烧屏，但移动也越频繁",
            onDismissRequest = { showShift = false },
        ) {
            Column {
                com.yike.focus.data.AodSettings.shiftChoices.forEach { m ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (m == shiftMinutes) colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .clickable {
                                shiftMinutes = m
                                com.yike.focus.data.AodSettings.shiftMinutes = m
                                showShift = false
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("$m 分钟", fontSize = 14.sp, color = colorScheme.onSurface)
                    }
                }
            }
        }
    }

    if (showWeight) {
        top.yukonga.miuix.kmp.overlay.OverlayDialog(
            show = true,
            title = "时钟字体粗细",
            onDismissRequest = { showWeight = false },
        ) {
            Column {
                com.yike.focus.data.AodSettings.weightNames.forEachIndexed { index, name ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (index == weight) colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .clickable {
                                weight = index
                                com.yike.focus.data.AodSettings.fontWeight = index
                                showWeight = false
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(name, fontSize = 14.sp, color = colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
