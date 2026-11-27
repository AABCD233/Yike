package com.yike.focus.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.yike.focus.receiver.ScheduledLockReceiver
import com.yike.focus.templateApp

/** 定时锁机：到点自动进入锁机页 */
object ScheduledLock {

    private fun prefs() = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs().getBoolean("sched_lock_on", false)
        set(value) = prefs().edit().putBoolean("sched_lock_on", value).apply()

    var startHour: Int
        get() = prefs().getInt("sched_lock_sh", 22)
        set(value) = prefs().edit().putInt("sched_lock_sh", value).apply()

    var startMinute: Int
        get() = prefs().getInt("sched_lock_sm", 30)
        set(value) = prefs().edit().putInt("sched_lock_sm", value).apply()

    var endHour: Int
        get() = prefs().getInt("sched_lock_eh", 6)
        set(value) = prefs().edit().putInt("sched_lock_eh", value).apply()

    var endMinute: Int
        get() = prefs().getInt("sched_lock_em", 0)
        set(value) = prefs().edit().putInt("sched_lock_em", value).apply()

    fun label(hour: Int, minute: Int) = "%02d:%02d".format(hour, minute)

    /** 现在是否该锁：支持跨零点 */
    fun inWindow(nowMinutes: Int): Boolean {
        if (!enabled) return false
        val start = startHour * 60 + startMinute
        val end = endHour * 60 + endMinute
        return if (start <= end) nowMinutes in start until end else (nowMinutes >= start || nowMinutes < end)
    }

    fun remainingMinutes(nowMinutes: Int): Int {
        val end = endHour * 60 + endMinute
        return if (end >= nowMinutes) end - nowMinutes else (24 * 60 - nowMinutes) + end
    }

    /** 每 5 分钟自检一次，省电也够准时 */
    fun schedule(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ScheduledLockReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, 91, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        if (!enabled) {
            alarm.cancel(pending)
            return
        }
        runCatching {
            alarm.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 60_000,
                5 * 60_000L,
                pending,
            )
        }
    }
}
