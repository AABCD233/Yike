package com.yike.focus.ui.screen.focussettings

import androidx.compose.foundation.background
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
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

@Composable
fun FocusSettingsScreen() {
    val navigator = LocalNavigator.current
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
            Text("专注设置", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
        }

        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
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

        Text(
            "待办的计划轮数在每条待办的高级设置里改；轮数完成后会回到待办页弹完成提示。",
            fontSize = 12.sp,
            color = colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}
