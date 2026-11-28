package com.yike.focus.ui.screen.lock

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.FocusTimer
import com.yike.focus.data.LockPeriod
import com.yike.focus.data.ScheduledLock
import com.yike.focus.service.LockAccessibility
import com.yike.focus.ui.navigation3.Navigator
import com.yike.focus.ui.navigation3.Route
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import kotlinx.coroutines.delay

private val WEEK = listOf("一", "二", "三", "四", "五", "六", "日")

@Composable
fun LockScreen(navController: Navigator, bottomPadding: Dp) {
    val context = LocalContext.current
    var pickDuration by remember { mutableStateOf(false) }
    var customMinutes by remember { mutableStateOf("") }
    var pendingMinutes by remember { mutableStateOf<Int?>(null) }
    var countdown by remember { mutableStateOf(5) }
    var editingPeriod by remember { mutableStateOf<LockPeriod?>(null) }
    var refresh by remember { mutableIntStateOf(0) }

    fun startLockOrAskPermission(minutes: Int) {
        if (!LockAccessibility.enabled(context)) {
            Toast.makeText(context, "没有无障碍权限，请授予一刻的无障碍权限", Toast.LENGTH_LONG).show()
            runCatching {
                context.startActivity(
                    android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            return
        }
        FocusTimer.startLock(minutes)
        context.startActivity(
            android.content.Intent(context, LockActivity::class.java)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = bottomPadding + 24.dp),
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
                    subtitle = "可以设置多个时段，比如 22:30-23:30、01:00-06:00",
                    container = colorScheme.tertiaryContainer,
                    titleColor = colorScheme.onTertiaryContainer,
                    subtitleColor = colorScheme.onSurfaceVariantSummary,
                    onClick = { editingPeriod = LockPeriod() },
                )
            }
            item {
                val periods = remember(refresh) { ScheduledLock.periods() }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("定时锁机时段", fontSize = 16.sp, color = colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (periods.isEmpty()) "还没有时段，点上面的卡片添加"
                            else "共 ${periods.size} 个时段，到点自动进入锁机",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariantSummary,
                        )
                        periods.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { editingPeriod = p },
                                ) {
                                    Text(
                                        "${p.startLabel()} - ${p.endLabel()}",
                                        fontSize = 15.sp,
                                        color = colorScheme.onSurface,
                                    )
                                    Text(
                                        "${p.daysLabel()}${if (p.enabled) "" else " · 已关闭"}",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                                Switch(
                                    checked = p.enabled,
                                    onCheckedChange = { on ->
                                        ScheduledLock.upsert(p.copy(enabled = on))
                                        ScheduledLock.schedule(context)
                                        refresh++
                                    },
                                )
                            }
                        }
                        if (periods.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "⚠️ 到点会自动跳到锁机页，不完成后无法退出",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                }
            }
        }
    }

    if (pickDuration) {
        OverlayDialog(
            show = true,
            title = "锁机时长",
            summary = "锁机期间屏幕常亮，返回和上滑都不生效，到时间自动结束。",
            onDismissRequest = { pickDuration = false },
        ) {
            Column {
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
                            Text(
                                "$min 分钟",
                                fontSize = 13.sp,
                                color = colorScheme.onSurface,
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
                    TextButton(
                        text = "开始",
                        onClick = {
                            val min = customMinutes.toIntOrNull() ?: return@TextButton
                            if (min in 1..600) {
                                pickDuration = false
                                countdown = 5
                                pendingMinutes = min
                            }
                        },
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            }
        }
    }

    editingPeriod?.let { period ->
        PeriodEditorDialog(
            initial = period,
            onDismiss = { editingPeriod = null },
            onSave = { p ->
                ScheduledLock.upsert(p)
                ScheduledLock.schedule(context)
                editingPeriod = null
                refresh++
            },
            onDelete = if (ScheduledLock.periods().any { it.id == period.id }) {
                {
                    ScheduledLock.remove(period.id)
                    ScheduledLock.schedule(context)
                    editingPeriod = null
                    refresh++
                }
            } else null,
        )
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
                "锁机 $minutes 分钟，期间不能提前退出。"
            },
            confirmText = if (countdown > 0) "请稍等 $countdown" else "开始锁机",
            dismissText = "取消",
            onConfirm = {
                if (countdown == 0) {
                    pendingMinutes = null
                    startLockOrAskPermission(minutes)
                }
            },
            onDismiss = { pendingMinutes = null },
        )
    }
}

@Composable
private fun PeriodEditorDialog(
    initial: LockPeriod,
    onDismiss: () -> Unit,
    onSave: (LockPeriod) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val context = LocalContext.current
    var sh by remember { mutableIntStateOf(initial.startHour) }
    var sm by remember { mutableIntStateOf(initial.startMinute) }
    var eh by remember { mutableIntStateOf(initial.endHour) }
    var em by remember { mutableIntStateOf(initial.endMinute) }
    var days by remember { mutableStateOf(initial.days) }

    fun pickTime(hour: Int, minute: Int, set: (Int, Int) -> Unit) {
        android.app.TimePickerDialog(context, { _, h, m -> set(h, m) }, hour, minute, true).show()
    }

    OverlayDialog(
        show = true,
        title = "设置定时锁机时段",
        onDismissRequest = onDismiss,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("开始时间", fontSize = 13.sp, color = colorScheme.onSurface, modifier = Modifier.weight(1f))
                TextButton(text = "%02d:%02d".format(sh, sm), onClick = { pickTime(sh, sm) { h, m -> sh = h; sm = m } })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("结束时间", fontSize = 13.sp, color = colorScheme.onSurface, modifier = Modifier.weight(1f))
                TextButton(text = "%02d:%02d".format(eh, em), onClick = { pickTime(eh, em) { h, m -> eh = h; em = m } })
            }
            Spacer(Modifier.height(6.dp))
            Text("重复", fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WEEK.forEachIndexed { index, label ->
                    val day = index + 1
                    val selected = day in days
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) colorScheme.primaryContainer else colorScheme.surfaceContainer)
                            .clickable {
                                days = if (selected) days - day else days + day
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, fontSize = 12.sp, color = colorScheme.onSurface)
                    }
                }
            }
            Text(
                "结束时间早于开始时间时，表示跨夜（比如 22:30 - 06:00）",
                fontSize = 11.sp,
                color = colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                if (onDelete != null) {
                    TextButton(text = "删除", onClick = onDelete, modifier = Modifier.weight(1f))
                } else {
                    TextButton(text = "取消", onClick = onDismiss, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.width(12.dp))
                TextButton(
                    text = "保存",
                    onClick = {
                        if (days.isNotEmpty()) {
                            onSave(
                                initial.copy(
                                    startHour = sh, startMinute = sm,
                                    endHour = eh, endMinute = em,
                                    days = days, enabled = true,
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
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
