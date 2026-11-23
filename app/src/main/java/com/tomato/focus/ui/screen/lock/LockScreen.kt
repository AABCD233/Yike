package com.tomato.focus.ui.screen.lock

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.focus.ui.theme.TP

@Composable
fun LockScreen(bottomPadding: Dp) {
    var tip by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TP.headerBrush())
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("锁机", fontSize = 26.sp, fontWeight = FontWeight.Medium, color = TP.TextDark)
            Spacer(Modifier.weight(1f))
            Pill("必开权限") { tip = "锁机需要「悬浮窗」和「忽略电池优化」两个权限，接入时会引导你开启。" }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BannerCard(
                title = "小憩",
                subtitle = "放松身心的深呼吸，感受禅修般的宁静",
                brush = Brush.horizontalGradient(listOf(Color(0xFF23BFA3), Color(0xFF3FD0B4))),
                onClick = { tip = "小憩（呼吸引导）还没有做，下一步接。" },
            )
            BannerCard(
                title = "立刻开始快速锁机",
                subtitle = "选择锁机时长，并立刻开始锁机",
                brush = Brush.horizontalGradient(listOf(Color(0xFFFF7B6B), Color(0xFFFFA08F))),
                height = 150.dp,
                onClick = { tip = "快速锁机需要后台计时、悬浮窗和拦截页，先把番茄钟跑通再做这一块。" },
            )
            BannerCard(
                title = "添加自定义锁机时间段",
                subtitle = "在指定时间范围内锁住手机，不做熬夜手机党",
                brush = Brush.horizontalGradient(listOf(Color(0xFF4B9CFF), Color(0xFF6FB4FF))),
                onClick = { tip = "定时锁机同样等后台服务接好再做。" },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Pill("提升强制性选项 | 锁机设置") {
                    tip = "强制项包括不可中途退出、拦截非白名单应用。"
                }
            }

            Spacer(Modifier.height(40.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("⏰", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text("当前没有设置定时锁机", fontSize = 14.sp, color = TP.TextDark.copy(alpha = 0.8f))
                Spacer(Modifier.height(6.dp))
                Text("锁机是比学霸模式更严格的自控功能", fontSize = 12.sp, color = TP.Coral)
            }
            Spacer(Modifier.height(bottomPadding + 24.dp))
        }
    }

    tip?.let { text ->
        AlertDialog(
            onDismissRequest = { tip = null },
            confirmButton = {
                TextButton(onClick = { tip = null }) { Text("知道了") }
            },
            text = { Text(text) },
        )
    }
}

@Composable
private fun BannerCard(
    title: String,
    subtitle: String,
    brush: Brush,
    height: Dp = 92.dp,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(brush)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, fontSize = 17.sp, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.92f))
    }
}

@Composable
private fun Pill(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.85f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, fontSize = 12.sp, color = TP.TextDark.copy(alpha = 0.8f))
    }
}
