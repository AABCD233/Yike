package com.yike.focus.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.yike.focus.R
import com.yike.focus.data.FocusTimer
import com.yike.focus.ui.MainActivity

/**
 * 悬浮球：专注期间浮在别的应用上，显示剩余时间；
 * 可拖动，点一下回到计时页。
 */
class FloatingBubbleService : Service() {

    private var windowManager: WindowManager? = null
    private var bubble: TextView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var params: WindowManager.LayoutParams? = null

    private val ticker = object : Runnable {
        override fun run() {
            updateText()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (bubble == null) showBubble()
        handler.removeCallbacks(ticker)
        handler.post(ticker)
        return START_STICKY
    }

    private fun showBubble() {
        val wm = windowManager ?: return
        val view = TextView(this).apply {
            text = timerText()
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFF1B1F24.toInt())
                setStroke(2, 0x33FFFFFF)
            }
        }
        val layoutParams = WindowManager.LayoutParams(
            dp(64), dp(64),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(16)
            y = dp(240)
        }
        params = layoutParams

        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = layoutParams.x
                    startY = layoutParams.y
                    touchX = event.rawX
                    touchY = event.rawY
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val bubbleSize = dp(64)
                    val metrics = resources.displayMetrics
                    val maxX = (metrics.widthPixels - bubbleSize).coerceAtLeast(0)
                    val maxY = (metrics.heightPixels - bubbleSize).coerceAtLeast(0)
                    layoutParams.x = (startX + (event.rawX - touchX).toInt()).coerceIn(0, maxX)
                    layoutParams.y = (startY + (event.rawY - touchY).toInt()).coerceIn(0, maxY)
                    runCatching { wm.updateViewLayout(view, layoutParams) }
                    true
                }
                else -> false
            }
        }
        view.setOnClickListener { backToTimer() }

        runCatching {
            wm.addView(view, layoutParams)
            bubble = view
        }
    }

    private fun updateText() {
        val state = FocusTimer.state.value
        if (state == null) {
            stopSelf()
            return
        }
        val view = bubble ?: return
        view.text = timerText()
        // 在自己 App 里不需要悬浮球，切出去才显示
        val visible = !com.yike.focus.data.AppState.foreground
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun timerText(): String {
        val state = FocusTimer.state.value ?: return "一刻"
        val remaining = FocusTimer.remainingSeconds(state)
        val m = remaining / 60
        val s = remaining % 60
        return "%02d:%02d".format(m, s)
    }

    private fun backToTimer() {
        runCatching {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                    putExtra(MainActivity.EXTRA_OPEN_FOCUS, true)
                }
            )
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "悬浮计时", NotificationManager.IMPORTANCE_MIN).apply {
                    setShowBadge(false)
                    setSound(null, null)
                    enableVibration(false)
                }
            )
        }
        val pending = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("一刻正在计时")
            .setContentText("点悬浮球可以回到计时页")
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pending)
            .build()
    }

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        bubble?.let { view ->
            runCatching { windowManager?.removeView(view) }
        }
        bubble = null
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "focus_bubble"
        private const val NOTIFICATION_ID = 2003

        fun start(context: Context) {
            if (!android.provider.Settings.canDrawOverlays(context)) return
            runCatching {
                context.startForegroundService(Intent(context, FloatingBubbleService::class.java))
            }
        }

        fun stop(context: Context) {
            runCatching { context.stopService(Intent(context, FloatingBubbleService::class.java)) }
        }
    }
}
