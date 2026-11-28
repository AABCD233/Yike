package com.yike.focus.data

import android.content.Context
import com.yike.focus.templateApp

/** 锁机页的显示与行为设置 */
object LockSettings {

    private fun prefs() = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** 锁机时允许打开白名单里的应用 */
    var allowWhitelist: Boolean
        get() = prefs().getBoolean("lock_allow_white", true)
        set(value) = prefs().edit().putBoolean("lock_allow_white", value).apply()

    /** 背景：FocusLook.backgroundColors 的索引 */
    var bgPreset: Int
        get() = prefs().getInt("lock_bg", 3)
        set(value) = prefs().edit().putInt("lock_bg", value.coerceIn(0, 4)).apply()

    /** 锁机页自定义背景图（绝对路径） */
    var imagePath: String?
        get() = prefs().getString("lock_bg_image", null)
        set(value) = prefs().edit().putString("lock_bg_image", value).apply()

    /** 锁机时播放音乐 */
    var musicOn: Boolean
        get() = prefs().getBoolean("lock_music", false)
        set(value) = prefs().edit().putBoolean("lock_music", value).apply()
}
