package com.yike.focus.ui.screen.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.FocusTimer
import com.yike.focus.ui.navigation3.Navigator
import com.yike.focus.ui.navigation3.Route
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun LockScreen(navController: Navigator, bottomPadding: Dp) {
    var tip by remember { mutableStateOf<String?>(null) }
    var pickDuration by remember { mutableStateOf(false) }
    var customMinutes by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("锁机", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 12.dp, bottom = bottomPadding + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                BannerCard(
                    title = "小憩",
                    subtitle = "放松身心的深呼吸，感受禅修般的宁静",
                    container = colorScheme.primaryContainer,
                    titleColor = colorScheme.onPrimaryContainer,
                    subtitleColor = colorScheme.onSurfaceVariantSummary,
                    onClick = { tip = "小憩（呼吸引导）还没有做，下一步接。" },
                )
            }
            item {
                BannerCard(
                    title = "立刻开始快速锁机",
                    subtitle = "选择锁机时长，并立刻开始锁机",
                    container = colorScheme.secondaryContainer,
                    titleColor = colorScheme.onSecondaryContainer,
                    subtitleColor = colorScheme.onSurfaceVariantSummary,
                    onClick = { pickDuration = true },
                )
            }
            item {
                BannerCard(
                    title = "添加自定义锁机时间段",
                    subtitle = "在指定时间范围内锁住手机，不做熬夜手机党",
                    container = colorScheme.tertiaryContainer,
                    titleColor = colorScheme.onTertiaryContainer,
                    subtitleColor = colorScheme.onSurfaceVariantSummary,
                    onClick = { tip = "定时锁机等后台服务接好再做。" },
                )
            }
            item {
                Card(onClick = { pickDuration = true }, showIndication = true) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("还没有设置定时锁机", fontSize = 16.sp, color = colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "锁机是比学霸模式更严格的自控功能",
                            fontSize = 13.sp,
                            color = colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        }
    }

    if (pickDuration) {
        AlertDialog(
            onDismissRequest = { pickDuration = false },
            title = { androidx.compose.material3.Text("锁机时长") },
            text = {
                Column {
                    androidx.compose.material3.Text(
                        "锁机期间屏幕常亮，返回键不生效，到时间自动结束。",
                        fontSize = 13.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(15, 25, 45, 60).forEach { min ->
                            Card(
                                onClick = {
                                    pickDuration = false
                                    FocusTimer.startLock(min)
                                    navController.push(Route.LockActive)
                                },
                                showIndication = true,
                            ) {
                                androidx.compose.material3.Text(
                                    "$min 分钟",
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = customMinutes,
                            onValueChange = { customMinutes = it.filter(Char::isDigit) },
                            singleLine = true,
                            placeholder = { androidx.compose.material3.Text("自定义分钟") },
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        Card(
                            onClick = {
                                val min = customMinutes.toIntOrNull() ?: return@Card
                                if (min in 1..600) {
                                    pickDuration = false
                                    FocusTimer.startLock(min)
                                    navController.push(Route.LockActive)
                                }
                            },
                            showIndication = true,
                        ) {
                            androidx.compose.material3.Text(
                                "开始",
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickDuration = false }) { androidx.compose.material3.Text("取消") }
            },
        )
    }

    tip?.let { text ->
        AlertDialog(
            onDismissRequest = { tip = null },
            confirmButton = {
                TextButton(onClick = { tip = null }) { androidx.compose.material3.Text("知道了") }
            },
            text = { androidx.compose.material3.Text(text) },
        )
    }
}

@Composable
private fun BannerCard(
    title: String,
    subtitle: String,
    container: Color,
    titleColor: Color,
    subtitleColor: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(color = container),
        onClick = onClick,
        showIndication = true,
        pressFeedbackType = PressFeedbackType.Tilt,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = titleColor)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 13.sp, color = subtitleColor)
        }
    }
}
