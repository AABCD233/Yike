package com.yike.focus.ui.screen.history

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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.FocusSession
import com.yike.focus.data.TodoStore
import com.yike.focus.ui.navigation3.Navigator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun HistoryScreen(navController: Navigator) {
    var refresh by remember { mutableStateOf(0) }
    val sessions = remember(refresh) {
        TodoStore.sessionsBetween(0L, Long.MAX_VALUE / 2).sortedByDescending { it.startTs }
    }
    val byDay = remember(refresh, sessions) {
        sessions.groupBy { Instant.ofEpochMilli(it.startTs).atZone(ZoneId.systemDefault()).toLocalDate() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 6.dp, end = 16.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navController.pop() }) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "返回", tint = colorScheme.onBackground)
            }
            Text("专注记录", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
        }

        if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("还没有专注记录", fontSize = 14.sp, color = colorScheme.onSurfaceVariantSummary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                byDay.forEach { (day, list) ->
                    item(key = "h-$day") {
                        DayHeader(day, list)
                    }
                    items(list) { session ->
                        SessionRow(session) {
                            TodoStore.deleteSession(session.id)
                            refresh++
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(day: LocalDate, list: List<FocusSession>) {
    val total = list.filter { !it.abandoned }.sumOf { it.seconds }
    Text(
        "$day · ${list.count { !it.abandoned }} 次 · ${total / 60} 分钟",
        fontSize = 12.sp,
        color = colorScheme.onSurfaceVariantSummary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp),
    )
}

@Composable
private fun SessionRow(session: FocusSession, onDelete: () -> Unit) {
    val start = Instant.ofEpochMilli(session.startTs).atZone(ZoneId.systemDefault())
    val end = Instant.ofEpochMilli(session.endTs).atZone(ZoneId.systemDefault())
    val kindLabel = when (session.kind) {
        "lock" -> "锁机"
        "relax" -> "休息"
        else -> "专注"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("$kindLabel · ${session.title}", fontSize = 15.sp, color = colorScheme.onSurface)
                Spacer(Modifier.height(3.dp))
                Text(
                    "%02d:%02d - %02d:%02d · %d 分钟%s".format(
                        start.hour, start.minute, end.hour, end.minute,
                        session.seconds / 60,
                        if (session.abandoned) " · 已放弃" else "",
                    ),
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "删除记录", tint = colorScheme.onSurfaceVariantSummary)
            }
        }
    }
}
