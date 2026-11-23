package com.tomato.focus.ui.screen.me

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.tomato.focus.ui.theme.tpTextSub
import com.tomato.focus.ui.theme.tpCardBg
import com.tomato.focus.ui.theme.tpPageBg
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.tomato.focus.data.Backup
import com.tomato.focus.data.TodoStore
import com.tomato.focus.ui.screen.todo.defaultFocusMinutes
import com.tomato.focus.ui.screen.todo.saveDefaultFocusMinutes
import com.tomato.focus.ui.navigation3.Navigator
import com.tomato.focus.ui.navigation3.Route
import com.tomato.focus.ui.theme.TP

@Composable
fun MeScreen(navController: Navigator, bottomPadding: Dp) {
    var tip by remember { mutableStateOf<String?>(null) }
    var showMinutes by remember { mutableStateOf(false) }
    var defaultMinutes by remember { mutableStateOf(defaultFocusMinutes()) }
    var backupTip by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(Backup.exportJson().toByteArray())
                }
            }.onSuccess { backupTip = "已导出，文件在你自己选的位置" }
                .onFailure { backupTip = "导出失败：${it.message}" }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()
                    ?.use { it.readText() } ?: error("读不到文件")
                Backup.importJson(text)
            }.onSuccess { r ->
                backupTip = "恢复完成：待办集 ${r.sets}，待办 ${r.todos}，记录 ${r.sessions}"
                refreshKey++
            }.onFailure { backupTip = "恢复失败：${it.message}" }
        }
    }
    val totalSeconds = remember(refreshKey) { TodoStore.summary(0L, Long.MAX_VALUE / 2).seconds }
    val totalDays = remember(refreshKey) {
        TodoStore.sessionsBetween(0L, Long.MAX_VALUE / 2)
            .map { java.time.LocalDate.ofEpochDay(it.startTs / 86_400_000L) }
            .distinct()
            .size
    }

    Column(modifier = Modifier.fillMaxSize().background(tpPageBg())) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TP.headerBrush())
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF1E6)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🍅", fontSize = 30.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("未登录", fontSize = 19.sp, fontWeight = FontWeight.Medium, color = TP.TextDark)
                    Spacer(Modifier.height(3.dp))
                    Text("点击默认头像登录（暂未接入账号）", fontSize = 12.sp, color = TP.TextDark.copy(alpha = 0.6f))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("共专注 $totalDays 天")
                Chip("累计 ${totalSeconds / 3600} 小时")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(tpCardBg()),
            ) {
                Item("⏱", "专注记录", "每一次专注都会存档") { navController.push(Route.History) }
                Item("🎨", "海报背景和外观", "待办与专注背景、深色模式") { navController.push(Route.Settings) }
                Item(
                    "🎯",
                    "默认番茄时长",
                    "新建待办时默认 $defaultMinutes 分钟，点这里改",
                ) { showMinutes = true }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(tpCardBg()),
            ) {
                Item("⬆️", "备份到文件", "把待办和专注记录导出成 JSON") {
                    exportLauncher.launch("tomatoplan-backup.json")
                }
                Item("⬇️", "从文件恢复", "从备份 JSON 里把数据导回来") {
                    importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                }
                Item("⚙️", "设置", "主题、界面风格、缩放") { navController.push(Route.Settings) }
                Item("ℹ️", "关于", "版本信息与开源许可") { navController.push(Route.About) }
                Item("🛡", "权限", "通知、悬浮窗、电池优化") { navController.push(Route.Permissions) }
            }
            Spacer(Modifier.height(bottomPadding + 24.dp))
        }
    }

    if (showMinutes) {
        AlertDialog(
            onDismissRequest = { showMinutes = false },
            title = { Text("默认番茄时长") },
            text = {
                Column {
                    Text("新建待办时用这个时长，也可以单条改。", fontSize = 13.sp, color = tpTextSub())
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 25, 30, 45, 60).forEach { min ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (min == defaultMinutes) TP.Teal.copy(alpha = 0.18f) else Color(0xFFF2F5F7))
                                    .clickable {
                                        defaultMinutes = min
                                        saveDefaultFocusMinutes(min)
                                        showMinutes = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                            ) {
                                Text(
                                    "$min",
                                    fontSize = 13.sp,
                                    color = if (min == defaultMinutes) TP.Teal else TP.TextDark,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showMinutes = false }) { Text("关闭") } },
        )
    }

    backupTip?.let { text ->
        AlertDialog(
            onDismissRequest = { backupTip = null },
            confirmButton = { TextButton(onClick = { backupTip = null }) { Text("好") } },
            text = { Text(text) },
        )
    }

    tip?.let {
        AlertDialog(
            onDismissRequest = { tip = null },
            confirmButton = { TextButton(onClick = { tip = null }) { Text("知道了") } },
            text = { Text(it) },
        )
    }
}

@Composable
private fun Chip(text: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.75f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, fontSize = 11.sp, color = TP.Coral)
    }
}

@Composable
private fun Item(emoji: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFF4F7F9)),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 15.sp, color = TP.TextDark)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = tpTextSub())
        }
    }
}
