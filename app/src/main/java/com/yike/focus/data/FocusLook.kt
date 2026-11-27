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

    /** 自选背景音乐路径 */
    var musicPath: String?
        get() = prefs().getString("focus_music", null)
        set(value) = prefs().edit().putString("focus_music", value).apply()

    /** 自选背景图（filesDir 下的路径），有图时优先用图 */
    var backgroundImage: String?
        get() = prefs().getString("focus_bg_image", null)
        set(value) = prefs().edit().putString("focus_bg_image", value).apply()

    // ---- 按待办分别存：todoId=0 表示全局默认 ----
    fun presetOf(todoId: Long): Int =
        prefs().getInt("focus_bg_$todoId", backgroundPreset)

    fun setPresetOf(todoId: Long, value: Int) {
        prefs().edit().putInt("focus_bg_$todoId", value.coerceIn(0, 4)).apply()
    }

    fun imageOf(todoId: Long): String? =
        prefs().getString("focus_bg_image_$todoId", if (todoId == 0L) backgroundImage else null)

    fun setImageOf(todoId: Long, path: String?) {
        prefs().edit().putString("focus_bg_image_$todoId", path).apply()
    }

    val backgroundNames = listOf("素白", "淡青", "暖砂", "墨黑", "跟随主题")
    val backgroundColors = listOf(
        0xFFF6F7F9,
        0xFFDCE9EF,
        0xFFF3E5D8,
        0xFF15171C,
        0x00000000,
    )
}
