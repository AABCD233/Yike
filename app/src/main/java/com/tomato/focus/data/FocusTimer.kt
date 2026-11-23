package com.tomato.focus.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * 全局唯一的专注计时器。
 * 用墙钟时间算已用时长，并把状态写进 SharedPreferences，
 * 这样切后台、进程被杀再回来都还接得上。
 */
object FocusTimer {

    private const val PREFS = "focus_timer"
    private const val KEY = "running"

    data class State(
        val todoId: Long?,
        val title: String,
        val mode: TimerMode,
        val totalSeconds: Int,
        val startWall: Long,
        val pausedAtWall: Long? = null,
        val pausedAccumMs: Long = 0L,
        val kind: String = "focus",
    ) {
        val isCountdown: Boolean get() = mode == TimerMode.COUNTDOWN
        val isPaused: Boolean get() = pausedAtWall != null
    }

    private val _state = MutableStateFlow<State?>(null)
    val state: StateFlow<State?> = _state.asStateFlow()

    private var prefs: android.content.SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs = p
        val raw = p.getString(KEY, null) ?: return
        runCatching {
            val o = JSONObject(raw)
            val s = State(
                todoId = if (o.isNull("todoId")) null else o.getLong("todoId"),
                title = o.getString("title"),
                mode = TimerMode.valueOf(o.getString("mode")),
                totalSeconds = o.optInt("total", 0),
                startWall = o.getLong("startWall"),
                pausedAtWall = if (o.isNull("pausedAt")) null else o.getLong("pausedAt"),
                pausedAccumMs = o.optLong("pausedAccum", 0L),
                kind = o.optString("kind", "focus"),
            )
            // 超过 12 小时的残留状态当作已结束，避免第二天打开还是昨天那个番茄
            if (System.currentTimeMillis() - s.startWall > 12 * 3600_000L) {
                p.edit().remove(KEY).apply()
            } else {
                _state.value = s
            }
        }.onFailure { p.edit().remove(KEY).apply() }
    }

    private fun persist() {
        val p = prefs ?: return
        val s = _state.value
        if (s == null) {
            p.edit().remove(KEY).apply()
            return
        }
        val o = JSONObject().apply {
            put("todoId", s.todoId ?: JSONObject.NULL)
            put("title", s.title)
            put("mode", s.mode.name)
            put("total", s.totalSeconds)
            put("startWall", s.startWall)
            put("pausedAt", s.pausedAtWall ?: JSONObject.NULL)
            put("pausedAccum", s.pausedAccumMs)
            put("kind", s.kind)
        }
        p.edit().putString(KEY, o.toString()).apply()
    }

    fun start(todo: Todo, overrideMinutes: Int? = null) {
        val minutes = overrideMinutes ?: todo.durationMin
        _state.value = State(
            todoId = todo.id,
            title = todo.title,
            mode = todo.timerMode,
            totalSeconds = if (todo.timerMode == TimerMode.COUNTDOWN) minutes * 60 else 0,
            startWall = System.currentTimeMillis(),
        )
        persist()
    }

    fun startRest(minutes: Int) {
        _state.value = State(
            todoId = null,
            title = "休息",
            mode = TimerMode.COUNTDOWN,
            totalSeconds = minutes * 60,
            startWall = System.currentTimeMillis(),
            kind = "relax",
        )
        persist()
    }

    fun startFree(title: String, minutes: Int) {
        _state.value = State(
            todoId = null,
            title = title,
            mode = TimerMode.COUNTDOWN,
            totalSeconds = minutes * 60,
            startWall = System.currentTimeMillis(),
        )
        persist()
    }

    fun pause() {
        val s = _state.value ?: return
        if (s.isPaused) return
        _state.value = s.copy(pausedAtWall = System.currentTimeMillis())
        persist()
    }

    fun resume() {
        val s = _state.value ?: return
        val pausedAt = s.pausedAtWall ?: return
        _state.value = s.copy(
            pausedAtWall = null,
            pausedAccumMs = s.pausedAccumMs + (System.currentTimeMillis() - pausedAt),
        )
        persist()
    }

    private fun elapsedMs(s: State): Long {
        val now = s.pausedAtWall ?: System.currentTimeMillis()
        return (now - s.startWall - s.pausedAccumMs).coerceAtLeast(0L)
    }

    fun elapsedSeconds(s: State? = _state.value): Int {
        s ?: return 0
        return (elapsedMs(s) / 1000L).toInt()
    }

    fun remainingSeconds(s: State? = _state.value): Int {
        s ?: return 0
        if (!s.isCountdown) return 0
        return (s.totalSeconds - elapsedSeconds(s)).coerceAtLeast(0)
    }

    /** 结束并写库 */
    fun stop(abandoned: Boolean): FocusSession? {
        val s = _state.value ?: return null
        val used = elapsedSeconds(s)
        _state.value = null
        persist()
        if (used < 60 && !abandoned) return null
        val session = FocusSession(
            todoId = s.todoId,
            title = s.title,
            startTs = s.startWall,
            endTs = System.currentTimeMillis(),
            seconds = used,
            kind = s.kind,
            abandoned = abandoned,
        )
        TodoStore.insertSession(session)
        return session
    }

    fun cancel() {
        _state.value = null
        persist()
    }
}
