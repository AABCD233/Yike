package com.yike.focus.ui.screen.todoset

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.FocusTimer
import com.yike.focus.data.TodoSet
import com.yike.focus.data.TodoStore
import com.yike.focus.ui.navigation3.Navigator
import com.yike.focus.ui.navigation3.Route
import com.yike.focus.ui.component.miuix.ConfirmDialog
import com.yike.focus.ui.component.miuix.InputDialog
import com.yike.focus.ui.screen.todo.TodoCard
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

@Composable
fun TodoSetScreen(navController: Navigator, bottomPadding: Dp) {
    var sets by remember { mutableStateOf(TodoStore.todoSets()) }
    var newSetName by remember { mutableStateOf("") }
    var adding by remember { mutableStateOf(false) }
    var expandedId by remember { mutableStateOf<Long?>(null) }
    var menuFor by remember { mutableStateOf<Long?>(null) }
    var renaming by remember { mutableStateOf<TodoSet?>(null) }
    var deleting by remember { mutableStateOf<TodoSet?>(null) }

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
            IconButton(onClick = { adding = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "新建待办集", tint = colorScheme.onBackground)
            }
        }

        if (adding) {
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = newSetName,
                        onValueChange = { newSetName = it },
                        singleLine = true,
                        placeholder = { androidx.compose.material3.Text("待办集名称") },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Card(
                        onClick = {
                            if (newSetName.isNotBlank()) {
                                TodoStore.insertTodoSet(TodoSet(name = newSetName.trim(), color = sets.size))
                                sets = TodoStore.todoSets()
                                newSetName = ""
                                adding = false
                            }
                        },
                        showIndication = true,
                    ) {
                        androidx.compose.material3.Text(
                            "创建",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
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
                                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(set.name, fontSize = 16.sp, color = colorScheme.onSurface)
                                    Text(
                                        "${TodoStore.countTodoInSet(set.id)} 个待办",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                                Box {
                                    IconButton(onClick = { menuFor = set.id }, modifier = Modifier.size(36.dp)) {
                                        Icon(
                                            Icons.Rounded.MoreVert,
                                            contentDescription = "更多操作",
                                            tint = colorScheme.onSurfaceVariantSummary,
                                        )
                                    }
                                    DropdownMenu(expanded = menuFor == set.id, onDismissRequest = { menuFor = null }) {
                                        DropdownMenuItem(
                                            text = { androidx.compose.material3.Text("重命名") },
                                            onClick = { menuFor = null; renaming = set },
                                        )
                                        DropdownMenuItem(
                                            text = { androidx.compose.material3.Text("删除") },
                                            onClick = { menuFor = null; deleting = set },
                                        )
                                    }
                                }
                                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colorScheme.onSurfaceVariantSummary)
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                        if (expandedId == set.id) {
                            val setTodos = remember(set.id, expandedId) { TodoStore.todos(set.id) }
                            if (setTodos.isEmpty()) {
                                Text(
                                    "这个待办集里还没有待办",
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
