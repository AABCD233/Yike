package com.yike.focus.ui.util

import android.content.Context
import com.yike.focus.BuildConfig

/** 版本号，日志和关于页用 */
fun getAppVersion(context: Context): String {
    val info = context.packageManager.getPackageInfo(context.packageName, 0)
    val name = info.versionName ?: BuildConfig.VERSION_NAME
    val code = info.longVersionCode
    return "$name ($code)"
}
