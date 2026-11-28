package com.yike.focus.ui.screen.locksettings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.FocusLook
import com.yike.focus.data.LockSettings
import top.yukonga.miuix.kmp.basic.Card
import androidx.compose.material3.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

@Composable
fun LockSettingsScreen() {
    var allowWhite by remember { mutableStateOf(LockSettings.allowWhitelist) }
    var bg by remember { mutableIntStateOf(LockSettings.bgPreset) }
    var music by remember { mutableStateOf(LockSettings.musicOn) }
    var allowQuit by remember { mutableStateOf(LockSettings.allowQuit) }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("锁机设置", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
        }

        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("锁机时允许打开白名单应用", fontSize = 15.sp, color = colorScheme.onSurface)
                        Text(
                            "关掉后锁机页不显示白名单入口，所有应用都会被拉回",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    Switch(checked = allowWhite, onCheckedChange = {
                        allowWhite = it
                        LockSettings.allowWhitelist = it
                    })
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("显示「提前结束」按钮", fontSize = 15.sp, color = colorScheme.onSurface)
                        Text(
                            "开启后锁机页右上方会出现提前结束，每月最多用 3 次；关闭则完全无退路",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    Switch(checked = allowQuit, onCheckedChange = {
                        allowQuit = it
                        LockSettings.allowQuit = it
                    })
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("锁机背景", fontSize = 15.sp, color = colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                val ctx = androidx.compose.ui.platform.LocalContext.current
                val picker = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                ) { uri ->
                    uri ?: return@rememberLauncherForActivityResult
                    runCatching {
                        val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@runCatching
                        val file = java.io.File(ctx.filesDir, "lock_bg_${System.currentTimeMillis()}.jpg")
                        file.writeBytes(bytes)
                        LockSettings.imagePath = file.absolutePath
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FocusLook.backgroundColors.forEachIndexed { index, value ->
                        val shown = if ((value ushr 24) == 0L) 0xFF3A3A42 else value
                        Box(
                            modifier = Modifier
                                .size(if (index == bg) 38.dp else 32.dp)
                                .clip(CircleShape)
                                .background(Color(shown))
                                .clickable {
                                    bg = index
                                    LockSettings.bgPreset = index
                                },
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                top.yukonga.miuix.kmp.basic.TextButton(
                    text = "从相册选图",
                    onClick = { picker.launch("image/*") },
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("锁机时播放音乐", fontSize = 15.sp, color = colorScheme.onSurface)
                        Text(
                            "使用「音乐」页里选的歌单；锁机时会短暂抢走音频焦点，暂停其他应用的音乐",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    Switch(checked = music, onCheckedChange = {
                        music = it
                        LockSettings.musicOn = it
                    })
                }
            }
        }
    }
}
