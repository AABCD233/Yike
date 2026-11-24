package com.yike.focus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yike.focus.data.Reminders

/** 开机后把已开启的每日提醒重新排上 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Reminders.rescheduleAll(context)
        }
    }
}
