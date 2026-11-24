package com.yike.focus.ui.screen.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yike.focus.data.TodoStore
import com.yike.focus.ui.navigation3.Navigator
import com.yike.focus.ui.navigation3.Route
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import java.time.LocalDate

@Composable
fun StatsScreen(navController: Navigator, bottomPadding: Dp) {
    var range by remember { mutableIntStateOf(0) }
    var dayOffset by remember { mutableIntStateOf(0) }

    val date = LocalDate.now().minusDays(dayOffset.toLong())
    val (from, to) = when (range) {
        0 -> TodoStore.dayStartMillis(date) to TodoStore.dayEndMillis(date)
        1 -> TodoStore.dayStartMillis(date.minusDays(6)) to TodoStore.dayEndMillis(date)
        else -> TodoStore.dayStartMillis(date.withDayOfMonth(1)) to TodoStore.dayEndMillis(date)
    }

    val all = TodoStore.summary(0L, Long.MAX_VALUE / 2)
    val daily = TodoStore.summary(from, to)
    val lockSummary = remember(range, dayOffset) { TodoStore.summary(from, to, kind = "lock") }
    val byTitle = TodoStore.secondsByTitle(from, to)
    val totalSeconds = byTitle.sumOf { it.second }
    val hourly = remember(range, dayOffset) { TodoStore.hourlyMinutes(from, to) }
    val last7 = remember { TodoStore.dailySeconds(7) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomPadding + 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("统计数据", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
            }
        }

        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("累计专注", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Metric("次数", all.count.toString(), "")
                        Metric("时长", (all.seconds / 3600).toString(), "小时")
                        Metric("日均", maxOf(0, all.seconds / 3600 / 30).toString(), "小时")
                    }
                }
            }
        }

        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("当日专注", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { dayOffset++ }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.ChevronLeft, contentDescription = "前一天", tint = colorScheme.onSurfaceVariantSummary)
                        }
                        Text(date.toString(), fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
                        IconButton(onClick = { if (dayOffset > 0) dayOffset-- }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.ChevronRight, contentDescription = "后一天", tint = colorScheme.onSurfaceVariantSummary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Metric("次数", daily.count.toString(), "")
                        Metric("时长", (daily.seconds / 60).toString(), "分钟")
                        Metric("放弃", daily.abandoned.toString(), "次")
                    }
                }
            }
        }

        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("专注时长分布", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("日", "周", "月").forEachIndexed { index, label ->
                            TabPill(label, range == index) { range = index }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    if (totalSeconds == 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                            Text("这段时间还没有专注记录", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            PieChart(
                                slices = byTitle.map { it.second.toFloat() },
                                modifier = Modifier.size(140.dp),
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                byTitle.take(6).forEachIndexed { i, (title, sec) ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(8.dp).clip(CircleShape)
                                                .background(chartColor(i)),
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            title,
                                            fontSize = 12.sp,
                                            color = colorScheme.onSurface,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                        )
                                        Text(
                                            "${(sec * 100 / totalSeconds)}%",
                                            fontSize = 12.sp,
                                            color = colorScheme.onSurfaceVariantSummary,
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "总计 ${totalSeconds / 3600} 小时 ${totalSeconds % 3600 / 60} 分钟",
                            fontSize = 13.sp,
                            color = colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("专注时段分布", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                    Spacer(Modifier.height(10.dp))
                    val maxVal = hourly.maxOrNull()?.coerceAtLeast(1) ?: 1
                    Row(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        hourly.forEach { minutes ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height((72 * minutes / maxVal).coerceAtLeast(if (minutes > 0) 3 else 0).dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
                                    .background(if (minutes > 0) colorScheme.primary else Color.Transparent),
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("0", "6", "12", "18", "23").forEach {
                            Text(it, fontSize = 10.sp, color = colorScheme.onSurfaceVariantSummary)
                        }
                    }
                }
            }
        }

        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("最近七天", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                    Spacer(Modifier.height(10.dp))
                    val maxVal = last7.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
                    Row(
                        modifier = Modifier.fillMaxWidth().height(96.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        last7.forEach { (day, sec) ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                            ) {
                                Text("${sec / 60}", fontSize = 10.sp, color = colorScheme.onSurfaceVariantSummary)
                                Spacer(Modifier.height(3.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((60 * sec / maxVal).coerceAtLeast(if (sec > 0) 3 else 0).dp)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                        .background(colorScheme.primaryContainer),
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("${day.dayOfMonth}", fontSize = 10.sp, color = colorScheme.onSurfaceVariantSummary)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("锁机", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Metric("次数", lockSummary.count.toString(), "")
                        Metric("时长", (lockSummary.seconds / 60).toString(), "分钟")
                        Metric("提前结束", lockSummary.abandoned.toString(), "次")
                    }
                }
            }
        }

        item {
            Card(onClick = { navController.push(Route.History) }, showIndication = true) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("查看专注记录", fontSize = 14.sp, color = colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurface)
            if (unit.isNotEmpty()) {
                Text(unit, fontSize = 11.sp, color = colorScheme.onSurfaceVariantSummary, modifier = Modifier.padding(bottom = 3.dp))
            }
        }
    }
}

@Composable
private fun TabPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        showIndication = true,
    ) {
        Text(
            text,
            fontSize = 13.sp,
            color = if (selected) colorScheme.primary else colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

private fun chartColor(index: Int): Color {
    val cs = listOf(
        Color(0xFFF0697F), Color(0xFF7FD3D8), Color(0xFFF3B45A), Color(0xFF4B9CFF),
        Color(0xFFB79CFF), Color(0xFF23BFA3), Color(0xFFFF7B6B),
    )
    return cs[((index % cs.size) + cs.size) % cs.size]
}

@Composable
private fun PieChart(slices: List<Float>, modifier: Modifier = Modifier) {
    val total = slices.sum().coerceAtLeast(1f)
    Canvas(modifier = modifier) {
        var start = -90f
        val pad = 4.dp.toPx()
        val sizeArc = Size(size.width - pad * 2, size.height - pad * 2)
        slices.forEachIndexed { index, value ->
            val sweep = 360f * (value / total)
            drawArc(
                color = chartColor(index),
                startAngle = start,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(pad, pad),
                size = sizeArc,
            )
            start += sweep
        }
    }
}
