package com.yike.focus.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.yike.focus.receiver.ReminderReceiver
import java.util.Calendar

/** 每日定时提醒：只用 AlarmManager + 广播，不起服务、不常驻内存。 */
object Reminders {

    fun schedule(context: Context, todo: Todo) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pending = pendingIntent(context, todo)
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
        alarmManager.cancel(pendingIntent(context, todo))
    }

    fun rescheduleAll(context: Context) {
        TodoStore.todos().filter { it.reminderEnabled }.forEach { schedule(context, it) }
    }

    private fun pendingIntent(context: Context, todo: Todo): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("todoId", todo.id)
            putExtra("title", todo.title)
        }
        return PendingIntent.getBroadcast(
            context,
            (todo.id % 100000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun nextTrigger(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }
}
