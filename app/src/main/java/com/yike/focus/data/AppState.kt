package com.yike.focus.data

/** 记录 App 是否在前台，用来决定完成后要不要借悬浮窗权限把自己拉起来 */
object AppState {
    @Volatile
    var foreground: Boolean = false
}
