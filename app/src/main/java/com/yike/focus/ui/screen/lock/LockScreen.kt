package com.yike.focus.ui.screen.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.FocusTimer
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import com.yike.focus.ui.navigation3.Navigator
import androidx.compose.ui.platform.LocalContext
import com.yike.focus.ui.navigation3.Route
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

@Composable
fun LockScreen(navController: Navigator, bottomPadding: Dp) {
    val context = LocalContext.current
    var tip by remember { mutableStateOf<String?>(null) }
    var pickDuration by remember { mutableStateOf(false) }
    var customMinutes by remember { mutableStateOf("") }
    var pendingMinutes by remember { mutableStateOf<Int?>(null) }
    var countdown by remember { mutableStateOf(5) }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("锁机", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 12.dp, bottom = bottomPadding + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                BannerCard(
                    title = "小憩",
                    subtitle = "放松身心的深呼吸，感受禅修般的宁静",
                    container = colorScheme.primaryContainer,
                    titleColor = colorScheme.onPrimaryContainer,
                    subtitleColor = colorScheme.onSurfaceVariantSummary,
                    onClick = { navController.push(Route.Breathe) },
                )
            }
            item {
                BannerCard(
                    title = "立刻开始快速锁机",
                    subtitle = "选择锁机时长，并立刻开始锁机",
                    container = colorScheme.secondaryContainer,
                    titleColor = colorScheme.onSecondaryContainer,
                    subtitleColor = colorScheme.onSurfaceVariantSummary,
                    onClick = { pickDuration = true },
                )
            }
            item {
                BannerCard(
                    title = "添加自定义锁机时间段",
                    subtitle = "在指定时间范围内锁住手机，不做熬夜手机党",
                    container = colorScheme.tertiaryContainer,
                    titleColor = colorScheme.onTertiaryContainer,
                    subtitleColor = colorScheme.onSurfaceVariantSummary,
                    onClick = { tip = "定时锁机等后台服务接好再做。" },
                )
            }
            item {
                val sched = com.yike.focus.data.ScheduledLock
                var schedOn by remember { mutableStateOf(sched.enabled) }
                var sh by remember { mutableIntStateOf(sched.startHour) }
                var sm by remember { mutableIntStateOf(sched.startMinute) }
                var eh by remember { mutableIntStateOf(sched.endHour) }
                var em by remember { mutableIntStateOf(sched.endMinute) }

                fun pushSchedule() {
                    sched.startHour = sh
                    sched.startMinute = sm
                    sched.endHour = eh
                    sched.endMinute = em
                    sched.enabled = schedOn
                    sched.schedule(context)
                }

                fun pickTime(hour: Int, minute: Int, onPicked: (Int, Int) -> Unit) {
                    android.app.TimePickerDialog(
                        context,
                        { _, h, m -> onPicked(h, m); pushSchedule() },
                        hour,
                        minute,
                        true,
                    ).show()
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("定时锁机", fontSize = 16.sp, color = colorScheme.onSurface)
                                Text(
                                    if (schedOn) "每天 ${sched.label(sh, sm)} - ${sched.label(eh, em)}" else "到点自动进入锁机，期间无法退出",
                                    fontSize = 12.sp,
                                    color = colorScheme.onSurfaceVariantSummary,
                                )
                            }
                            Switch(checked = schedOn, onCheckedChange = {
                                schedOn = it
                                pushSchedule()
                                if (it) tip = "已开启：到点会自动锁定屏幕，请确认时间是否正确"
                            })
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("开始", fontSize = 13.sp, color = colorScheme.onSurface)
                            Spacer(Modifier.width(10.dp))
                            Card(onClick = { pickTime(sh, sm) { h, m -> sh = h; sm = m } }, showIndication = true) {
                                Text(sched.label(sh, sm), fontSize = 13.sp, color = colorScheme.onSurface, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Text("结束", fontSize = 13.sp, color = colorScheme.onSurface)
                            Spacer(Modifier.width(10.dp))
                            Card(onClick = { pickTime(eh, em) { h, m -> eh = h; em = m } }, showIndication = true) {
                                Text(sched.label(eh, em), fontSize = 13.sp, color = colorScheme.onSurface, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "⚠️ 开启后到点会自动跳到锁机页，不完成后无法退出；只建议在睡前设置。",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        }
    }

    if (pickDuration) {
        top.yukonga.miuix.kmp.overlay.OverlayDialog(
            show = true,
            title = "锁机时长",
            summary = "锁机期间屏幕常亮，返回键不生效，到时间自动结束。",
            onDismissRequest = { pickDuration = false },
        ) {
                Column {
                    androidx.compose.material3.Text(
                        "锁机期间屏幕常亮，返回键不生效，到时间自动结束。",
                        fontSize = 13.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 25, 45, 60).forEach { min ->
                            Card(
                                onClick = {
                                    pickDuration = false
                                    countdown = 5
                                    pendingMinutes = min
                                },
                                showIndication = true,
                            ) {
                                androidx.compose.material3.Text(
                                    "$min 分钟",
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = customMinutes,
                            onValueChange = { customMinutes = it.filter(Char::isDigit) },
                            singleLine = true,
                            placeholder = { androidx.compose.material3.Text("自定义分钟") },
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        Card(
                            onClick = {
                                val min = customMinutes.toIntOrNull() ?: return@Card
                                if (min in 1..600) {
                                    pickDuration = false
                                    countdown = 5
                                    pendingMinutes = min
                                }
                            },
                            showIndication = true,
                        ) {
                            androidx.compose.material3.Text(
                                "开始",
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
        }
    }

    pendingMinutes?.let { minutes ->
        LaunchedEffect(countdown) {
            if (countdown > 0) {
                delay(1000)
                countdown--
            }
        }
        com.yike.focus.ui.component.miuix.ConfirmDialog(
            show = true,
            title = "即将开始锁机",
            summary = if (countdown > 0) {
                "还有 $countdown 秒。倒计时结束后会锁机 $minutes 分钟，期间上滑回桌面也会被拉回来。"
            } else {
                "锁机 $minutes 分钟，期间屏幕常亮，不能提前退出。"
            },
            confirmText = if (countdown > 0) "请稍等 $countdown" else "开始锁机",
            dismissText = "取消",
            onConfirm = {
                if (countdown == 0) {
                    pendingMinutes = null
                    if (com.yike.focus.service.LockAccessibility.enabled(context)) {
                        FocusTimer.startLock(minutes)
                        context.startActivity(
                            android.content.Intent(context, LockActivity::class.java)
                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } else {
                        tip = "还没有开无障碍权限，开不了锁机。去「我的 → 权限 → 无障碍」开启后再试。"
                    }
                }
            },
            onDismiss = { pendingMinutes = null },
        )
    }

    tip?.let { text ->
        com.yike.focus.ui.component.miuix.ConfirmDialog(
            show = true,
            title = "提示",
            summary = text,
            confirmText = "知道了",
            dismissText = null,
            onConfirm = { tip = null },
            onDismiss = { tip = null },
        )
    }
}

@Composable
private fun BannerCard(
    title: String,
    subtitle: String,
    container: Color,
    titleColor: Color,
    subtitleColor: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(color = container),
        onClick = onClick,
        showIndication = true,
        pressFeedbackType = top.yukonga.miuix.kmp.utils.PressFeedbackType.Tilt,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = titleColor)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 13.sp, color = subtitleColor)
        }
    }
}
