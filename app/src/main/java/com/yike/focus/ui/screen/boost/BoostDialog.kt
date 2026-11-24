package com.yike.focus.ui.screen.boost

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.BoostSettings
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.basic.Icon

@Composable
fun BoostDialog(onDismiss: () -> Unit) {
    var hardcore by remember { mutableStateOf(BoostSettings.hardcore) }
    var strict by remember { mutableStateOf(BoostSettings.strict) }
    var blockNotify by remember { mutableStateOf(BoostSettings.blockNotify) }
    var whitelist by remember { mutableStateOf(BoostSettings.whitelistEnabled) }

    OverlayDialog(
        show = true,
        title = "功能增强",
        summary = "计时开始后更严格，适合需要管住自己的时候",
        onDismissRequest = onDismiss,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            BoostSwitch(
                icon = Icons.Rounded.Block,
                title = "硬核模式",
                summary = "计时期间阻止其他应用打开（需要额外授权，后续版本生效）",
                checked = hardcore,
            ) { hardcore = it; BoostSettings.hardcore = it }
            BoostSwitch(
                icon = Icons.Rounded.Lock,
                title = "严格模式",
                summary = "计时中不能暂停、不能提前结束",
                checked = strict,
            ) { strict = it; BoostSettings.strict = it }
            BoostSwitch(
                icon = Icons.Rounded.NotificationsOff,
                title = "拦截通知",
                summary = "计时期间拦截其他应用通知（需要通知使用权，后续版本生效）",
                checked = blockNotify,
            ) { blockNotify = it; BoostSettings.blockNotify = it }
            BoostSwitch(
                icon = Icons.Rounded.Apps,
                title = "白名单",
                summary = "允许在计时期间使用的应用",
                checked = whitelist,
            ) { whitelist = it; BoostSettings.whitelistEnabled = it }

            TextButton(
                text = "完成",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}

@Composable
private fun BoostSwitch(
    icon: ImageVector,
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SwitchPreference(
        title = title,
        summary = summary,
        startAction = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(end = 8.dp),
            )
        },
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}
