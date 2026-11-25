package com.yike.focus.data

import android.app.NotificationManager
import android.content.Context
import com.yike.focus.templateApp

/** 勿扰模式：专注开始时切到「仅闹钟」，结束恢复原来的状态 */
object Dnd {

    private fun manager(context: Context = templateApp) =
        context.getSystemService(NotificationManager::class.java)

    fun granted(context: Context = templateApp): Boolean =
        manager(context)?.isNotificationPolicyAccessGranted == true

    fun enable(): Boolean {
        val m = manager() ?: return false
        if (!m.isNotificationPolicyAccessGranted) return false
        val prefs = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (prefs.getInt("dnd_previous", -1) == -1) {
            prefs.edit().putInt("dnd_previous", m.currentInterruptionFilter).apply()
        }
        runCatching { m.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS) }
        return true
    }

    fun disable() {
        val m = manager() ?: return
        val prefs = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val previous = prefs.getInt("dnd_previous", -1)
        if (previous != -1 && m.isNotificationPolicyAccessGranted) {
            runCatching { m.setInterruptionFilter(previous) }
        }
        prefs.edit().remove("dnd_previous").apply()
    }
}
