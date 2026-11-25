package com.yike.focus.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 刚结束的一次专注/休息：用来在计时页弹完成框，前台后台都能接上 */
object FinishEvent {

    data class Finished(val title: String, val seconds: Int, val isRest: Boolean)

    var pending by mutableStateOf<Finished?>(null)

    fun emit(finished: Finished) {
        pending = finished
    }

    fun consume(): Finished? {
        val value = pending
        pending = null
        return value
    }
}
