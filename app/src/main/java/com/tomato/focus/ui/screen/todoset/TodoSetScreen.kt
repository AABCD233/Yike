package com.tomato.focus.ui.screen.todoset

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.tomato.focus.ui.theme.tpTextSub
import com.tomato.focus.ui.theme.tpCardBg
import com.tomato.focus.ui.theme.tpPageBg
import com.tomato.focus.data.TodoStore
import com.tomato.focus.ui.navigation3.Navigator
import com.tomato.focus.ui.navigation3.Route
import com.tomato.focus.ui.theme.TP

@Composable
fun TodoSetScreen(
    navController: Navigator,
    bottomPadding: Dp,
) {
    var sets by remember { mutableStateOf(TodoStore.todoSets()) }
    var expandedId by remember { mutableStateOf<Long?>(null) }
    var menuFor by remember { mutableStateOf<Long?>(null) }
    var renaming by remember { mutableStateOf<com.tomato.focus.data.TodoSet?>(null) }
    var deleting by remember { mutableStateOf<com.tomato.focus.data.TodoSet?>(null) }
    var newSetName by remember { mutableStateOf("") }
    var adding by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(tpPageBg())) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TP.headerBrush())
                .statusBarsPadding()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("待办集", fontSize = 26.sp, fontWeight = FontWeight.Medium, color = TP.TextDark)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { adding = true }) {
                    Icon(Icons.Rounded.Add, contentDescription = "新建待办集", tint = TP.TextDark.copy(alpha = 0.8f))
                }
            }
            Text(
                "把同一类待办放进一个集合，统计时按集合看进度",
                fontSize = 12.sp,
                color = TP.TextDark.copy(alpha = 0.65f),
            )
            if (adding) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        if (newSetName.isEmpty()) {
                            Text("待办集名称", fontSize = 13.sp, color = tpTextSub())
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = newSetName,
                            onValueChange = { newSetName = it },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = TP.TextDark, fontSize = 13.sp),
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(TP.Teal)
                            .clickable {
                                if (newSetName.isNotBlank()) {
                                    TodoStore.insertTodoSet(
                                        com.tomato.focus.data.TodoSet(
                                            name = newSetName.trim(),
                                            color = sets.size,
                                        )
                                    )
                                    sets = TodoStore.todoSets()
                                    newSetName = ""
                                    adding = false
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text("创建", fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }

        if (sets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("还没有待办集，点右上角 + 新建", fontSize = 13.sp, color = tpTextSub())
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp, 12.dp, 12.dp, bottomPadding + 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(sets, key = { it.id }) { set ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(tpCardBg()),
                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedId = if (expandedId == set.id) null else set.id }
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(56.dp)
                                .background(TP.chartColor(set.color)),
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
                            Text(set.name, fontSize = 16.sp, color = TP.TextDark)
                            Text(
                                "${TodoStore.countTodoInSet(set.id)} 个待办",
                                fontSize = 12.sp,
                                color = tpTextSub(),
                            )
                        }
                        Box {
                            IconButton(onClick = { menuFor = set.id }) {
                                Icon(
                                    Icons.Rounded.MoreVert,
                                    contentDescription = "更多操作",
                                    tint = tpTextSub(),
                                )
                            }
                            DropdownMenu(
                                expanded = menuFor == set.id,
                                onDismissRequest = { menuFor = null },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("重命名") },
                                    onClick = { menuFor = null; renaming = set },
                                )
                                DropdownMenuItem(
                                    text = { Text("删除") },
                                    onClick = { menuFor = null; deleting = set },
                                )
                            }
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = tpTextSub())
                    }
                        if (expandedId == set.id) {
                            val setTodos = remember(set.id, expandedId) { TodoStore.todos(set.id) }
                            if (setTodos.isEmpty()) {
                                Text(
                                    "这个待办集里还没有待办",
                                    fontSize = 12.sp,
                                    color = tpTextSub(),
                                    modifier = Modifier.padding(start = 18.dp, bottom = 12.dp),
                                )
                            } else {
                                Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
                                    setTodos.forEach { todo ->
                                        com.tomato.focus.ui.screen.todo.TodoCard(
                                            todo = todo,
                                            running = false,
                                            onStart = {
                                                com.tomato.focus.data.FocusTimer.start(todo)
                                                navController.push(Route.Focus)
                                            },
                                        )
                                        Spacer(Modifier.height(8.dp))
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
        var text by remember(set.id) { mutableStateOf(set.name) }
        AlertDialog(
            onDismissRequest = { renaming = null },
            title = { Text("重命名待办集") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (text.isNotBlank()) {
                        TodoStore.renameTodoSet(set.id, text.trim())
                        sets = TodoStore.todoSets()
                        renaming = null
                    }
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { renaming = null }) { Text("取消") } },
        )
    }

    deleting?.let { set ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("删除待办集") },
            text = { Text("删除「${set.name}」？里面的待办会保留，只是不再属于这个集合。") },
            confirmButton = {
                TextButton(onClick = {
                    TodoStore.deleteTodoSet(set.id)
                    sets = TodoStore.todoSets()
                    deleting = null
                }) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("取消") } },
        )
    }

}
