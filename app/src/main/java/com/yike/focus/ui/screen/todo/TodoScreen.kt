package com.yike.focus.ui.screen.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.FocusTimer
import com.yike.focus.data.TimerMode
import com.yike.focus.data.Todo
import com.yike.focus.data.TodoKind
import com.yike.focus.data.TodoStore
import com.yike.focus.ui.navigation3.Navigator
import com.yike.focus.ui.component.miuix.ConfirmDialog
import com.yike.focus.ui.navigation3.Route
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import androidx.compose.ui.text.font.FontWeight

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

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "待办",
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onBackground,
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { navController.push(Route.History) }) {
                Icon(
                    Icons.Rounded.History,
                    contentDescription = "专注记录",
                    tint = colorScheme.onBackground,
                )
            }
            IconButton(onClick = { navController.push(Route.Boost) }) {
                Icon(
                    Icons.Rounded.Bolt,
                    contentDescription = "功能增强",
                    tint = colorScheme.onBackground,
                )
            }
            IconButton(onClick = { showAdd = true }) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "添加任务",
                    tint = colorScheme.onBackground,
                )
            }
        }

        focusState?.let { fs ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                onClick = { navController.push(Route.Focus) },
                showIndication = true,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("正在专注：${fs.title}", fontSize = 14.sp, color = colorScheme.primary)
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (fs.isPaused) "已暂停" else "继续",
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }

        if (todos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("还没有待办，点右上角 + 添加", fontSize = 14.sp, color = colorScheme.onSurfaceVariantSummary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomPadding + 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(todos, key = { it.id }) { todo ->
                    TodoCard(
                        todo = todo,
                        running = focusState?.todoId == todo.id,
                        onStart = {
                            if (FocusTimer.state.value != null) {
                                navController.push(Route.Focus)
                            } else {
                                FocusTimer.start(todo)
                                navController.push(Route.Focus)
                            }
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
        AddTodoDialog(
            onDismiss = { editing = null },
            initial = todo,
            onCreate = { updated ->
                TodoStore.updateTodo(updated)
                todos = TodoStore.todos()
                editing = null
            },
        )
    }

    deleting?.let { todo ->
        ConfirmDialog(
            show = true,
            title = "删除待办",
            summary = "删除「${todo.title}」？已经产生的专注记录会保留。",
            confirmText = "删除",
            onDismiss = { deleting = null },
            onConfirm = {
                TodoStore.deleteTodo(todo.id)
                todos = TodoStore.todos()
                deleting = null
            },
        )
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onStart,
                onLongClick = { onEdit?.invoke() },
            ),
        showIndication = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    todo.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitleOf(todo, todaySec),
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (running) "进行中" else "开始",
                    fontSize = 15.sp,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp),
                )
                Text(
                    "今日 $count 次",
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(end = 12.dp, top = 2.dp),
                )
            }
            if (onEdit != null || onDelete != null) {
                Box {
                    IconButton(onClick = { menu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = "更多操作",
                            tint = colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        if (onEdit != null) {
                            DropdownMenuItem(
                                text = { androidx.compose.material3.Text("编辑") },
                                onClick = { menu = false; onEdit() },
                            )
                        }
                        if (onDelete != null) {
                            DropdownMenuItem(
                                text = { androidx.compose.material3.Text("删除") },
                                onClick = { menu = false; onDelete() },
                            )
                        }
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
