package com.yike.focus.ui.screen.todoset

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.yike.focus.data.FocusTimer
import com.yike.focus.data.Todo
import com.yike.focus.data.TodoSet
import com.yike.focus.data.TodoStore
import com.yike.focus.ui.component.miuix.ConfirmDialog
import com.yike.focus.ui.component.miuix.InputDialog
import com.yike.focus.ui.navigation3.Navigator
import com.yike.focus.ui.navigation3.Route
import com.yike.focus.ui.screen.todo.AddTodoDialog
import com.yike.focus.ui.screen.todo.TodoCard
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

private val setColors = listOf(
    0xFF5B8DEF, 0xFF2EB6F5, 0xFFF27FA5, 0xFF4FC7B0, 0xFF9B7BE0,
    0xFFE8553E, 0xFFD08B4E, 0xFF7C8AA0, 0xFFE4577C, 0xFF3D7BD9,
)

@Composable
fun TodoSetScreen(navController: Navigator, bottomPadding: Dp) {
    var sets by remember { mutableStateOf(TodoStore.todoSets()) }
    var refresh by remember { mutableIntStateOf(0) }
    var expandedId by remember { mutableStateOf<Long?>(null) }
    var menuSet by remember { mutableStateOf<TodoSet?>(null) }
    var sorting by remember { mutableStateOf<TodoSet?>(null) }
    var renaming by remember { mutableStateOf<TodoSet?>(null) }
    var deleting by remember { mutableStateOf<TodoSet?>(null) }
    var editing by remember { mutableStateOf<TodoSet?>(null) }
    var creating by remember { mutableStateOf(false) }
    var addTo by remember { mutableStateOf<TodoSet?>(null) }
    var newTodoFor by remember { mutableStateOf<TodoSet?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("待办集", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
            Spacer(Modifier.weight(1f))
            androidx.compose.material3.IconButton(onClick = { navController.push(Route.Boost) }) {
                Icon(Icons.Rounded.Bolt, contentDescription = "增强模式", tint = colorScheme.onBackground)
            }
            androidx.compose.material3.IconButton(onClick = { creating = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "新建待办集", tint = colorScheme.onBackground)
            }
        }

        if (sets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("还没有待办集，点右上角 + 新建", fontSize = 14.sp, color = colorScheme.onSurfaceVariantSummary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomPadding + 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(sets, key = { it.id }) { set ->
                    Column {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { expandedId = if (expandedId == set.id) null else set.id },
                            showIndication = true,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(setColors[set.color % setColors.size])),
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(set.name, fontSize = 16.sp, color = colorScheme.onSurface)
                                    Text(
                                        "${TodoStore.countTodoInSet(set.id)} 个待办 · 休息 ${set.restMin} 分",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                                androidx.compose.material3.IconButton(onClick = { editing = set }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Rounded.Settings, contentDescription = "待办集设置", tint = colorScheme.onSurfaceVariantSummary)
                                }
                                androidx.compose.material3.IconButton(onClick = { addTo = set }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Rounded.Add, contentDescription = "添加待办", tint = colorScheme.onSurfaceVariantSummary)
                                }
                                androidx.compose.material3.IconButton(onClick = { menuSet = set }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = "更多操作", tint = colorScheme.onSurfaceVariantSummary)
                                }
                                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colorScheme.onSurfaceVariantSummary)
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                        if (expandedId == set.id) {
                            val setTodos = remember(set.id, expandedId, refresh) { TodoStore.todos(set.id) }
                            if (setTodos.isEmpty()) {
                                Text(
                                    "这个待办集里还没有待办，点 + 添加",
                                    fontSize = 12.sp,
                                    color = colorScheme.onSurfaceVariantSummary,
                                    modifier = Modifier.padding(start = 18.dp, top = 8.dp),
                                )
                            } else {
                                Column(
                                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    setTodos.forEach { todo ->
                                        TodoCard(
                                            todo = todo,
                                            running = false,
                                            refreshKey = refresh,
                                            onStart = {
                                                if (FocusTimer.state.value != null) {
                                                    navController.push(Route.Focus)
                                                } else {
                                                    FocusTimer.start(todo)
                                                    navController.push(Route.Focus)
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (creating || editing != null) {
        val base = editing
        SetEditorDialog(
            initial = base,
            onDismiss = { creating = false; editing = null },
            onSave = { set ->
                if (base == null) {
                    TodoStore.insertTodoSet(set)
                } else {
                    TodoStore.updateTodoSet(set)
                }
                sets = TodoStore.todoSets()
                refresh++
                creating = false
                editing = null
            },
        )
    }

    menuSet?.let { set ->
        OverlayDialog(
            show = true,
            title = set.name,
            summary = "选择要做的操作",
            onDismissRequest = { menuSet = null },
        ) {
            Column {
                listOf(
                    "重命名" to { renaming = set; menuSet = null },
                    "子待办排序" to { sorting = set; menuSet = null },
                    "删除待办集" to { deleting = set; menuSet = null },
                ).forEach { (label, action) ->
                    Text(
                        label,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { action() }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        }
    }

    sorting?.let { set ->
        val list = remember(set.id, refresh) { TodoStore.todos(set.id) }
        OverlayDialog(
            show = true,
            title = "子待办排序",
            summary = "调整「${set.name}」里的顺序",
            onDismissRequest = { sorting = null },
        ) {
            Column {
                if (list.isEmpty()) {
                    Text("这个集里还没有待办", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                }
                list.forEachIndexed { index, todo ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${index + 1}. ${todo.title}",
                            fontSize = 14.sp,
                            color = colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = "上移",
                            onClick = { TodoStore.moveTodo(todo, up = true); refresh++ },
                        )
                        TextButton(
                            text = "下移",
                            onClick = { TodoStore.moveTodo(todo, up = false); refresh++ },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(
                    text = "完成",
                    onClick = { sorting = null },
                    modifier = Modifier.fillMaxWidth(),
                    colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }

    addTo?.let { set ->
        var showNew by remember(set.id) { mutableStateOf(false) }
        OverlayDialog(
            show = true,
            title = "添加到「${set.name}」",
            summary = "可以新建，也可以从已有待办里挑",
            onDismissRequest = { addTo = null },
        ) {
            Column {
                TextButton(
                    text = "新建待办",
                    onClick = { showNew = true; newTodoFor = set; addTo = null },
                    modifier = Modifier.fillMaxWidth(),
                    colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary(),
                )
                Spacer(Modifier.height(6.dp))
                val candidates = remember(set.id, refresh) { TodoStore.todos().filter { it.setId != set.id } }
                if (candidates.isEmpty()) {
                    Text(
                        "没有可以挑的待办，先新建一条吧",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                } else {
                    candidates.take(12).forEach { todo ->
                        Text(
                            todo.title,
                            fontSize = 14.sp,
                            color = colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    TodoStore.assignTodoToSet(todo.id, set.id)
                                    sets = TodoStore.todoSets()
                                    refresh++
                                    addTo = null
                                }
                                .padding(vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }

    newTodoFor?.let { set ->
        AddTodoDialog(
            onDismiss = { newTodoFor = null },
            initial = Todo(title = "", setId = set.id),
            onCreate = { todo ->
                TodoStore.insertTodo(todo.copy(setId = set.id))
                sets = TodoStore.todoSets()
                refresh++
                expandedId = set.id
                newTodoFor = null
            },
        )
    }

    renaming?.let { set ->
        InputDialog(
            show = true,
            title = "重命名待办集",
            initial = set.name,
            onDismiss = { renaming = null },
            onConfirm = { name ->
                TodoStore.renameTodoSet(set.id, name)
                sets = TodoStore.todoSets()
                renaming = null
            },
        )
    }

    deleting?.let { set ->
        ConfirmDialog(
            show = true,
            title = "删除待办集",
            summary = "删除「${set.name}」？里面的待办会保留。",
            confirmText = "删除",
            onDismiss = { deleting = null },
            onConfirm = {
                TodoStore.deleteTodoSet(set.id)
                sets = TodoStore.todoSets()
                deleting = null
            },
        )
    }
}

@Composable
private fun SetEditorDialog(
    initial: TodoSet?,
    onDismiss: () -> Unit,
    onSave: (TodoSet) -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember { mutableIntStateOf(initial?.color ?: 0) }
    var restMin by remember { mutableIntStateOf(initial?.restMin ?: 5) }
    var longRestMin by remember { mutableIntStateOf(initial?.longRestMin ?: 15) }
    var autoNext by remember { mutableStateOf(initial?.autoNext ?: false) }

    OverlayDialog(
        show = true,
        title = if (initial == null) "添加待办集" else "待办集设置",
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 520.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = "待办集名称",
            )
            Spacer(Modifier.height(14.dp))
            Text("颜色", fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                setColors.chunked(5).forEachIndexed { rowIndex, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        row.forEachIndexed { colIndex, c ->
                            val index = rowIndex * 5 + colIndex
                            Box(
                                modifier = Modifier
                                    .size(if (index == color) 34.dp else 28.dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .clickable { color = index },
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            StepperLine(
                title = "完成单个待办后休息",
                value = "$restMin 分钟",
                onMinus = { restMin = (restMin - 1).coerceAtLeast(1) },
                onPlus = { restMin = (restMin + 1).coerceAtMost(60) },
            )
            StepperLine(
                title = "全部完成后的长休息",
                value = "$longRestMin 分钟",
                onMinus = { longRestMin = (longRestMin - 5).coerceAtLeast(5) },
                onPlus = { longRestMin = (longRestMin + 5).coerceAtMost(120) },
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("连续执行子待办", fontSize = 13.sp, color = colorScheme.onSurface, modifier = Modifier.weight(1f))
                androidx.compose.material3.Switch(checked = autoNext, onCheckedChange = { autoNext = it })
            }
            Text(
                "仅支持倒计时；开启后按顺序自动进入下一个待办",
                fontSize = 11.sp,
                color = colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(text = "取消", onClick = onDismiss, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                TextButton(
                    text = "保存",
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(
                                (initial ?: TodoSet(name = name)).copy(
                                    name = name.trim(),
                                    color = color,
                                    restMin = restMin,
                                    longRestMin = longRestMin,
                                    autoNext = autoNext,
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
private fun StepperLine(title: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 13.sp, color = colorScheme.onSurface, modifier = Modifier.weight(1f))
        TextButton(text = "−", onClick = onMinus)
        Text(value, fontSize = 13.sp, color = colorScheme.onSurface)
        TextButton(text = "+", onClick = onPlus)
    }
}
