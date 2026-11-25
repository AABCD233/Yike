package com.yike.focus.data

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import com.yike.focus.templateApp

/** 前台应用检测 + 白名单存取。 */
object AppGuard {

    fun hasUsageAccess(): Boolean {
        val context = templateApp
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasOverlayAccess(): Boolean = Settings.canDrawOverlays(templateApp)

    fun openUsageAccessSettings(context: Context) {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun openOverlaySettings(context: Context) {
        runCatching {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    /** 当前桌面启动器的包名 */
    fun homePackage(): String? {
        val context = templateApp
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return context.packageManager.resolveActivity(homeIntent, 0)?.activityInfo?.packageName
    }

    /** 最近一次切到前台的应用（包名 + 类名），类名用来识别多任务页 */
    fun foregroundApp(): Pair<String, String?>? {
        val context = templateApp
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null
        val now = System.currentTimeMillis()
        val events = manager.queryEvents(now - 15_000, now)
        var lastPackage: String? = null
        var lastClass: String? = null
        var lastTs = 0L
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND && event.timeStamp >= lastTs) {
                lastTs = event.timeStamp
                lastPackage = event.packageName
                lastClass = event.className
            }
        }
        val pkg = lastPackage ?: return null
        return pkg to lastClass
    }

    /** 当前是不是停在多任务（最近任务）界面 */
    fun isRecents(pkg: String, className: String?): Boolean {
        if (pkg != "com.android.systemui") return false
        val cls = className?.lowercase() ?: return false
        return cls.contains("recents") || cls.contains("overview") || cls.contains("launcherui")
    }

    /** 最近一次切到前台的应用包名 */
    fun foregroundPackage(): String? {
        val context = templateApp
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null
        val now = System.currentTimeMillis()
        val events = manager.queryEvents(now - 15_000, now)
        var lastPackage: String? = null
        var lastTs = 0L
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND && event.timeStamp >= lastTs) {
                lastTs = event.timeStamp
                lastPackage = event.packageName
            }
        }
        return lastPackage
    }

    private fun prefs() = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun whitelist(): Set<String> =
        prefs().getStringSet("guard_whitelist", emptySet())?.toSet() ?: emptySet()

    fun setWhitelist(packages: Set<String>) {
        prefs().edit().putStringSet("guard_whitelist", packages).apply()
    }

    /** 我们自己的包、系统界面、桌面启动器都不拦 */
    fun isExempt(pkg: String): Boolean {
        val context = templateApp
        if (pkg == context.packageName) return true
        if (pkg == "com.android.systemui") return true
        val launcher = context.packageManager.getLaunchIntentForPackage(pkg) != null
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val home = context.packageManager.resolveActivity(homeIntent, 0)?.activityInfo?.packageName
        if (home != null && TextUtils.equals(home, pkg)) return true
        return launcher && pkg == home
    }
}
