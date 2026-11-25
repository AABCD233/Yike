package com.yike.focus.ui.screen.setup

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsPaused
import androidx.compose.material.icons.rounded.PictureInPicture
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
import com.yike.focus.data.Dnd
import com.yike.focus.service.LockAccessibility
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import kotlinx.coroutines.delay

/** 第一次打开时一次性把要用的权限列出来，省得后面缺一个弹一次 */
@Composable
fun PermissionSetupScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            tick++
        }
    }

    val usage = remember(tick) { AppGuard.hasUsageAccess() }
    val overlay = remember(tick) { AppGuard.hasOverlayAccess() }
    val notification = remember(tick) { NotificationManagerCompat.from(context).areNotificationsEnabled() }
    val accessibility = remember(tick) { LockAccessibility.enabled(context) }
    val dnd = remember(tick) { Dnd.granted(context) }
    val allFiles = remember(tick) { android.os.Environment.isExternalStorageManager() }
    val battery = remember(tick) {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(context.packageName)
    }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(20.dp)) {
            Text("先给几个权限", fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
            Spacer(Modifier.height(6.dp))
            Text(
                "都是本地功能要用的，可以现在给，也可以跳过以后在「我的 → 权限」里开。",
                fontSize = 13.sp,
                color = colorScheme.onSurfaceVariantSummary,
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SetupRow(Icons.Rounded.Notifications, "通知", "专注结束、每日提醒", notification) {
                    runCatching {
                        context.startActivity(
                            android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
            }
            item {
                SetupRow(Icons.Rounded.Accessibility, "无障碍", "锁机时拦住返回和主页键", accessibility) {
                    LockAccessibility.openSettings(context)
                }
            }
            item {
                SetupRow(Icons.Rounded.Layers, "应用列表", "硬核模式判断你在用哪个应用", usage) {
                    AppGuard.openUsageAccessSettings(context)
                }
            }
            item {
                SetupRow(Icons.Rounded.PictureInPicture, "悬浮窗", "拦截页要盖在其他应用之上", overlay) {
                    AppGuard.openOverlaySettings(context)
                }
            }
            item {
                SetupRow(Icons.Rounded.NotificationsPaused, "勿扰访问", "计时时自动开勿扰", dnd) {
                    runCatching {
                        context.startActivity(
                            android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
            }
            item {
                SetupRow(Icons.Rounded.FolderOpen, "所有文件访问", "选本地音乐目录", allFiles) {
                    runCatching {
                        context.startActivity(
                            android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                .setData(android.net.Uri.parse("package:${context.packageName}"))
                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
            }
            item {
                SetupRow(Icons.Rounded.BatterySaver, "忽略电池优化", "后台计时和守护不被清理", battery) {
                    runCatching {
                        context.startActivity(
                            android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    text = "开始使用",
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}

@Composable
private fun SetupRow(
    icon: ImageVector,
    title: String,
    summary: String,
    granted: Boolean,
    onAction: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onAction, showIndication = true) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = colorScheme.onSurfaceVariantSummary)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontSize = 15.sp, color = colorScheme.onSurface)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (granted) "已开启" else "未开启",
                        fontSize = 11.sp,
                        color = if (granted) colorScheme.primary else colorScheme.onSurfaceVariantSummary,
                    )
                }
                Text(summary, fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
            }
            if (!granted) Text("去开启", fontSize = 13.sp, color = colorScheme.primary)
        }
    }
}
