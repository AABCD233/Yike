package com.yike.focus.ui.screen.todo

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.FocusTimer
import com.yike.focus.data.TimerMode
import com.yike.focus.data.Todo
import com.yike.focus.data.TodoKind
import com.yike.focus.data.TodoStore
import com.yike.focus.ui.navigation3.Navigator
import com.yike.focus.ui.navigation3.Route
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import java.io.File

@Composable
fun TodoScreen(
    navController: Navigator,
    bottomPadding: Dp,
) {
    val context = LocalContext.current
    var todos by remember { mutableStateOf(TodoStore.todos()) }
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Todo?>(null) }
    var detailed by remember { mutableStateOf<Todo?>(null) }
    var deleting by remember { mutableStateOf<Todo?>(null) }
    var bgFor by remember { mutableStateOf<Todo?>(null) }
    val focusState by FocusTimer.state.collectAsState()

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        val todo = bgFor
        if (uri != null && todo != null) {
            runCatching {
                val dir = File(context.filesDir, "todo_bg").apply { mkdirs() }
                val file = File(dir, "todo_${todo.id}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                TodoStore.updateTodo(todo.copy(bgPath = file.absolutePath))
            }
            todos = TodoStore.todos()
        }
        bgFor = null
    }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("待办", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { navController.push(Route.History) }) {
                Icon(Icons.Rounded.History, contentDescription = "专注记录", tint = colorScheme.onBackground)
            }
            IconButton(onClick = { navController.push(Route.Boost) }) {
                Icon(Icons.Rounded.Bolt, contentDescription = "功能增强", tint = colorScheme.onBackground)
            }
            IconButton(onClick = { showAdd = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "添加待办", tint = colorScheme.onBackground)
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
                        onLongPress = { detailed = todo },
                        onMenu = { detailed = todo },
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

    detailed?.let { todo ->
        TaskDetailDialog(
            todo = todo,
            onDismiss = { detailed = null },
            onEdit = { detailed = null; editing = todo },
            onChangeBackground = {
                bgFor = todo
                detailed = null
                pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onOpenWhitelist = { detailed = null; navController.push(Route.Whitelist) },
            onOpenHistory = { detailed = null; navController.push(Route.History) },
            onMoveUp = {
                TodoStore.moveTodo(todo, up = true)
                todos = TodoStore.todos()
                detailed = null
            },
            onMoveDown = {
                TodoStore.moveTodo(todo, up = false)
                todos = TodoStore.todos()
                detailed = null
            },
            onDelete = { detailed = null; deleting = todo },
        )
    }

    deleting?.let { todo ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { androidx.compose.material3.Text("删除待办") },
            text = { androidx.compose.material3.Text("删除「${todo.title}」？已经产生的专注记录会保留。") },
            confirmButton = {
                TextButton(onClick = {
                    TodoStore.deleteTodo(todo.id)
                    todos = TodoStore.todos()
                    deleting = null
                }) { androidx.compose.material3.Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { androidx.compose.material3.Text("取消") }
            },
        )
    }
}

/** 长按卡片弹出的任务面板 */
@Composable
private fun TaskDetailDialog(
    todo: Todo,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onChangeBackground: () -> Unit,
    onOpenWhitelist: () -> Unit,
    onOpenHistory: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    val count = remember(todo.id) { TodoStore.todayCount(todo.id) }
    val seconds = remember(todo.id) { TodoStore.todaySeconds(todo.id) }
    val week = remember(todo.id) { TodoStore.dailySeconds(7) }

    OverlayDialog(
        show = true,
        title = todo.title,
        onDismissRequest = onDismiss,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PanelAction("编辑", Icons.Rounded.Edit, Modifier.weight(1f), onClick = onEdit)
                PanelAction("更换背景", Icons.Rounded.Image, Modifier.weight(1f), onClick = onChangeBackground)
                PanelAction("白名单", Icons.Rounded.Apps, Modifier.weight(1f), onClick = onOpenWhitelist)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PanelAction("上移", Icons.Rounded.ArrowUpward, Modifier.weight(1f), onClick = onMoveUp)
                PanelAction("下移", Icons.Rounded.ArrowDownward, Modifier.weight(1f), onClick = onMoveDown)
                PanelAction("删除", Icons.Rounded.DeleteOutline, Modifier.weight(1f), danger = true, onClick = onDelete)
            }
            Spacer(Modifier.height(14.dp))
            Card(modifier = Modifier.fillMaxWidth(), onClick = onOpenHistory, showIndication = true) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.History, contentDescription = null, tint = colorScheme.primary)
                    Spacer(Modifier.width(10.dp))
                    Text("专注历史记录", fontSize = 15.sp, color = colorScheme.onSurface)
                    Spacer(Modifier.weight(1f))
                    Text("${count} 次", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                }
            }
            Spacer(Modifier.height(10.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    Text("最近七天", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val max = week.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
                        week.forEach { (day, sec) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (sec == 0) colorScheme.surfaceContainerHighest
                                            else colorScheme.primary.copy(alpha = 0.35f + 0.65f * (sec.toFloat() / max))
                                        ),
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("${day.dayOfMonth}", fontSize = 10.sp, color = colorScheme.onSurfaceVariantSummary)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("累计专注", fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
                            Text(
                                "${seconds / 60} 分钟",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurface,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("今日次数", fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
                            Text(
                                "$count 次",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Timer, contentDescription = null, tint = colorScheme.onSurfaceVariantSummary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "定时功能：还没有设置定时提醒或定时学霸",
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@Composable
private fun PanelAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        showIndication = true,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (danger) Color(0xFFC8432B) else colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, color = if (danger) Color(0xFFC8432B) else colorScheme.onSurface)
        }
    }
}

@Composable
internal fun TodoCard(
    todo: Todo,
    running: Boolean,
    onStart: () -> Unit,
    onLongPress: (() -> Unit)? = null,
    onMenu: (() -> Unit)? = null,
) {
    val count = remember(todo.id, todo.title) { TodoStore.todayCount(todo.id) }
    val todaySec = remember(todo.id, todo.title) { TodoStore.todaySeconds(todo.id) }
    val bitmap = rememberBitmap(todo.bgPath)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onStart, onLongClick = { onLongPress?.invoke() }),
        showIndication = true,
    ) {
        Box {
            bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
                Box(
                    modifier = Modifier.matchParentSize().background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.10f), Color.Black.copy(alpha = 0.45f))
                        )
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        todo.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (bitmap != null) Color.White else colorScheme.onSurface,
                        maxLines = 2,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        subtitleOf(todo, todaySec),
                        fontSize = 13.sp,
                        color = if (bitmap != null) Color.White.copy(alpha = 0.85f)
                        else colorScheme.onSurfaceVariantSummary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (running) "进行中" else "开始",
                        fontSize = 15.sp,
                        color = if (bitmap != null) Color.White else colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                    Text(
                        "今日 $count 次",
                        fontSize = 12.sp,
                        color = if (bitmap != null) Color.White.copy(alpha = 0.8f)
                        else colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.padding(end = 12.dp, top = 2.dp),
                    )
                }
                if (onMenu != null) {
                    IconButton(onClick = onMenu, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Rounded.MoreVert,
                            contentDescription = "更多操作",
                            tint = if (bitmap != null) Color.White else colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberBitmap(path: String?): ImageBitmap? {
    return remember(path) {
        if (path == null) null else runCatching {
            val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
            BitmapFactory.decodeFile(path, opts)?.asImageBitmap()
        }.getOrNull()
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
