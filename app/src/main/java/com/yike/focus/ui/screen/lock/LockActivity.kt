package com.yike.focus.ui.screen.lock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.yike.focus.data.FocusTimer
import com.yike.focus.ui.theme.TemplateTheme

/** 锁机专用页面：无障碍服务会把用户从桌面拉回这里 */
class LockActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FocusTimer.state.value?.kind != "lock") {
            finish()
            return
        }
        setContent {
            TemplateTheme {
                LockActiveScreen(onExit = { finish() })
            }
        }
    }
}
