package com.yike.focus.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.yike.focus.R
import com.yike.focus.data.AppGuard
import com.yike.focus.data.BoostSettings
import com.yike.focus.data.FocusTimer
import com.yike.focus.data.Reminders
import com.yike.focus.ui.MainActivity

/** 收到闹钟：挑出今天该提醒的待办，发通知，然后排下一次 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 专注中就别再推每日提醒了，免得打扰
        val running = FocusTimer.state.value
        val busy = running != null && !running.isPaused &&
            (running.kind == "focus" || running.kind == "lock")

        if (!busy) {
            Reminders.dueNow().forEach { todo ->
                notify(context, todo.id, todo.title)
                Reminders.markFired(todo.id)
            }
        }
        Reminders.rescheduleAll(context)
    }

    private fun notify(context: Context, todoId: Long, title: String) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "每日提醒", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "待办的每日定时提醒"
                }
            )
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("今天的待办")
            .setContentText(title)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        runCatching {
            NotificationManagerCompat.from(context).notify(todoId.toInt().let { if (it <= 0) 3001 else it }, notification)
        }
    }

    private companion object {
        const val CHANNEL_ID = "todo_reminder"
    }
}
