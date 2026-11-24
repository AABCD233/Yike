package com.yike.focus.ui.screen.todo

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yike.focus.data.TimerMode
import com.yike.focus.data.Todo
import com.yike.focus.data.TodoKind
import com.yike.focus.templateApp
import com.yike.focus.ui.theme.TP
import java.time.LocalDate

@Composable
fun AddTodoDialog(
    onDismiss: () -> Unit,
    onCreate: (Todo) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(TodoKind.NORMAL) }
    var mode by remember { mutableStateOf(TimerMode.COUNTDOWN) }
    var minutes by remember { mutableStateOf(defaultFocusMinutes()) }
    var customOpen by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("") }
    var hoursText by remember { mutableStateOf("") }
    var dailyText by remember { mutableStateOf("") }
    var advanced by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "关闭", tint = TP.TextDark)
                    }
                    Text(
                        "添加待办",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 17.sp,
                        color = TP.TextDark,
                    )
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreate(
                                    Todo(
                                        title = title.trim(),
                                        kind = kind,
                                        timerMode = mode,
                                        durationMin = minutes,
                                        targetHours = hoursText.toFloatOrNull() ?: 0f,
                                        startDate = if (kind == TodoKind.GOAL) TodoStore_today() else null,
                                        endDate = if (kind == TodoKind.GOAL) LocalDate.now().plusDays(30).toString() else null,
                                        dailyMin = dailyText.toIntOrNull() ?: 0,
                                        note = note,
                                    )
                                )
                            }
                        },
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = "确定", tint = TP.TextDark)
                    }
                }

                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text(placeholderFor(kind), color = TP.TextGrey) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("普通番茄钟", kind == TodoKind.NORMAL) { kind = TodoKind.NORMAL }
                    Pill("定目标", kind == TodoKind.GOAL) { kind = TodoKind.GOAL }
                    Pill("养习惯", kind == TodoKind.HABIT) { kind = TodoKind.HABIT }
                }

                if (kind == TodoKind.GOAL) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("我想在", fontSize = 14.sp, color = TP.TextDark)
                        Highlight("设定目标日期") { }
                        Text("之前，一共完成", fontSize = 14.sp, color = TP.TextDark)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = hoursText,
                            onValueChange = { hoursText = it.filter(Char::isDigit) },
                            singleLine = true,
                            placeholder = { Text("小时", color = TP.TextGrey) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(120.dp),
                        )
                        Text("  小时", fontSize = 14.sp, color = TP.TextDark)
                    }
                }

                if (kind == TodoKind.HABIT) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("我想", fontSize = 14.sp, color = TP.TextDark)
                        Highlight("每天") { }
                        Text("完成", fontSize = 14.sp, color = TP.TextDark)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = dailyText,
                            onValueChange = { dailyText = it.filter(Char::isDigit) },
                            singleLine = true,
                            placeholder = { Text("分钟", color = TP.TextGrey) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(120.dp),
                        )
                        Text("  分钟", fontSize = 14.sp, color = TP.TextDark)
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("最后一步，设置单次专注的时长：", fontSize = 13.sp, color = TP.TextGrey)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill("倒计时", mode == TimerMode.COUNTDOWN) { mode = TimerMode.COUNTDOWN }
                    Pill("正向计时", mode == TimerMode.COUNTUP) { mode = TimerMode.COUNTUP }
                    Pill("不计时", mode == TimerMode.NONE) { mode = TimerMode.NONE }
                }
                if (mode == TimerMode.COUNTDOWN) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Pill("25 分钟", !customOpen && minutes == 25) { minutes = 25; customOpen = false }
                        Pill("35 分钟", !customOpen && minutes == 35) { minutes = 35; customOpen = false }
                        Pill("自定义", customOpen) { customOpen = true }
                    }
                    if (customOpen) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = customText,
                                onValueChange = {
                                    customText = it.filter(Char::isDigit)
                                    customText.toIntOrNull()?.let { v -> minutes = v.coerceIn(1, 600) }
                                },
                                singleLine = true,
                                placeholder = { Text("分钟", color = TP.TextGrey) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(120.dp),
                            )
                            Text("  分钟（1-600）", fontSize = 13.sp, color = TP.TextGrey)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    "· 倒计时 25 分钟即标准番茄时间",
                    fontSize = 12.sp,
                    color = TP.TextGrey,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .clickable { advanced = !advanced }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (advanced) "收起高级设置" else "展开更多高级设置",
                        fontSize = 13.sp,
                        color = TP.Teal,
                    )
                }
                if (advanced) {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = { Text("备注（可选）", color = TP.TextGrey) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

private fun placeholderFor(kind: TodoKind) = when (kind) {
    TodoKind.NORMAL -> "请输入待办名称"
    TodoKind.GOAL -> "请输入目标名称"
    TodoKind.HABIT -> "请输入习惯名称"
}

@Composable
private fun Pill(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) TP.Teal.copy(alpha = 0.15f) else Color(0xFFF2F5F7))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(text, fontSize = 13.sp, color = if (selected) TP.Teal else TP.TextDark.copy(alpha = 0.75f))
    }
}

@Composable
private fun Highlight(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(TP.Coral.copy(alpha = 0.12f))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(text, fontSize = 13.sp, color = TP.Coral)
    }
}

/** 只用日期字符串，避免在弹窗里引入日期选择器 */
private fun TodoStore_today(): String = LocalDate.now().toString()

/** 设置里的「默认番茄时长」，新建待办时用它当默认值 */
internal fun defaultFocusMinutes(): Int {
    val prefs = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)
    return prefs.getInt("default_focus_min", 25).coerceIn(1, 600)
}

internal fun saveDefaultFocusMinutes(minutes: Int) {
    templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .edit().putInt("default_focus_min", minutes.coerceIn(1, 600)).apply()
}
