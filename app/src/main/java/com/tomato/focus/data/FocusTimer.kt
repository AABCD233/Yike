package com.tomato.focus.data

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 全局唯一的专注计时器。
 * 用 SystemClock.elapsedRealtime 计算已用时间，切后台、锁屏回来都不会漂。
 */
object FocusTimer {

    data class State(
        val todoId: Long?,
        val title: String,
        val mode: TimerMode,
        val totalSeconds: Int,
        val startedWall: Long,
        val startedElapsed: Long,
        val pausedAtElapsed: Long? = null,
        val pausedAccumMs: Long = 0L,
    ) {
        val isCountdown: Boolean get() = mode == TimerMode.COUNTDOWN
        val isPaused: Boolean get() = pausedAtElapsed != null
    }

    private val _state = MutableStateFlow<State?>(null)
    val state: StateFlow<State?> = _state.asStateFlow()

    fun start(todo: Todo, overrideMinutes: Int? = null) {
        val minutes = overrideMinutes ?: todo.durationMin
        _state.value = State(
            todoId = todo.id,
            title = todo.title,
            mode = todo.timerMode,
            totalSeconds = if (todo.timerMode == TimerMode.COUNTDOWN) minutes * 60 else 0,
            startedWall = System.currentTimeMillis(),
            startedElapsed = SystemClock.elapsedRealtime(),
        )
    }

    fun startFree(title: String, minutes: Int) {
        _state.value = State(
            todoId = null,
            title = title,
            mode = TimerMode.COUNTDOWN,
            totalSeconds = minutes * 60,
            startedWall = System.currentTimeMillis(),
            startedElapsed = SystemClock.elapsedRealtime(),
        )
    }

    fun pause() {
        val s = _state.value ?: return
        if (s.isPaused) return
        _state.value = s.copy(pausedAtElapsed = SystemClock.elapsedRealtime())
    }

    fun resume() {
        val s = _state.value ?: return
        val pausedAt = s.pausedAtElapsed ?: return
        _state.value = s.copy(
            pausedAtElapsed = null,
            pausedAccumMs = s.pausedAccumMs + (SystemClock.elapsedRealtime() - pausedAt),
        )
    }

    /** 已用秒数 */
    fun elapsedSeconds(s: State? = _state.value): Int {
        s ?: return 0
        val now = s.pausedAtElapsed ?: SystemClock.elapsedRealtime()
        val used = (now - s.startedElapsed - s.pausedAccumMs).coerceAtLeast(0L)
        return (used / 1000L).toInt()
    }

    /** 剩余秒数；正向计时返回 0 */
    fun remainingSeconds(s: State? = _state.value): Int {
        s ?: return 0
        if (!s.isCountdown) return 0
        return (s.totalSeconds - elapsedSeconds(s)).coerceAtLeast(0)
    }

    /** 结束并写库，返回写进去的记录 */
    fun stop(abandoned: Boolean): FocusSession? {
        val s = _state.value ?: return null
        val used = elapsedSeconds(s)
        _state.value = null
        if (used < 60 && !abandoned) return null
        val end = System.currentTimeMillis()
        val session = FocusSession(
            todoId = s.todoId,
            title = s.title,
            startTs = s.startedWall,
            endTs = end,
            seconds = used,
            kind = "focus",
            abandoned = abandoned,
        )
        TodoStore.insertSession(session)
        return session
    }

    fun cancel() {
        _state.value = null
    }
}
