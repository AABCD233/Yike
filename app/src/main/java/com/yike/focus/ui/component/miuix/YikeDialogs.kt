package com.yike.focus.ui.component.miuix

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.overlay.OverlayDialog

/** 统一的确认框：Miuix 的 OverlayDialog，别再用 Material AlertDialog */
@Composable
fun ConfirmDialog(
    show: Boolean,
    title: String,
    summary: String? = null,
    confirmText: String = "确定",
    dismissText: String? = "取消",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    OverlayDialog(
        show = show,
        title = title,
        summary = summary,
        onDismissRequest = onDismiss,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (dismissText != null) {
                TextButton(
                    text = dismissText,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(20.dp))
            }
            TextButton(
                text = confirmText,
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}

/** 单行输入框对话框 */
@Composable
fun InputDialog(
    show: Boolean,
    title: String,
    initial: String,
    confirmText: String = "保存",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember(show) { mutableStateOf(initial) }
    OverlayDialog(
        show = show,
        title = title,
        onDismissRequest = onDismiss,
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                text = "取消",
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(20.dp))
            TextButton(
                text = confirmText,
                onClick = { if (text.isNotBlank()) onConfirm(text.trim()) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}

/** 只有一句话和「知道了」的提示框 */
@Composable
fun TipDialog(show: Boolean, text: String, onDismiss: () -> Unit) {
    OverlayDialog(
        show = show,
        onDismissRequest = onDismiss,
    ) {
        androidx.compose.material3.Text(text, modifier = Modifier.padding(bottom = 16.dp))
        TextButton(
            text = "知道了",
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColorsPrimary(),
        )
    }
}
