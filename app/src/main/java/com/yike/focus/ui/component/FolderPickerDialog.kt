package com.yike.focus.ui.component

import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import java.io.File

/** 只选文件夹：不用系统文件管理器，自己从存储根目录点进去 */
@Composable
fun FolderPickerDialog(
    title: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var dir by remember { mutableStateOf(File(Environment.getExternalStorageDirectory().absolutePath)) }
    val dirs = remember(dir) {
        (dir.listFiles() ?: emptyArray())
            .filter { it.isDirectory && !it.name.startsWith(".") }
            .sortedBy { it.name.lowercase() }
    }

    OverlayDialog(
        show = true,
        title = title,
        summary = dir.absolutePath,
        onDismissRequest = onDismiss,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (dir.parentFile != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { dir = dir.parentFile }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.ArrowUpward, contentDescription = "上一层", tint = colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("上一层", fontSize = 14.sp, color = colorScheme.primary)
                }
            }
            Column(modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                dirs.forEach { d ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { dir = d }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Folder, contentDescription = null, tint = colorScheme.onSurfaceVariantSummary)
                        Spacer(Modifier.width(10.dp))
                        Text(d.name, fontSize = 14.sp, color = colorScheme.onSurface)
                    }
                }
                if (dirs.isEmpty()) {
                    Text(
                        "没有子文件夹（可能是没拿到存储权限）",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariantSummary,
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
            TextButton(
                text = "用这个文件夹",
                onClick = { onPick(dir.absolutePath) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}
