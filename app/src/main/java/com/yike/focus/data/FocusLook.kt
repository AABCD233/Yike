package com.yike.focus.data

import android.content.Context
import com.yike.focus.templateApp

/** 专注页的背景、音乐选择，都是本地偏好。 */
object FocusLook {

    private fun prefs() = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** 0..5 对应 TP.focusGradients 里的预设 */
    var backgroundPreset: Int
        get() = prefs().getInt("focus_bg", 0)
        set(value) = prefs().edit().putInt("focus_bg", value.coerceIn(0, 5)).apply()

    /** none / white / pink / brown */
    var noiseKind: String
        get() = prefs().getString("focus_noise", "none") ?: "none"
        set(value) = prefs().edit().putString("focus_noise", value).apply()

    val backgroundNames = listOf("晨雾", "暮色", "林间", "海边", "夜灯", "素白")
}
