package com.yike.focus.data

import android.content.Context
import com.yike.focus.templateApp
import java.util.Calendar

/** 锁机主动退出的每月额度（跟番茄ToDo 一样限次，避免一锁就没退路） */
object LockLimit {

    private fun prefs() = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private fun monthKey(): String {
        val c = Calendar.getInstance()
        return "%d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1)
    }

    private fun used(): Int {
        if (prefs().getString("lock_quit_month", "") != monthKey()) return 0
        return prefs().getInt("lock_quit_used", 0)
    }

    fun remaining(): Int = (3 - used()).coerceAtLeast(0)

    /** 用掉一次主动退出，成功返回 true */
    fun consume(): Boolean {
        if (remaining() <= 0) return false
        prefs().edit()
            .putString("lock_quit_month", monthKey())
            .putInt("lock_quit_used", used() + 1)
            .apply()
        return true
    }
}
