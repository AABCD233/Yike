package com.yike.focus.ui.screen.whitelist

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.yike.focus.data.AppGuard
import com.yike.focus.ui.component.miuix.ConfirmDialog
import com.yike.focus.ui.navigation3.Navigator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap?,
    val isSystem: Boolean,
)

@Composable
fun WhitelistScreen(navController: Navigator) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var showSystem by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(AppGuard.whitelist()) }
    var permissionTip by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!AppGuard.hasUsageAccess()) {
            permissionTip = "白名单要靠「应用列表」权限判断你正在用哪个应用，先去开一下。"
        } else if (!AppGuard.hasOverlayAccess()) {
            permissionTip = "还需要「显示在其他应用上层」权限，拦截页才弹得出来。"
        }
        apps = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .map { info ->
                    val icon = runCatching {
                        pm.getApplicationIcon(info).toBitmap(96, 96).asImageBitmap()
                    }.getOrNull()
                    AppEntry(
                        packageName = info.packageName,
                        label = pm.getApplicationLabel(info).toString(),
                        icon = icon,
                        isSystem = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    )
                }
                .sortedBy { it.label.lowercase() }
        }
    }

    val filtered = apps.filter { app ->
        (showSystem || !app.isSystem || selected.contains(app.packageName)) &&
            (query.isBlank() || app.label.contains(query, true) || app.packageName.contains(query, true))
    }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
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
                        "硬核模式需要「应用列表」和「悬浮窗」两项权限。",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariantSummary,
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!AppGuard.hasUsageAccess()) {
                            TextAction("开应用列表权限") { AppGuard.openUsageAccessSettings(context) }
                        }
                        if (!AppGuard.hasOverlayAccess()) {
                            TextAction("开悬浮窗") { AppGuard.openOverlaySettings(context) }
                        }
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
            Column {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { androidx.compose.material3.Text("搜索应用") },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("显示系统应用", fontSize = 13.sp, color = colorScheme.onSurface)
                    Spacer(Modifier.weight(1f))
                    Checkbox(checked = showSystem, onCheckedChange = { showSystem = it })
                }
            }
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
                        modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (app.icon != null) {
                            Image(
                                bitmap = app.icon,
                                contentDescription = null,
                                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(9.dp)),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(colorScheme.surfaceContainerHighest),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(app.label, fontSize = 15.sp, color = colorScheme.onSurface)
                            Text(
                                app.packageName,
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariantSummary,
                                maxLines = 1,
                            )
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
