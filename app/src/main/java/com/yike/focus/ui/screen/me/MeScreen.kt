package com.yike.focus.ui.screen.me

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.Backup
import com.yike.focus.data.TodoStore
import com.yike.focus.ui.navigation3.Navigator
import com.yike.focus.ui.navigation3.Route
import com.yike.focus.ui.screen.todo.defaultFocusMinutes
import com.yike.focus.ui.screen.todo.saveDefaultFocusMinutes
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

@Composable
fun MeScreen(navController: Navigator, bottomPadding: Dp) {
    var tip by remember { mutableStateOf<String?>(null) }
    var showMinutes by remember { mutableStateOf(false) }
    var defaultMinutes by remember { mutableStateOf(defaultFocusMinutes()) }
    var backupTip by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val totalSeconds = remember(refreshKey) { TodoStore.summary(0L, Long.MAX_VALUE / 2).seconds }
    val totalDays = remember(refreshKey) {
        TodoStore.sessionsBetween(0L, Long.MAX_VALUE / 2)
            .map { it.startTs / 86_400_000L }
            .distinct()
            .size
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(Backup.exportJson().toByteArray())
                }
            }.onSuccess { backupTip = "已导出到你选的位置" }
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(modifier = Modifier.fillMaxSize(), colors = CardDefaults.defaultColors(color = colorScheme.secondaryContainer)) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.size(12.dp))
                Column {
                    Text("未登录", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = colorScheme.onBackground)
                    Text(
                        "本地记录，不需要账号",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }

        item {
            Card {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("累计专注", fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
                        Text(
                            "${totalSeconds / 3600} 小时",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurface,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("专注天数", fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
                        Text(
                            "$totalDays 天",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        item {
            Card {
                ArrowPreference(
                    title = "专注记录",
                    summary = "每一次专注都会存档",
                    startAction = { PrefIcon(Icons.Rounded.History) },
                    onClick = { navController.push(Route.History) },
                )
                ArrowPreference(
                    title = "默认番茄时长",
                    summary = "新建待办时默认 $defaultMinutes 分钟",
                    startAction = { PrefIcon(Icons.Rounded.Timer) },
                    onClick = { showMinutes = true },
                )
                ArrowPreference(
                    title = "外观",
                    summary = "主题、界面风格、缩放",
                    startAction = { PrefIcon(Icons.Rounded.DarkMode) },
                    onClick = { navController.push(Route.Settings) },
                )
            }
        }

        item {
            Card {
                ArrowPreference(
                    title = "备份到文件",
                    summary = "导出待办和专注记录",
                    startAction = { PrefIcon(Icons.Rounded.Backup) },
                    onClick = { exportLauncher.launch("yike-backup.json") },
                )
                ArrowPreference(
                    title = "从文件恢复",
                    summary = "从备份 JSON 导回来",
                    startAction = { PrefIcon(Icons.Rounded.Restore) },
                    onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                )
            }
        }

        item {
            Card {
                ArrowPreference(
                    title = "设置",
                    summary = "界面、主题、更新检查",
                    startAction = { PrefIcon(Icons.Rounded.Settings) },
                    onClick = { navController.push(Route.Settings) },
                )
                ArrowPreference(
                    title = "权限",
                    summary = "通知、悬浮窗、电池优化",
                    startAction = { PrefIcon(Icons.Rounded.Security) },
                    onClick = { navController.push(Route.Permissions) },
                )
                ArrowPreference(
                    title = "关于",
                    summary = "版本信息与开源许可",
                    startAction = { PrefIcon(Icons.Rounded.Info) },
                    onClick = { navController.push(Route.About) },
                )
            }
        }
    }

    if (showMinutes) {
        AlertDialog(
            onDismissRequest = { showMinutes = false },
            title = { androidx.compose.material3.Text("默认番茄时长") },
            text = {
                Column {
                    androidx.compose.material3.Text("新建待办时用这个时长，也可以单条改。")
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 25, 30, 45, 60).forEach { min ->
                            Card(
                                onClick = {
                                    defaultMinutes = min
                                    saveDefaultFocusMinutes(min)
                                    showMinutes = false
                                },
                                showIndication = true,
                                colors = CardDefaults.defaultColors(
                                    color = if (min == defaultMinutes) colorScheme.primaryContainer
                                    else colorScheme.surfaceContainerHighest
                                ),
                            ) {
                                androidx.compose.material3.Text(
                                    "$min",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMinutes = false }) { androidx.compose.material3.Text("关闭") }
            },
        )
    }

    backupTip?.let { text ->
        AlertDialog(
            onDismissRequest = { backupTip = null },
            confirmButton = { TextButton(onClick = { backupTip = null }) { androidx.compose.material3.Text("好") } },
            text = { androidx.compose.material3.Text(text) },
        )
    }

    tip?.let { text ->
        AlertDialog(
            onDismissRequest = { tip = null },
            confirmButton = { TextButton(onClick = { tip = null }) { androidx.compose.material3.Text("知道了") } },
            text = { androidx.compose.material3.Text(text) },
        )
    }
}

@Composable
private fun PrefIcon(icon: ImageVector) {
    Icon(
        icon,
        contentDescription = null,
        tint = colorScheme.onSurface,
        modifier = Modifier.padding(end = 6.dp).size(22.dp),
    )
}
