package com.yike.focus.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.yike.focus.data.FocusTimer
import com.yike.focus.ui.screen.lock.LockActivity

/**
 * 锁机用的无障碍服务：
 * 1. 过滤 HOME / BACK 键，锁机期间按了也不生效；
 * 2. 一旦切到别的应用（包括桌面上滑），立刻把锁机页拉回前台。
 */
class LockAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (!isLocking()) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName || pkg.startsWith("com.android.systemui")) return
        bringBackLock()
    }

    private fun bringBackLock() {
        runCatching {
            startActivity(
                Intent(this, LockActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            )
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!isLocking()) return false
        return when (event.keyCode) {
            KeyEvent.KEYCODE_HOME, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_APP_SWITCH -> true
            else -> false
        }
    }

    private fun isLocking(): Boolean = FocusTimer.state.value?.kind == "lock"

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    companion object {
        @Volatile
        private var instance: LockAccessibilityService? = null
    }
}

/** 检查并跳转无障碍设置 */
object LockAccessibility {

    private const val SERVICE_CLASS = "com.yike.focus.service.LockAccessibilityService"

    fun enabled(context: Context = com.yike.focus.templateApp): Boolean {
        val expected = ComponentName(context.packageName, SERVICE_CLASS).flattenToString()
        val current = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        return current.split(':').any { it.equals(expected, true) }
    }

    fun openSettings(context: Context) {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
