package com.yike.focus.ui.screen.music

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
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
import com.yike.focus.data.MusicPlaylist
import com.yike.focus.ui.component.FolderPickerDialog
import com.yike.focus.ui.navigation3.Navigator
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

@Composable
fun MusicScreen(navController: Navigator) {
    var showPicker by remember { mutableStateOf(false) }
    val current = MusicPlaylist.current()

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 6.dp, end = 8.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navController.pop() }) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "返回", tint = colorScheme.onBackground)
            }
            Text("背景音乐", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Rounded.FolderOpen, contentDescription = "选择目录", tint = colorScheme.onBackground)
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    current?.title ?: "还没有选目录",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurface,
                )
                Text(
                    if (MusicPlaylist.tracks.isEmpty()) "选一个放歌的文件夹，里面的音频会排成播放列表"
                    else "第 ${MusicPlaylist.index + 1} / ${MusicPlaylist.tracks.size} 首 · ${MusicPlaylist.mode.label}",
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ControlIcon(Icons.Rounded.SkipPrevious, "上一首") { MusicPlaylist.previous() }
                    Spacer(Modifier.size(18.dp))
                    ControlIcon(
                        if (MusicPlaylist.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        "播放/暂停",
                        large = true,
                    ) { MusicPlaylist.toggle() }
                    Spacer(Modifier.size(18.dp))
                    ControlIcon(Icons.Rounded.SkipNext, "下一首") { MusicPlaylist.next() }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    MusicPlaylist.Mode.entries.forEach { m ->
                        val selected = MusicPlaylist.mode == m
                        Card(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            onClick = { MusicPlaylist.setMode(m) },
                            showIndication = true,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    if (m == MusicPlaylist.Mode.SHUFFLE) Icons.Rounded.Shuffle else Icons.Rounded.Repeat,
                                    contentDescription = null,
                                    tint = if (selected) colorScheme.primary else colorScheme.onSurfaceVariantSummary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.size(4.dp))
                                Text(
                                    m.label,
                                    fontSize = 11.sp,
                                    color = if (selected) colorScheme.primary else colorScheme.onSurfaceVariantSummary,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (MusicPlaylist.tracks.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "选目录时会扫描该目录和它的一层子目录。\n读不到文件的话，先去「我的 → 权限」把「所有文件访问」打开。",
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                )
                Spacer(Modifier.height(12.dp))
                Card(onClick = { showPicker = true }, showIndication = true) {
                    Text(
                        "选择音乐目录",
                        fontSize = 14.sp,
                        color = colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                itemsIndexed(MusicPlaylist.tracks) { i, track ->
                    val selected = i == MusicPlaylist.index
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { MusicPlaylist.playAt(i) },
                        showIndication = true,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                track.title,
                                fontSize = 14.sp,
                                color = if (selected) colorScheme.primary else colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                            )
                            if (selected) {
                                Text("正在播放", fontSize = 11.sp, color = colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        FolderPickerDialog(
            title = "选择音乐目录",
            onPick = { path ->
                MusicPlaylist.setDirectory(path)
                MusicPlaylist.saveDirectory(path)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun ControlIcon(icon: ImageVector, description: String, large: Boolean = false, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(if (large) 64.dp else 46.dp)) {
        Icon(
            icon,
            contentDescription = description,
            tint = colorScheme.onSurface,
            modifier = Modifier.size(if (large) 40.dp else 28.dp),
        )
    }
}
