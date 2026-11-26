package com.yike.focus.ui.screen.boost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.AppGuard
import com.yike.focus.data.BoostSettings
import com.yike.focus.ui.navigation3.LocalNavigator
import com.yike.focus.ui.navigation3.Route
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

@Composable
fun BoostScreen() {
    val navigator = LocalNavigator.current
    var hardcore by remember { mutableStateOf(BoostSettings.hardcore) }
    var strict by remember { mutableStateOf(BoostSettings.strict) }
    var dnd by remember { mutableStateOf(BoostSettings.dnd) }
    var blockDesktop by remember { mutableStateOf(BoostSettings.blockDesktop) }
    var hideRecents by remember { mutableStateOf(BoostSettings.hideRecents) }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 6.dp, end = 16.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navigator.pop() }) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "返回", tint = colorScheme.onBackground)
            }
            Text("功能增强", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title = "硬核模式",
                        summary = "计时期间跳到白名单以外的应用，就弹拦截页把你拉回来",
                        startAction = { BoostIcon(Icons.Rounded.Block) },
                        checked = hardcore,
                        onCheckedChange = {
                            hardcore = it
                            BoostSettings.hardcore = it
                            if (it && (!AppGuard.hasUsageAccess() || !AppGuard.hasOverlayAccess())) {
                                navigator.push(Route.Whitelist)
                            }
                        },
                    )
                    SwitchPreference(
                        title = "严格模式",
                        summary = "计时中不能暂停、不能提前结束，返回手势也退不出去",
                        startAction = { BoostIcon(Icons.Rounded.Lock) },
                        checked = strict,
                        onCheckedChange = { strict = it; BoostSettings.strict = it },
                    )
                    SwitchPreference(
                        title = "禁止桌面和多任务",
                        summary = if (!AppGuard.hasUsageAccess() || !AppGuard.hasOverlayAccess()) {
                            "缺权限：需要「使用情况访问」和「显示在其他应用上层」，去「我的 → 权限」开启"
                        } else {
                            "专注时上滑回桌面或进最近任务，会被拉回计时页"
                        },
                        startAction = { BoostIcon(Icons.Rounded.Block) },
                        checked = blockDesktop,
                        onCheckedChange = { blockDesktop = it; BoostSettings.blockDesktop = it },
                    )
                    SwitchPreference(
                        title = "在最近任务里隐藏",
                        summary = "专注时本应用不出现在多任务列表里",
                        startAction = { BoostIcon(Icons.Rounded.Apps) },
                        checked = hideRecents,
                        onCheckedChange = { hideRecents = it; BoostSettings.hideRecents = it },
                    )
                    SwitchPreference(
                        title = "勿扰模式",
                        summary = if (com.yike.focus.data.Dnd.granted()) {
                            "计时开始时自动开启勿扰，结束恢复原状"
                        } else {
                            "需要「勿扰访问」权限，去「我的 → 权限」开启"
                        },
                        startAction = { BoostIcon(Icons.Rounded.NotificationsOff) },
                        checked = dnd,
                        onCheckedChange = { dnd = it; BoostSettings.dnd = it },
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    ArrowPreference(
                        title = "白名单应用",
                        summary = "选择计时期间允许使用的应用",
                        startAction = { BoostIcon(Icons.Rounded.Apps) },
                        onClick = { navigator.push(Route.Whitelist) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BoostIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = colorScheme.onSurfaceVariantSummary,
        modifier = Modifier.padding(end = 8.dp),
    )
}
