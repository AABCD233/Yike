package com.yike.focus.ui.screen.whitelist

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.AppGuard
import com.yike.focus.ui.component.miuix.ConfirmDialog
import com.yike.focus.ui.navigation3.Navigator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class AppEntry(val packageName: String, val label: String)

@Composable
fun WhitelistScreen(navController: Navigator) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(AppGuard.whitelist()) }
    var permissionTip by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!AppGuard.hasUsageAccess()) {
            permissionTip = "拦截需要「使用情况访问」权限，点下面的按钮去开。"
        } else if (!AppGuard.hasOverlayAccess()) {
            permissionTip = "还需要「显示在其他应用上层」权限才能弹出拦截页。"
        }
        apps = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                .map { AppEntry(it.packageName, pm.getApplicationLabel(it).toString()) }
                .sortedBy { it.label }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 6.dp, end = 16.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.pop() }) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "返回", tint = colorScheme.onBackground)
                }
                Text("白名单", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
            }

            if (!AppGuard.hasUsageAccess() || !AppGuard.hasOverlayAccess()) {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("还差权限", fontSize = 15.sp, color = colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "硬核模式要用「使用情况访问」判断你在用哪个应用，用「悬浮窗」权限弹拦截页。",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariantSummary,
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (!AppGuard.hasUsageAccess()) {
                                TextAction("开使用情况访问") { AppGuard.openUsageAccessSettings(context) }
                            }
                            if (!AppGuard.hasOverlayAccess()) {
                                TextAction("开悬浮窗") { AppGuard.openOverlaySettings(context) }
                            }
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { androidx.compose.material3.Text("搜索应用") },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                )
            }

            val filtered = apps.filter {
                query.isBlank() || it.label.contains(query, true) || it.packageName.contains(query, true)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filtered, key = { it.packageName }) { app ->
                    val checked = selected.contains(app.packageName)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            selected = if (checked) selected - app.packageName else selected + app.packageName
                            AppGuard.setWhitelist(selected)
                        },
                        showIndication = true,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.label, fontSize = 15.sp, color = colorScheme.onSurface)
                                Text(app.packageName, fontSize = 11.sp, color = colorScheme.onSurfaceVariantSummary)
                            }
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    selected = if (checked) selected - app.packageName else selected + app.packageName
                                    AppGuard.setWhitelist(selected)
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    permissionTip?.let {
        ConfirmDialog(
            show = true,
            title = "需要权限",
            summary = it,
            confirmText = "知道了",
            dismissText = null,
            onConfirm = { permissionTip = null },
            onDismiss = { permissionTip = null },
        )
    }
}

@Composable
private fun TextAction(text: String, onClick: () -> Unit) {
    Card(onClick = onClick, showIndication = true) {
        Text(
            text,
            fontSize = 13.sp,
            color = colorScheme.primary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
