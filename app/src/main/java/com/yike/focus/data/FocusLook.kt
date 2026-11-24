package com.yike.focus.data

import android.content.Context
import com.yike.focus.templateApp

/** 专注页底色，只放纯色，不做渐变。 */
object FocusLook {

    private fun prefs() = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** 0 素白 1 淡青 2 暖砂 3 墨黑 4 跟随主题 */
    var backgroundPreset: Int
        get() = prefs().getInt("focus_bg", 4)
        set(value) = prefs().edit().putInt("focus_bg", value.coerceIn(0, 4)).apply()

    val backgroundNames = listOf("素白", "淡青", "暖砂", "墨黑", "跟随主题")
    val backgroundColors = listOf(
        0xFFF6F7F9,
        0xFFDCE9EF,
        0xFFF3E5D8,
        0xFF15171C,
        0x00000000,
    )
}
