package com.yike.focus.ui.viewmodel

import androidx.compose.runtime.Immutable
import com.yike.focus.ui.UiMode
import com.yike.focus.ui.theme.AppSettings

@Immutable
data class MainActivityUiState(
    val appSettings: AppSettings,
    val pageScale: Float,
    val enableBlur: Boolean,
    val enableFloatingBottomBar: Boolean,
    val enableFloatingBottomBarBlur: Boolean,
    val enableSwipeDismiss: Boolean,
    val pagerInterceptionMode: Int,
    val uiMode: UiMode,
)
