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
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.yike.focus.R
import com.yike.focus.data.FocusLook
import com.yike.focus.data.FocusTimer
import com.yike.focus.data.LockLimit
import com.yike.focus.data.LockSettings
import com.yike.focus.data.MusicPlaylist
import com.yike.focus.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 锁机界面（悬浮窗实现）：常驻最上层，手势回桌面时也盖着，
 * 按钮直接挂在这层上，所以任何时候都能点。
 */
class LockOverlayService : Service() {

    private var root: LinearLayout? = null
    private var clock: TextView? = null
    private var sub: TextView? = null
    private var musicBtn: TextView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var listed = false

    private val ticker = object : Runnable {
        override fun run() {
            val st = FocusTimer.state.value
            if (st == null || st.kind != "lock") {
                stopSelf()
                return
            }
            val remaining = FocusTimer.remainingSeconds(st)
            clock?.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            sub?.text = "本次锁机 %s - %s · 剩余 %02d:%02d".format(
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(st.startWall)),
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(st.startWall + st.totalSeconds * 1000L)),
                remaining / 60, remaining % 60,
            )
            handler.postDelayed(this, 1000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTI_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!android.provider.Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (root == null) show()
        handler.removeCallbacks(ticker)
        handler.post(ticker)
        return START_STICKY
    }

    private fun show() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(40), dp(24), dp(24))
            // 只在空白处吃掉触摸，按钮仍然可点
            setOnTouchListener { _, _ -> true }
        }
        applyBackground(container)

        val clockView = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 46f
            gravity = Gravity.CENTER
        }
        val subView = TextView(this).apply {
            setTextColor(0xCCFFFFFF.toInt())
            textSize = 13f
            gravity = Gravity.CENTER
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val music = action("音乐") {
            if (MusicPlaylist.tracks.isEmpty()) return@action
            if (MusicPlaylist.isPlaying) MusicPlaylist.toggle()
            else MusicPlaylist.playAt(if (MusicPlaylist.index >= 0) MusicPlaylist.index else 0)
        }
        val bgBtn = action("换背景") {
            val next = (LockSettings.bgPreset + 1) % FocusLook.backgroundColors.size
            LockSettings.bgPreset = next
            applyBackground(container)
        }
        val quit = action("提前结束") {
            if (LockLimit.consume()) {
                FocusTimer.stop(abandoned = true)
                stopSelf()
                runCatching {
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                }
            }
        }
        row.addView(music)
        row.addView(bgBtn)
        row.addView(quit)

        container.addView(clockView)
        container.addView(subView)
        container.addView(row)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.TOP or Gravity.START }

        runCatching {
            wm.addView(container, params)
            root = container
            clock = clockView
            sub = subView
            musicBtn = music
        }
    }

    private fun applyBackground(container: LinearLayout) {
        val idx = LockSettings.bgPreset
        val raw: Long = FocusLook.backgroundColors.getOrElse(idx) { 0xFF15171C }
        val safe = if ((raw ushr 24) == 0L) 0xFF15171CL else raw
        container.setBackgroundColor(safe.toInt())
    }

    private fun action(label: String, onClick: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(10), dp(18), dp(10))
            background = GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                setColor(0x33FFFFFF)
            }
            setOnClickListener { onClick() }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            lp.setMargins(dp(6), 0, dp(6), 0)
            layoutParams = lp
        }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "锁机", NotificationManager.IMPORTANCE_MIN).apply {
                    setShowBadge(false)
                    setSound(null, null)
                }
            )
        }
        val pending = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("锁机进行中")
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pending)
            .build()
    }

    override fun onDestroy() {
        handler.removeCallbacks(ticker)
        root?.let { view ->
            runCatching {
                (getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(view)
            }
        }
        root = null
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL = "lock_overlay"
        private const val NOTI_ID = 2004

        fun start(context: Context) {
            if (!android.provider.Settings.canDrawOverlays(context)) return
            runCatching { context.startForegroundService(Intent(context, LockOverlayService::class.java)) }
        }

        fun stop(context: Context) {
            runCatching { context.stopService(Intent(context, LockOverlayService::class.java)) }
        }
    }
}
