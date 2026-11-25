package com.yike.focus.data

import android.content.Context
import com.yike.focus.templateApp

/** 功能增强：几项"更狠"的开关，全部本地保存。 */
object BoostSettings {

    private fun prefs() = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var hardcore: Boolean
        get() = prefs().getBoolean("boost_hardcore", false)
        set(value) = prefs().edit().putBoolean("boost_hardcore", value).apply()

    var strict: Boolean
        get() = prefs().getBoolean("boost_strict", false)
        set(value) = prefs().edit().putBoolean("boost_strict", value).apply()

    var blockNotify: Boolean
        get() = prefs().getBoolean("boost_block_notify", false)
        set(value) = prefs().edit().putBoolean("boost_block_notify", value).apply()

    /** 禁止桌面和多任务：专注时切到桌面就弹拦截页 */
    var blockDesktop: Boolean
        get() = prefs().getBoolean("boost_block_desktop", false)
        set(value) = prefs().edit().putBoolean("boost_block_desktop", value).apply()

    /** 在最近任务里隐藏本应用 */
    var hideRecents: Boolean
        get() = prefs().getBoolean("boost_hide_recents", false)
        set(value) = prefs().edit().putBoolean("boost_hide_recents", value).apply()

    var dnd: Boolean
        get() = prefs().getBoolean("boost_dnd", false)
        set(value) = prefs().edit().putBoolean("boost_dnd", value).apply()

    var whitelistEnabled: Boolean
        get() = prefs().getBoolean("boost_whitelist", false)
        set(value) = prefs().edit().putBoolean("boost_whitelist", value).apply()
}
