package com.yike.focus.data

import android.content.Context
import com.yike.focus.templateApp

/** 熄屏时钟的外观与防烧屏 */
object AodSettings {

    private fun prefs() = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** 0 = 细, 1 = 常规, 2 = 中等, 3 = 粗 */
    var fontWeight: Int
        get() = prefs().getInt("aod_weight", 0)
        set(value) = prefs().edit().putInt("aod_weight", value.coerceIn(0, 3)).apply()

    val weightNames = listOf("细", "常规", "中等", "粗")

    var antiBurn: Boolean
        get() = prefs().getBoolean("aod_antiburn", true)
        set(value) = prefs().edit().putBoolean("aod_antiburn", value).apply()

    /** 位移间隔，分钟 */
    var shiftMinutes: Int
        get() = prefs().getInt("aod_shift", 1)
        set(value) = prefs().edit().putInt("aod_shift", value.coerceIn(1, 30)).apply()

    val shiftChoices = listOf(1, 5, 10, 15)
}
