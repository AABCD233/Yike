package com.yike.focus.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.yike.focus.R
import com.yike.focus.data.FocusTimer
import com.yike.focus.data.Reminders
import com.yike.focus.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 常驻提醒服务：前台服务 + 一条静音常驻通知，每分钟检查一次待办提醒。
 * 比纯闹钟准时，代价是通知栏里常驻一条（可折叠、不响）。
 */
class ReminderService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (job == null) {
            job = scope.launch {
                while (isActive) {
                    delay(60_000)
                    tick()
                }
            }
        }
        tick()
        return START_STICKY
    }

    private fun tick() {
        val running = FocusTimer.state.value
        val busy = running != null && !running.isPaused &&
            (running.kind == "focus" || running.kind == "lock")
        if (busy) return
        Reminders.dueNow().forEach { todo ->
            Reminders.notifyNow(this, todo.id, todo.title)
            Reminders.markFired(todo.id)
        }
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "提醒服务", NotificationManager.IMPORTANCE_MIN).apply {
                    description = "常驻运行，用来准时提醒待办；不会响铃"
                    setShowBadge(false)
                    setSound(null, null)
                    enableVibration(false)
                }
            )
        }
        val pending = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("一刻在后台待命")
            .setContentText("有定时提醒的待办会准时提醒你")
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pending)
            .build()
    }

    override fun onDestroy() {
        job?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "reminder_service"
        private const val NOTIFICATION_ID = 2002

        fun start(context: Context) {
            runCatching {
                context.startForegroundService(Intent(context, ReminderService::class.java))
            }
        }

        fun stop(context: Context) {
            runCatching { context.stopService(Intent(context, ReminderService::class.java)) }
        }
    }
}
