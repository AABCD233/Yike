package com.tomato.focus.ui.screen.todo

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.focus.data.FocusTimer
import com.tomato.focus.data.TimerMode
import com.tomato.focus.data.Todo
import com.tomato.focus.data.TodoKind
import com.tomato.focus.data.TodoStore
import com.tomato.focus.ui.navigation3.Navigator
import com.tomato.focus.ui.navigation3.Route
import com.tomato.focus.ui.theme.TP

@Composable
fun TodoScreen(
    navController: Navigator,
    bottomPadding: Dp,
) {
    var todos by remember { mutableStateOf(TodoStore.todos()) }
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Todo?>(null) }
    var deleting by remember { mutableStateOf<Todo?>(null) }
    val focusState by FocusTimer.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FA))) {
        TodoHeader(
            onAdd = { showAdd = true },
            onHistory = { navController.push(Route.History) },
        )
        if (todos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("还没有待办，点右上角 + 新建一个", fontSize = 13.sp, color = TP.TextGrey)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = bottomPadding + 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(todos, key = { it.id }) { todo ->
                    TodoCard(
                        todo = todo,
                        running = focusState?.todoId == todo.id,
                        onStart = {
                            FocusTimer.start(todo)
                            navController.push(Route.Focus)
                        },
                        onEdit = { editing = todo },
                        onDelete = { deleting = todo },
                    )
                }
            }
        }
    }

    if (showAdd) {
        AddTodoDialog(
            onDismiss = { showAdd = false },
            onCreate = { todo ->
                TodoStore.insertTodo(todo)
                todos = TodoStore.todos()
                showAdd = false
            },
        )
    }

    editing?.let { todo ->
        RenameDialog(
            initial = todo.title,
            onDismiss = { editing = null },
            onConfirm = { name ->
                TodoStore.updateTodo(todo.copy(title = name))
                todos = TodoStore.todos()
                editing = null
            },
        )
    }

    deleting?.let { todo ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("删除待办") },
            text = { Text("删除「${todo.title}」？已经产生的专注记录会保留。") },
            confirmButton = {
                TextButton(onClick = {
                    TodoStore.deleteTodo(todo.id)
                    todos = TodoStore.todos()
                    deleting = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun RenameDialog(initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重命名") },
        text = { TextField(value = text, onValueChange = { text = it }, singleLine = true) },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text.trim()) }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun TodoHeader(onAdd: () -> Unit, onHistory: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TP.headerBrush())
            .statusBarsPadding()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("待办", fontSize = 26.sp, fontWeight = FontWeight.Medium, color = TP.TextDark)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onHistory) {
                Icon(Icons.Rounded.Alarm, contentDescription = "专注记录", tint = TP.TextDark.copy(alpha = 0.75f))
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Rounded.Add, contentDescription = "添加待办", tint = TP.TextDark.copy(alpha = 0.75f))
            }
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.75f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Text("学霸白名单选项（待接入）", fontSize = 12.sp, color = TP.TextDark.copy(alpha = 0.7f))
        }
    }
}

@Composable
internal fun TodoCard(
    todo: Todo,
    running: Boolean,
    onStart: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    val count = remember(todo.id, todo.title) { TodoStore.todayCount(todo.id) }
    val todaySec = remember(todo.id, todo.title) { TodoStore.todaySeconds(todo.id) }
    var menu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TP.cardBrush(todo.title.hashCode() + todo.id.toInt())),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp, end = 90.dp, top = 16.dp, bottom = 16.dp),
        ) {
            Text(
                todo.title,
                fontSize = 21.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 2,
            )
            Spacer(Modifier.weight(1f))
            Text(
                subtitleOf(todo, todaySec),
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                if (running) "进行中" else "开始",
                fontSize = 19.sp,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onStart() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text("今日已专注 $count 次", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
        }
        if (onEdit != null || onDelete != null) {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { menu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "更多操作",
                        tint = Color.White.copy(alpha = 0.9f),
                    )
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    if (onEdit != null) {
                        DropdownMenuItem(
                            text = { Text("重命名") },
                            onClick = { menu = false; onEdit() },
                        )
                    }
                    if (onDelete != null) {
                        DropdownMenuItem(
                            text = { Text("删除") },
                            onClick = { menu = false; onDelete() },
                        )
                    }
                }
            }
        }
    }
}

internal fun subtitleOf(todo: Todo, todaySeconds: Int): String = when (todo.kind) {
    TodoKind.GOAL -> {
        val hours = todaySeconds / 3600f
        "定目标 · 已完成 %.1f 小时 · 目标 %.1f 小时".format(hours, todo.targetHours)
    }
    TodoKind.HABIT -> {
        val min = todaySeconds / 60
        "养习惯 · 今日 $min/${todo.dailyMin} 分钟"
    }
    TodoKind.NORMAL -> when (todo.timerMode) {
        TimerMode.COUNTDOWN -> "${todo.durationMin} 分钟"
        TimerMode.COUNTUP -> "正向计时"
        TimerMode.NONE -> "不计时"
    }
}
