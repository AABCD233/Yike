@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.yike.focus.ui.screen.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.TimerMode
import com.yike.focus.data.Todo
import com.yike.focus.data.TodoKind
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

@Composable
fun AddTodoDialog(
    onDismiss: () -> Unit,
    initial: Todo? = null,
    onCreate: (Todo) -> Unit,
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var kind by remember { mutableStateOf(initial?.kind ?: TodoKind.NORMAL) }
    var mode by remember { mutableStateOf(initial?.timerMode ?: TimerMode.COUNTDOWN) }
    var minutes by remember { mutableStateOf(initial?.durationMin ?: 25) }
    var hoursText by remember { mutableStateOf(initial?.targetHours?.takeIf { it > 0f }?.toString() ?: "") }
    var dailyText by remember { mutableStateOf(initial?.dailyMin?.takeIf { it > 0 }?.toString() ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var breakMin by remember { mutableStateOf(initial?.breakMin ?: 5) }
    var longBreakMin by remember { mutableStateOf(initial?.longBreakMin ?: 15) }
    var rounds by remember { mutableStateOf(initial?.rounds ?: 4) }
    var advanced by remember { mutableStateOf(false) }
    var reminder by remember { mutableStateOf(initial?.reminderEnabled ?: false) }
    var reminderHour by remember { mutableStateOf(initial?.reminderHour ?: 8) }
    var reminderMinute by remember { mutableStateOf(initial?.reminderMinute ?: 0) }
    var totalRounds by remember { mutableStateOf(initial?.totalRounds ?: 0) }
    var habitPerWeek by remember { mutableStateOf(initial?.habitPerWeek ?: 0) }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var goalDate by remember { mutableStateOf(initial?.endDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showFreqPicker by remember { mutableStateOf(false) }

    OverlayDialog(
        show = true,
        title = if (initial == null) "添加待办" else "编辑待办",
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                when (kind) {
                    TodoKind.NORMAL -> "待办名称"
                    TodoKind.GOAL -> "目标名称"
                    TodoKind.HABIT -> "习惯名称"
                },
                fontSize = 11.sp,
                color = colorScheme.onSurfaceVariantSummary,
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("普通番茄钟", kind == TodoKind.NORMAL) { kind = TodoKind.NORMAL }
                Chip("定目标", kind == TodoKind.GOAL) { kind = TodoKind.GOAL }
                Chip("养习惯", kind == TodoKind.HABIT) { kind = TodoKind.HABIT }
            }

            if (kind == TodoKind.GOAL) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("我想在", fontSize = 13.sp, color = colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorScheme.primaryContainer)
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            goalDate ?: "设定目标日期",
                            fontSize = 13.sp,
                            color = colorScheme.onPrimaryContainer,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("之前完成", fontSize = 13.sp, color = colorScheme.onSurface)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("目标总时长", fontSize = 13.sp, color = colorScheme.onSurface)
                    Spacer(Modifier.width(10.dp))
                    TextField(
                        value = hoursText,
                        onValueChange = { hoursText = it.filter(Char::isDigit) },
                        modifier = Modifier.width(110.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Text(" 小时", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                }
            }

            if (kind == TodoKind.HABIT) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("我想", fontSize = 13.sp, color = colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorScheme.primaryContainer)
                            .clickable { showFreqPicker = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            if (habitPerWeek == 0) "每天" else "每周 $habitPerWeek 次",
                            fontSize = 13.sp,
                            color = colorScheme.onPrimaryContainer,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("完成", fontSize = 13.sp, color = colorScheme.onSurface)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("每天目标", fontSize = 13.sp, color = colorScheme.onSurface)
                    Spacer(Modifier.width(10.dp))
                    TextField(
                        value = dailyText,
                        onValueChange = { dailyText = it.filter(Char::isDigit) },
                        modifier = Modifier.width(110.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Text(" 分钟", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("计时方式", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("倒计时", mode == TimerMode.COUNTDOWN) { mode = TimerMode.COUNTDOWN }
                Chip("正向计时", mode == TimerMode.COUNTUP) { mode = TimerMode.COUNTUP }
                Chip("不计时", mode == TimerMode.NONE) { mode = TimerMode.NONE }
            }

            if (mode == TimerMode.COUNTDOWN) {
                Spacer(Modifier.height(14.dp))
                Text("单次专注时长", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 25, 35, 45, 60).forEach { value ->
                        Chip("$value", minutes == value) { minutes = value }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Stepper(
                    title = "更精确的分钟数",
                    value = "$minutes 分钟",
                    onMinus = { minutes = (minutes - 1).coerceAtLeast(1) },
                    onPlus = { minutes = (minutes + 1).coerceAtMost(600) },
                )
            }

            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { advanced = !advanced }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (advanced) "收起高级设置" else "展开更多高级设置",
                    fontSize = 13.sp,
                    color = colorScheme.primary,
                )
            }

            if (advanced) {
                if (mode == TimerMode.COUNTDOWN) {
                Stepper(
                    title = "休息时长",
                    value = "$breakMin 分钟",
                    onMinus = { breakMin = (breakMin - 1).coerceAtLeast(1) },
                    onPlus = { breakMin = (breakMin + 1).coerceAtMost(60) },
                )
                Stepper(
                    title = "长休息时长",
                    value = "$longBreakMin 分钟",
                    onMinus = { longBreakMin = (longBreakMin - 5).coerceAtLeast(5) },
                    onPlus = { longBreakMin = (longBreakMin + 5).coerceAtMost(120) },
                )
                Stepper(
                    title = "总共几轮算完成（0 不限）",
                    value = if (totalRounds == 0) "不限" else "$totalRounds 轮",
                    onMinus = { totalRounds = (totalRounds - 1).coerceAtLeast(0) },
                    onPlus = { totalRounds = (totalRounds + 1).coerceAtMost(50) },
                )
                Stepper(
                    title = "几轮后长休息",
                    value = "$rounds 个",
                    onMinus = { rounds = (rounds - 1).coerceAtLeast(1) },
                    onPlus = { rounds = (rounds + 1).coerceAtMost(12) },
                )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("每日定时提醒", fontSize = 13.sp, color = colorScheme.onSurface, modifier = Modifier.weight(1f))
                    androidx.compose.material3.Switch(checked = reminder, onCheckedChange = { reminder = it })
                }
                if (reminder) {
                    Stepper(
                        title = "提醒时间（小时）",
                        value = "%02d".format(reminderHour),
                        onMinus = { reminderHour = (reminderHour - 1 + 24) % 24 },
                        onPlus = { reminderHour = (reminderHour + 1) % 24 },
                    )
                    Stepper(
                        title = "提醒时间（分钟）",
                        value = "%02d".format(reminderMinute),
                        onMinus = { reminderMinute = (reminderMinute - 5 + 60) % 60 },
                        onPlus = { reminderMinute = (reminderMinute + 5) % 60 },
                    )
                }
                Spacer(Modifier.height(6.dp))
                TextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("备注", fontSize = 11.sp, color = colorScheme.onSurfaceVariantSummary)
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                text = "取消",
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(16.dp))
            TextButton(
                text = if (initial == null) "添加" else "保存",
                onClick = {
                    if (title.isBlank()) {
                        android.widget.Toast.makeText(ctx, "先给待办起个名字吧", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        onCreate(
                            Todo(
                                id = initial?.id ?: 0L,
                                title = title.trim(),
                                kind = kind,
                                timerMode = mode,
                                durationMin = minutes,
                                targetHours = hoursText.toFloatOrNull() ?: 0f,
                                dailyMin = dailyText.toIntOrNull() ?: 0,
                                note = note,
                                createdAt = initial?.createdAt ?: System.currentTimeMillis(),
                                bgPath = initial?.bgPath,
                                breakMin = breakMin,
                                longBreakMin = longBreakMin,
                                rounds = rounds,
                                reminderEnabled = reminder,
                                reminderHour = reminderHour,
                                reminderMinute = reminderMinute,
                                totalRounds = totalRounds,
                                habitPerWeek = habitPerWeek,
                                endDate = if (kind == TodoKind.GOAL) goalDate else initial?.endDate,
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }

    if (showDatePicker) {
        val pickerState = androidx.compose.material3.rememberDatePickerState()
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        goalDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                            .toString()
                    }
                    showDatePicker = false
                }) { androidx.compose.material3.Text("确定") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDatePicker = false }) {
                    androidx.compose.material3.Text("取消")
                }
            },
        ) {
            androidx.compose.material3.DatePicker(state = pickerState)
        }
    }

    if (showFreqPicker) {
        top.yukonga.miuix.kmp.overlay.OverlayDialog(
            show = true,
            title = "频率",
            onDismissRequest = { showFreqPicker = false },
        ) {
            Column {
                listOf(0 to "每天", 1 to "每周 1 次", 2 to "每周 2 次", 3 to "每周 3 次",
                    4 to "每周 4 次", 5 to "每周 5 次", 6 to "每周 6 次", 7 to "每周 7 次").forEach { (value, label) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (habitPerWeek == value) colorScheme.primaryContainer
                                else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .clickable {
                                habitPerWeek = value
                                showFreqPicker = false
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Text(label, fontSize = 14.sp, color = colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) colorScheme.primaryContainer else colorScheme.surfaceContainerHighest)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text,
            fontSize = 13.sp,
            color = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun Stepper(title: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 13.sp, color = colorScheme.onSurface, modifier = Modifier.weight(1f))
        IconButton(onClick = onMinus, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Rounded.Remove, contentDescription = "减少", tint = colorScheme.onSurfaceVariantSummary)
        }
        Text(value, fontSize = 13.sp, color = colorScheme.onSurface, modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = onPlus, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Rounded.Add, contentDescription = "增加", tint = colorScheme.onSurfaceVariantSummary)
        }
    }
}
