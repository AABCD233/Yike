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
        if (FocusTimer.state.value != null) return
        val cal = Calendar.getInstance()
        val now = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        // Calendar 周日=1，统一成 周一=1 … 周日=7
        val jsDay = cal.get(Calendar.DAY_OF_WEEK)
        val day = if (jsDay == Calendar.SUNDAY) 7 else jsDay - 1
        val period = ScheduledLock.activePeriod(now, day) ?: return
        val left = ScheduledLock.remainingMinutes(period, now).coerceAtLeast(1)
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
