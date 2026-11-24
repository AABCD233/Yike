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
import com.yike.focus.data.Reminders
import com.yike.focus.data.TodoStore
import com.yike.focus.ui.MainActivity

/** 收到闹钟就发一条通知，然后把明天同一时间的闹钟排上 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getLongExtra("todoId", 0L)
        val title = intent.getStringExtra("title") ?: "该开始今天的专注了"
        notify(context, todoId, title)
        TodoStore.todo(todoId)?.let { Reminders.schedule(context, it) }
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
