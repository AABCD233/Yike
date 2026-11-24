package com.yike.focus.ui.screen.whitelist

import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.IconButton
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
import com.yike.focus.ui.navigation3.Navigator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class WhitelistedApp(val packageName: String, val label: String, val icon: ImageBitmap?)

/** 计时中打开：只列出白名单里的应用，点一下就跳过去 */
@Composable
fun WhitelistOpenScreen(navController: Navigator) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<WhitelistedApp>>(emptyList()) }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            AppGuard.whitelist().mapNotNull { pkg ->
                runCatching {
                    val info = pm.getApplicationInfo(pkg, 0)
                    WhitelistedApp(
                        packageName = pkg,
                        label = pm.getApplicationLabel(info).toString(),
                        icon = pm.getApplicationIcon(info).toBitmap(96, 96).asImageBitmap(),
                    )
                }.getOrNull()
            }.sortedBy { it.label }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 6.dp, end = 16.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navController.pop() }) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "返回", tint = colorScheme.onBackground)
            }
            Text("白名单应用", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
        }

        if (apps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "还没有加入白名单的应用，去「功能增强 → 白名单应用」里选",
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(apps, key = { it.packageName }) { app ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                            if (intent != null) {
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                runCatching { context.startActivity(intent) }
                            }
                        },
                        showIndication = true,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (app.icon != null) {
                                Image(
                                    bitmap = app.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(9.dp)),
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(app.label, fontSize = 15.sp, color = colorScheme.onSurface, modifier = Modifier.weight(1f))
                            Text("打开", fontSize = 13.sp, color = colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
