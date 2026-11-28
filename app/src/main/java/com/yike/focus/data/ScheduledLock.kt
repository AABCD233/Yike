package com.yike.focus.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.yike.focus.receiver.ScheduledLockReceiver
import com.yike.focus.templateApp
import org.json.JSONArray
import org.json.JSONObject

/** 一个定时锁机时段（支持多条，比如 22:30-23:30、01:00-06:00） */
data class LockPeriod(
    val id: Long = System.currentTimeMillis(),
    val startHour: Int = 22,
    val startMinute: Int = 30,
    val endHour: Int = 6,
    val endMinute: Int = 0,
    /** 1=周一 … 7=周日 */
    val days: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val enabled: Boolean = true,
) {
    fun startLabel() = "%02d:%02d".format(startHour, startMinute)
    fun endLabel() = "%02d:%02d".format(endHour, endMinute)
    fun daysLabel(): String = when (days) {
        setOf(1, 2, 3, 4, 5) -> "工作日"
        setOf(6, 7) -> "周末"
        setOf(1, 2, 3, 4, 5, 6, 7) -> "每天"
        else -> days.sorted().joinToString(" ") { listOf("一", "二", "三", "四", "五", "六", "日")[it - 1] }
    }
}

object ScheduledLock {

    private fun prefs() = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun periods(): List<LockPeriod> {
        val raw = prefs().getString("lock_periods", null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val daysArr = o.getJSONArray("days")
                LockPeriod(
                    id = o.getLong("id"),
                    startHour = o.getInt("sh"),
                    startMinute = o.getInt("sm"),
                    endHour = o.getInt("eh"),
                    endMinute = o.getInt("em"),
                    days = (0 until daysArr.length()).map { daysArr.getInt(it) }.toSet(),
                    enabled = o.getBoolean("on"),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun save(list: List<LockPeriod>) {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(JSONObject().apply {
                put("id", p.id)
                put("sh", p.startHour)
                put("sm", p.startMinute)
                put("eh", p.endHour)
                put("em", p.endMinute)
                put("days", JSONArray(p.days.sorted()))
                put("on", p.enabled)
            })
        }
        prefs().edit().putString("lock_periods", arr.toString()).apply()
    }

    fun upsert(period: LockPeriod) {
        val list = periods().toMutableList()
        val index = list.indexOfFirst { it.id == period.id }
        if (index >= 0) list[index] = period else list += period
        save(list)
    }

    fun remove(id: Long) {
        save(periods().filterNot { it.id == id })
    }

    /** 当前时间命中的时段；没有则 null */
    fun activePeriod(nowMinutes: Int, dayOfWeek: Int): LockPeriod? =
        periods().firstOrNull { p ->
            if (!p.enabled || dayOfWeek !in p.days) return@firstOrNull false
            val start = p.startHour * 60 + p.startMinute
            val end = p.endHour * 60 + p.endMinute
            if (start <= end) nowMinutes in start until end else (nowMinutes >= start || nowMinutes < end)
        }

    fun remainingMinutes(period: LockPeriod, nowMinutes: Int): Int {
        val end = period.endHour * 60 + period.endMinute
        return if (end >= nowMinutes) end - nowMinutes else (24 * 60 - nowMinutes) + end
    }

    /** 每 5 分钟自检一次 */
    fun schedule(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ScheduledLockReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, 91, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        if (periods().none { it.enabled }) {
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
