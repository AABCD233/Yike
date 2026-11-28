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
import com.yike.focus.data.AppGuard
import com.yike.focus.data.BoostSettings
import com.yike.focus.data.FocusTimer
import com.yike.focus.ui.MainActivity
import com.yike.focus.ui.screen.guard.GuardActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 硬核模式：专注进行中每 1.2 秒看一次前台应用，
 * 不在白名单里就弹拦截页。没有使用情况权限时自己停掉。
 */
class GuardService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!AppGuard.hasUsageAccess()) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (job == null) {
            job = scope.launch {
                while (isActive) {
                    delay(1200)
                    val running = FocusTimer.state.value
                    if (running == null || running.isPaused) continue
                    if (running.kind != "focus") continue
                    if (!BoostSettings.hardcore && !BoostSettings.blockDesktop) continue
                    val foreground = AppGuard.foregroundApp() ?: continue
                    val pkg = foreground.first
                    if (pkg == packageName) {
                        hideShield()
                        continue
                    }

                    // 开了「禁止桌面和多任务」：不管它是桌面、最近任务还是别的，
                    // 只要不是我们、也不在白名单里，就拉回来
                    if (BoostSettings.blockDesktop) {
                        if (AppGuard.whitelist().contains(pkg)) continue
                        launchGuard(desktop = true)
                        continue
                    }
                    if (!BoostSettings.hardcore) continue
                    if (AppGuard.isExempt(pkg)) continue
                    if (AppGuard.whitelist().contains(pkg)) continue
                    launchGuard(desktop = false)
                }
            }
        }
        return START_STICKY
    }

    private var shield: android.view.View? = null

    private fun showShield() {
        if (shield != null) return
        runCatching {
            val wm = getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            val view = android.widget.TextView(this).apply {
                text = "锁机中\n正在返回…"
                setTextColor(android.graphics.Color.WHITE)
                textSize = 18f
                gravity = android.view.Gravity.CENTER
                setBackgroundColor(android.graphics.Color.BLACK)
            }
            val params = android.view.WindowManager.LayoutParams(
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.graphics.PixelFormat.OPAQUE,
            )
            wm.addView(view, params)
            shield = view
        }
    }

    private fun hideShield() {
        val view = shield ?: return
        runCatching {
            val wm = getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            wm.removeView(view)
        }
        shield = null
    }

    private fun launchLockBack() {
        runCatching {
            startActivity(
                Intent(this, com.yike.focus.ui.screen.lock.LockActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                }
            )
        }
    }

    private fun launchGuard(desktop: Boolean) {
        runCatching {
            startActivity(
                Intent(this, GuardActivity::class.java).putExtra("desktop", desktop).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            )
        }
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "专注守护", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "硬核模式在后台检查前台应用"
                    setShowBadge(false)
                }
            )
        }
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("专注守护已开启")
            .setContentText("计时期间会拦住白名单以外的应用")
            .setOngoing(true)
            .setContentIntent(pending)
            .build()
    }

    override fun onDestroy() {
        hideShield()
        job?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "focus_guard"
        private const val NOTIFICATION_ID = 2001

        fun start(context: Context) {
            if (!AppGuard.hasUsageAccess()) return
            runCatching {
                context.startForegroundService(Intent(context, GuardService::class.java))
            }
        }

        fun stop(context: Context) {
            runCatching { context.stopService(Intent(context, GuardService::class.java)) }
        }
    }
}
