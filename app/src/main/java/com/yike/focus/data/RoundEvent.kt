package com.yike.focus.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 待办轮数完成时，回到主界面弹的那条提示 */
object RoundEvent {
    var celebrateTitle by mutableStateOf<String?>(null)
    /** 需要主界面切到第几个 tab，处理完清空 */
    var requestPage by mutableStateOf<Int?>(null)
}
