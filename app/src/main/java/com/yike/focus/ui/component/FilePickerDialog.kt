package com.yike.focus.ui.component

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import java.io.File

/**
 * 简易文件浏览器：设备上被砍掉 DocumentsUI 时也能选到音乐文件。
 * 从内部存储根目录开始逐层进去，只列文件夹和音频文件。
 */
@Composable
fun FilePickerDialog(
    title: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var dir by remember {
        mutableStateOf(
            File(context.getExternalFilesDir(null)?.parentFile, "").let {
                File(Environment.getExternalStorageDirectory().absolutePath)
            }
        )
    }
    val entries = remember(dir) {
        (dir.listFiles() ?: emptyArray())
            .filter { it.isDirectory || it.name.lowercase().let { n -> AUDIO_ANY.any { n.endsWith(it) } } }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
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
            Box(modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                Column {
                    entries.forEach { file ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (file.isDirectory) dir = file else onPick(file.absolutePath)
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                if (file.isDirectory) Icons.Rounded.Folder else Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariantSummary,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(file.name, fontSize = 14.sp, color = colorScheme.onSurface)
                        }
                    }
                    if (entries.isEmpty()) {
                        Text(
                            "这个文件夹里没有音频文件",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            TextButton(
                text = "关闭",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}

private val AUDIO_ANY = listOf(".mp3", ".m4a", ".aac", ".ogg", ".wav", ".flac")
