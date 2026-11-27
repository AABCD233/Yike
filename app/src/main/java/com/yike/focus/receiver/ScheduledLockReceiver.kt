package com.yike.focus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yike.focus.data.FocusTimer
import com.yike.focus.data.ScheduledLock
import com.yike.focus.ui.screen.lock.LockActivity
import java.util.Calendar

class ScheduledLockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!ScheduledLock.enabled) return
        // 已经在专注/锁机里就不重复拉起
        if (FocusTimer.state.value != null) return
        val cal = Calendar.getInstance()
        val now = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        if (!ScheduledLock.inWindow(now)) return
        val left = ScheduledLock.remainingMinutes(now).coerceAtLeast(1)
        FocusTimer.startLock(left)
        runCatching {
            context.startActivity(
                Intent(context, LockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }
}
