package com.yike.focus.ui.screen.boost

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.yike.focus.data.BoostSettings

@Composable
fun BoostDialog(onDismiss: () -> Unit) {
    var hardcore by remember { mutableStateOf(BoostSettings.hardcore) }
    var strict by remember { mutableStateOf(BoostSettings.strict) }
    var blockNotify by remember { mutableStateOf(BoostSettings.blockNotify) }
    var whitelist by remember { mutableStateOf(BoostSettings.whitelistEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("功能增强", fontWeight = FontWeight.Medium) },
        text = {
            Column {
                BoostRow(
                    title = "硬核模式",
                    summary = "计时开始后阻止其他应用打开（需要额外授权，后续版本生效）",
                    checked = hardcore,
                    onCheckedChange = { hardcore = it; BoostSettings.hardcore = it },
                )
                BoostRow(
                    title = "严格模式",
                    summary = "计时中不能暂停、不能提前结束",
                    checked = strict,
                    onCheckedChange = { strict = it; BoostSettings.strict = it },
                )
                BoostRow(
                    title = "拦截通知",
                    summary = "计时期间拦截其他应用的通知（需要通知使用权，后续版本生效）",
                    checked = blockNotify,
                    onCheckedChange = { blockNotify = it; BoostSettings.blockNotify = it },
                )
                BoostRow(
                    title = "白名单",
                    summary = "允许在计时期间使用的应用",
                    checked = whitelist,
                    onCheckedChange = { whitelist = it; BoostSettings.whitelistEnabled = it },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("完成") }
        },
    )
}

@Composable
private fun BoostRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp)
            Spacer(Modifier.height(2.dp))
            Text(summary, fontSize = 12.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
