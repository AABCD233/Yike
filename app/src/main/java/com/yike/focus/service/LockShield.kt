package com.yike.focus.service

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.yike.focus.data.FocusTimer
import com.yike.focus.data.LockSettings

/**
 * 锁机遮罩：全屏、可触摸消费。
 * 只在「前台不是一刻、也不是白名单」时挂上，配合无障碍的窗口事件做即时遮挡，
 * 用户上滑回桌面时看到的是遮罩而不是桌面。
 */
object LockShield {

    private var view: View? = null

    fun show(context: Context) {
        if (view != null) return
        runCatching {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val root = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setBackgroundColor(Color.parseColor("#15171C"))
                // 吃掉所有触摸，避免误触桌面
                setOnTouchListener { _, _ -> true }
            }
            val title = TextView(context).apply {
                text = "锁机中"
                setTextColor(Color.WHITE)
                textSize = 16f
            }
            val clock = TextView(context).apply {
                setTextColor(Color.WHITE)
                textSize = 48f
                gravity = Gravity.CENTER
                val st = FocusTimer.state.value
                val left = st?.let { FocusTimer.remainingSeconds(it) } ?: 0
                text = "%02d:%02d".format(left / 60, left % 60)
            }
            root.addView(title)
            root.addView(clock)
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.OPAQUE,
            )
            wm.addView(root, params)
            view = root
        }
    }

    fun hide() {
        val v = view ?: return
        runCatching {
            val wm = v.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.removeView(v)
        }
        view = null
    }
}
