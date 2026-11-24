package com.yike.focus.data

import android.content.Context
import com.yike.focus.templateApp

/** 番茄循环：学习 → 短休息 → 学习 … 每 4 个一轮长休息 */
object PomodoroSettings {

    private fun prefs() = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var breakMinutes: Int
        get() = prefs().getInt("pomo_break", 5)
        set(value) = prefs().edit().putInt("pomo_break", value.coerceIn(1, 60)).apply()

    var longBreakMinutes: Int
        get() = prefs().getInt("pomo_long_break", 15)
        set(value) = prefs().edit().putInt("pomo_long_break", value.coerceIn(1, 120)).apply()

    var roundsBeforeLong: Int
        get() = prefs().getInt("pomo_rounds", 4)
        set(value) = prefs().edit().putInt("pomo_rounds", value.coerceIn(1, 12)).apply()

    var autoBreak: Boolean
        get() = prefs().getBoolean("pomo_auto_break", true)
        set(value) = prefs().edit().putBoolean("pomo_auto_break", value).apply()

    var autoNext: Boolean
        get() = prefs().getBoolean("pomo_auto_next", false)
        set(value) = prefs().edit().putBoolean("pomo_auto_next", value).apply()

    /** 本次会话里已经完成几个番茄（进程重启后清零就行） */
    var roundIndex: Int
        get() = prefs().getInt("pomo_round", 0)
        set(value) = prefs().edit().putInt("pomo_round", value).apply()

    /** 休息结束后要回到哪个任务 */
    var lastTodoId: Long
        get() = prefs().getLong("pomo_last_todo", 0L)
        set(value) = prefs().edit().putLong("pomo_last_todo", value).apply()

    val nextIsLongBreak: Boolean
        get() = roundIndex > 0 && roundIndex % roundsBeforeLong == 0
}
