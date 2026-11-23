package com.tomato.focus.ui.screen.stats

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tomato.focus.data.TodoStore
import com.tomato.focus.ui.theme.TP
import java.time.LocalDate

@Composable
fun StatsScreen(bottomPadding: Dp) {
    var range by remember { mutableIntStateOf(0) } // 0 日 1 周 2 月
    var dayOffset by remember { mutableIntStateOf(0) }

    val date = LocalDate.now().minusDays(dayOffset.toLong())
    val (from, to) = when (range) {
        0 -> TodoStore.dayStartMillis(date) to TodoStore.dayEndMillis(date)
        1 -> TodoStore.dayStartMillis(date.minusDays(6)) to TodoStore.dayEndMillis(date)
        else -> TodoStore.dayStartMillis(date.withDayOfMonth(1)) to TodoStore.dayEndMillis(date)
    }

    val all = TodoStore.summary(0L, Long.MAX_VALUE / 2)
    val daily = TodoStore.summary(from, to)
    val byTitle = TodoStore.secondsByTitle(from, to)
    val totalSeconds = byTitle.sumOf { it.second }

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
            Text("统计数据", fontSize = 26.sp, fontWeight = FontWeight.Medium, color = TP.TextDark)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Card(title = "累计专注") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Metric("次数", all.count.toString(), "")
                    Metric("时长", (all.seconds / 3600).toString(), "小时")
                    Metric("日均时长", maxOf(0, all.seconds / 3600 / 30).toString(), "小时")
                }
            }

            Card(
                title = "当日专注",
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { dayOffset++ },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(Icons.Rounded.ChevronLeft, contentDescription = "前一天", tint = TP.TextGrey)
                        }
                        Text(date.toString(), fontSize = 12.sp, color = TP.TextGrey)
                        IconButton(
                            onClick = { if (dayOffset > 0) dayOffset-- },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(Icons.Rounded.ChevronRight, contentDescription = "后一天", tint = TP.TextGrey)
                        }
                    }
                },
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Metric("次数", daily.count.toString(), "")
                    Metric("时长", (daily.seconds / 60).toString(), "分钟")
                    Metric("放弃次数", daily.abandoned.toString(), "")
                }
            }

            Card(title = "专注时长分布") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("日", "周", "月").forEachIndexed { index, label ->
                        TabPill(label, range == index) { range = index }
                    }
                }
                Spacer(Modifier.height(14.dp))
                if (totalSeconds == 0) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("这段时间还没有专注记录", fontSize = 13.sp, color = TP.TextGrey)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PieChart(
                            slices = byTitle.map { it.second.toFloat() },
                            modifier = Modifier.size(150.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            byTitle.take(6).forEachIndexed { i, (title, sec) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(TP.chartColor(i)),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        title,
                                        fontSize = 12.sp,
                                        color = TP.TextDark,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                    )
                                    Text(
                                        "${(sec * 100 / totalSeconds)}%",
                                        fontSize = 12.sp,
                                        color = TP.TextGrey,
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
                        color = TP.TextDark,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.height(bottomPadding + 24.dp))
        }
    }
}

@Composable
private fun Card(
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TP.TextDark)
            Spacer(Modifier.weight(1f))
            trailing?.invoke()
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun Metric(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = TP.TextGrey)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = TP.TextDark)
            if (unit.isNotEmpty()) {
                Text(unit, fontSize = 11.sp, color = TP.TextGrey, modifier = Modifier.padding(bottom = 3.dp))
            }
        }
    }
}

@Composable
private fun TabPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) TP.Teal.copy(alpha = 0.15f) else Color(0xFFF2F5F7))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 6.dp),
    ) {
        Text(text, fontSize = 13.sp, color = if (selected) TP.Teal else TP.TextGrey)
    }
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
                color = TP.chartColor(index),
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
