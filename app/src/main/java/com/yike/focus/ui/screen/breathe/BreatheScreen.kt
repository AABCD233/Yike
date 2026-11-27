package com.yike.focus.ui.screen.breathe

import android.graphics.BitmapFactory
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.FocusLook
import com.yike.focus.ui.navigation3.Navigator
import top.yukonga.miuix.kmp.basic.Text
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

private const val ROUND_SECONDS = 180

private val GUIDES = listOf(
    "放松肩膀", "放松手臂", "放松背部", "放松腿部", "放松面部", "放松全身",
)

@Composable
fun BreatheScreen(navController: Navigator) {
    var running by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }
    var phase by remember { mutableIntStateOf(-1) }
    var left by remember { mutableIntStateOf(ROUND_SECONDS) }
    var finished by remember { mutableStateOf(false) }
    val breath = remember { Animatable(0f) }

    val bgImage = remember {
        FocusLook.backgroundImage?.let { path ->
            runCatching {
                BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = 2 })
                    ?.asImageBitmap()
            }.getOrNull()
        }
    }
    val presetColor = remember {
        val idx = FocusLook.backgroundPreset
        if (idx in FocusLook.backgroundColors.indices) Color(FocusLook.backgroundColors[idx]) else null
    }
    val background = if (bgImage != null) Color.Black
    else presetColor?.takeIf { it.alpha > 0f } ?: Color(0xFF123B63)

    val spin = rememberInfiniteTransition(label = "petal")
    val slowSpin by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(240_000, easing = LinearEasing), RepeatMode.Restart),
        label = "spin",
    )

    // 呼吸循环 + 计时
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        while (running && !finished) {
            phase = 0
            breath.animateTo(1f, tween(4000))
            if (!running) break
            phase = 1
            delay(4000)
            if (!running) break
            phase = 2
            breath.animateTo(0f, tween(6000))
        }
    }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        while (running && left > 0) {
            delay(1000)
            left--
        }
        running = false
        finished = true
        phase = -1
    }

    val guide = GUIDES[((ROUND_SECONDS - left) / 30).coerceIn(0, GUIDES.lastIndex)]

    Box(modifier = Modifier.fillMaxSize().background(background)) {
        bgImage?.let {
            androidx.compose.foundation.Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { navController.pop() }) {
                    androidx.compose.material3.Icon(
                        Icons.Rounded.Close,
                        contentDescription = "关闭",
                        tint = Color.White.copy(alpha = 0.85f),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                when {
                    finished -> "这一轮结束了"
                    !started -> "深度冥想，放松身心"
                    phase == 0 -> "吸气"
                    phase == 1 -> "屏住"
                    phase == 2 -> "呼气"
                    else -> "跟着花瓣深呼吸"
                },
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (finished) "深呼吸几次，再回去继续"
                else if (started) "$guide · 剩余 ${left / 60}:${"%02d".format(left % 60)}"
                else "4 秒吸气 · 4 秒屏住 · 6 秒呼气",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
            )

            Spacer(Modifier.weight(1f))
            Canvas(modifier = Modifier.size(320.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val petalW = size.minDimension * 0.15f
                val petalH = size.minDimension * 0.30f
                val minR = size.minDimension * 0.12f
                val maxR = size.minDimension * 0.28f
                val r = minR + (maxR - minR) * breath.value
                val open = 0.55f + 0.45f * breath.value

                repeat(12) { i ->
                    val baseAngle = 30f * i
                    // 花瓣自身随呼吸轻轻摆开
                    val angle = baseAngle + slowSpin * 0.15f
                    val rad = Math.toRadians((angle - 90f).toDouble())
                    val cx = center.x + (r * cos(rad)).toFloat()
                    val cy = center.y + (r * sin(rad)).toFloat()

                    rotate(degrees = angle, pivot = Offset(cx, cy)) {
                        drawOval(
                            color = Color.White.copy(alpha = 0.22f * open),
                            topLeft = Offset(cx - petalW / 2, cy - petalH),
                            size = Size(petalW, petalH),
                        )
                        drawOval(
                            color = Color.White.copy(alpha = 0.14f * open),
                            topLeft = Offset(cx - petalW * 0.34f, cy - petalH * 0.66f),
                            size = Size(petalW * 0.68f, petalH * 0.66f),
                        )
                    }
                }
                drawCircle(
                    color = Color.White.copy(alpha = 0.28f),
                    radius = petalW * 0.55f,
                    center = center,
                )
            }
            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable {
                        if (finished) {
                            navController.pop()
                        } else {
                            started = true
                            running = !running
                            if (!running) phase = -1
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    when {
                        finished -> "结束"
                        running -> "暂停"
                        started -> "继续"
                        else -> "开始"
                    },
                    color = Color.White,
                    fontSize = 16.sp,
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "小憩 3 分钟，随时可以暂停",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
