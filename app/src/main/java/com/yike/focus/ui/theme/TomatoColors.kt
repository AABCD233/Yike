package com.yike.focus.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** 番茄计划的配色，取自截图里那套清爽的蓝绿 + 珊瑚色。 */
object TP {
    val HeaderTop = Color(0xFF87DCEA)
    val HeaderMid = Color(0xFFB7EAF2)
    val HeaderBottom = Color(0xFFE8F7FA)

    val Teal = Color(0xFF23BFA3)
    val Coral = Color(0xFFFF7B6B)
    val Blue = Color(0xFF4B9CFF)
    val Pink = Color(0xFFF0697F)
    val Amber = Color(0xFFF3B45A)
    val Mint = Color(0xFF7FD3D8)
    val Lavender = Color(0xFFB79CFF)

    val TextDark = Color(0xFF23282D)
    val TextGrey = Color(0xFF8C939B)
    val Divider = Color(0xFFEDF1F4)

    fun headerBrush() = Brush.verticalGradient(listOf(HeaderTop, HeaderMid, HeaderBottom))

    /** 待办卡片背景：没有照片素材，用渐变代替 */
    fun cardBrush(seed: Int): Brush {
        val palettes = listOf(
            listOf(Color(0xFF6FA8C9), Color(0xFF9FC4D9)),
            listOf(Color(0xFFB9A6B8), Color(0xFFD8C4CE)),
            listOf(Color(0xFF83C6C0), Color(0xFFA9DFD8)),
            listOf(Color(0xFF9F8FC4), Color(0xFFC6B6E0)),
            listOf(Color(0xFFD79A9A), Color(0xFFE8BCB6)),
            listOf(Color(0xFF7FA9A0), Color(0xFFA9CCC2)),
        )
        val idx = ((seed % palettes.size) + palettes.size) % palettes.size
        val c = palettes[idx]
        return Brush.linearGradient(listOf(c[0], c[1]))
    }

    fun chartColor(index: Int): Color {
        val cs = listOf(Pink, Mint, Amber, Blue, Lavender, Teal, Coral)
        return cs[((index % cs.size) + cs.size) % cs.size]
    }
}

// 深色模式下的页面与卡片底色：只改这两处，渐变头部保持原来的浅色
@Composable
fun tpPageBg(): Color = if (isInDarkTheme()) Color(0xFF141719) else Color(0xFFF7F9FA)

@Composable
fun tpCardBg(): Color = if (isInDarkTheme()) Color(0xFF1E2226) else Color.White

@Composable
fun tpTextSub(): Color = if (isInDarkTheme()) Color(0xFF9AA3AC) else TP.TextGrey
