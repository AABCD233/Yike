package com.yike.focus.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.yike.focus.receiver.ReminderReceiver
import com.yike.focus.templateApp
import java.time.LocalDate
import java.util.Calendar

/**
 * 每日定时提醒。
 * 双保险：精确闹钟负责准点提醒；另外每 10 分钟做一次自检，
 * 系统把精确闹钟吞掉或手机重启后也能补上一次。
 * 全部走广播，不起常驻服务。
 */
object Reminders {

    private const val CHECK_INTERVAL_MS = 10 * 60 * 1000L

    fun start(context: Context) {
        scheduleCheck(context)
        TodoStore.todos().filter { it.reminderEnabled }.forEach { schedule(context, it) }
    }

    fun schedule(context: Context, todo: Todo) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pending = exactIntent(context, todo)
        if (!todo.reminderEnabled) {
            alarmManager.cancel(pending)
            return
        }
        val trigger = nextTrigger(todo.reminderHour, todo.reminderMinute)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        runCatching {
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
            }
        }
    }

    fun cancel(context: Context, todo: Todo) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.cancel(exactIntent(context, todo))
    }

    fun rescheduleAll(context: Context) = start(context)

    /** 每 10 分钟自检一次，成本只有一次广播 */
    private fun scheduleCheck(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        runCatching {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + CHECK_INTERVAL_MS,
                CHECK_INTERVAL_MS,
                checkIntent(context),
            )
        }
    }

    private fun exactIntent(context: Context, todo: Todo): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_EXACT
            putExtra("todoId", todo.id)
        }
        return PendingIntent.getBroadcast(
            context,
            (todo.id % 100000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun checkIntent(context: Context): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_CHECK
        }
        return PendingIntent.getBroadcast(
            context,
            999_001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** 今天该提醒、还没提醒过的待办 */
    fun dueNow(): List<Todo> {
        val now = Calendar.getInstance()
        val today = LocalDate.now().toString()
        return TodoStore.todos().filter { todo ->
            if (!todo.reminderEnabled) return@filter false
            val due = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE) >=
                todo.reminderHour * 60 + todo.reminderMinute
            due && firedOn(todo.id) != today
        }
    }

    fun markFired(todoId: Long) {
        val prefs = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("fired_$todoId", LocalDate.now().toString()).apply()
    }

    private fun firedOn(todoId: Long): String? =
        templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("fired_$todoId", null)

    private fun nextTrigger(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis) target.add(Calendar.DAY_OF_YEAR, 1)
        return target.timeInMillis
    }

    const val ACTION_EXACT = "com.yike.focus.REMIND_EXACT"
    const val ACTION_CHECK = "com.yike.focus.REMIND_CHECK"
}
