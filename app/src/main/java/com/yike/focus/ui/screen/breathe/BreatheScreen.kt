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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.FocusLook
import com.yike.focus.ui.navigation3.Navigator
import top.yukonga.miuix.kmp.basic.Text
import kotlinx.coroutines.delay

/**
 * 小憩：跟着花瓣深呼吸。
 * 节奏 4 秒吸 / 4 秒停 / 6 秒呼，花瓣随呼吸放大缩小，整体缓慢旋转。
 */
@Composable
fun BreatheScreen(navController: Navigator) {
    var running by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }
    var phase by remember { mutableIntStateOf(-1) }
    val scale = remember { Animatable(0.9f) }

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

    val rotation = rememberInfiniteTransition(label = "petal")
    val spin by rotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(120_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spin",
    )

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        while (true) {
            phase = 0
            scale.animateTo(1.32f, tween(4000))
            phase = 1
            delay(4000)
            phase = 2
            scale.animateTo(0.88f, tween(6000))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
    ) {
        bgImage?.let {
            androidx.compose.foundation.Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            Box(
                modifier = Modifier.matchParentSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
            )
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

            Spacer(Modifier.height(12.dp))
            Text(
                when {
                    !started -> "深度冥想，放松身心"
                    phase == 0 -> "吸气"
                    phase == 1 -> "屏住"
                    phase == 2 -> "呼气"
                    else -> "跟着花瓣深呼吸"
                },
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.weight(1f))
            Canvas(modifier = Modifier.size(300.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val petalWidth = size.minDimension * 0.20f
                val petalHeight = size.minDimension * 0.44f
                rotate(spin) {
                    repeat(12) { i ->
                        rotate(30f * i, pivot = center) {
                            drawOval(
                                color = Color.White.copy(alpha = 0.16f),
                                topLeft = Offset(center.x - petalWidth / 2, center.y - petalHeight),
                                size = Size(petalWidth, petalHeight),
                            )
                            drawOval(
                                color = Color.White.copy(alpha = 0.10f),
                                topLeft = Offset(
                                    center.x - petalWidth * 0.35f,
                                    center.y - petalHeight * 0.55f,
                                ),
                                size = Size(petalWidth * 0.7f, petalHeight * 0.55f),
                            )
                        }
                    }
                }
                // 中心的呼吸核
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = petalWidth * 0.32f * scale.value,
                    center = center,
                )
            }
            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .clickable {
                        started = true
                        running = !running
                        if (!running) phase = -1
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (running) "暂停" else if (started) "继续" else "开始",
                    color = Color.White,
                    fontSize = 16.sp,
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "4 秒吸气 · 4 秒屏住 · 6 秒呼气",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
