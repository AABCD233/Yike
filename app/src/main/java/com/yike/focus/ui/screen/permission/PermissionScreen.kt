package com.yike.focus.ui.screen.permission

import android.content.Context
import android.os.PowerManager
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsPaused
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.yike.focus.data.AppGuard
import com.yike.focus.ui.navigation3.LocalNavigator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import kotlinx.coroutines.delay

/** 权限页：只放这个 App 真正用到的四项 */
@Composable
fun PermissionScreen() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1200)
            tick++
        }
    }

    val appList = remember(tick) { AppGuard.hasUsageAccess() }
    val overlay = remember(tick) { AppGuard.hasOverlayAccess() }
    val notification = remember(tick) {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    val battery = remember(tick) {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(context.packageName)
    }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 6.dp, end = 16.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navigator.pop() }) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "返回", tint = colorScheme.onBackground)
            }
            Text("权限", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                PermissionRow(
                    icon = Icons.Rounded.Layers,
                    title = "应用列表",
                    summary = "读取「正在使用哪个 App」，硬核模式靠它判断要不要拦",
                    granted = appList,
                    actionText = "去开启",
                    onAction = { AppGuard.openUsageAccessSettings(context) },
                )
            }
            item {
                PermissionRow(
                    icon = Icons.Rounded.PictureInPicture,
                    title = "显示在其他应用上层",
                    summary = "拦截页要盖在其他应用之上",
                    granted = overlay,
                    actionText = "去开启",
                    onAction = { AppGuard.openOverlaySettings(context) },
                )
            }
            item {
                PermissionRow(
                    icon = Icons.Rounded.Notifications,
                    title = "通知",
                    summary = "番茄结束、离开计时页的提醒",
                    granted = notification,
                    actionText = "去开启",
                    onAction = {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        runCatching { context.startActivity(intent) }
                    },
                )
            }
            item {
                PermissionRow(
                    icon = Icons.Rounded.NotificationsPaused,
                    title = "勿扰访问",
                    summary = "计时开始自动开勿扰、结束自动关",
                    granted = com.yike.focus.data.Dnd.granted(),
                    actionText = "去开启",
                    onAction = {
                        runCatching {
                            context.startActivity(
                                android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    },
                )
            }
            item {
                PermissionRow(
                    icon = Icons.Rounded.FolderOpen,
                    title = "所有文件访问",
                    summary = "选音乐目录、读取本地音频要用",
                    granted = android.os.Environment.isExternalStorageManager(),
                    actionText = "去开启",
                    onAction = {
                        runCatching {
                            context.startActivity(
                                android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                    .setData(android.net.Uri.parse("package:${context.packageName}"))
                                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    },
                )
            }
            item {
                PermissionRow(
                    icon = Icons.Rounded.BatterySaver,
                    title = "忽略电池优化",
                    summary = "后台计时和守护不被系统清理",
                    granted = battery,
                    actionText = "去设置",
                    onAction = {
                        runCatching {
                            context.startActivity(
                                android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    summary: String,
    granted: Boolean,
    actionText: String,
    onAction: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(end = 12.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontSize = 15.sp, color = colorScheme.onSurface)
                    Spacer(Modifier.padding(start = 6.dp))
                    Text(
                        if (granted) "已开启" else "未开启",
                        fontSize = 11.sp,
                        color = if (granted) colorScheme.primary else colorScheme.onSurfaceVariantSummary,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(summary, fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
            }
            if (!granted) {
                Card(onClick = onAction, showIndication = true) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(actionText, fontSize = 13.sp, color = colorScheme.primary)
                    }
                }
            }
        }
    }
}
