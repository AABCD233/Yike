package com.tomato.focus.data

/** 待办类型：普通番茄钟 / 定目标 / 养习惯 */
enum class TodoKind { NORMAL, GOAL, HABIT }

/** 计时方式：倒计时 / 正向计时 / 不计时 */
enum class TimerMode { COUNTDOWN, COUNTUP, NONE }

data class TodoSet(
    val id: Long = 0,
    val name: String,
    val color: Int = 0,
    val sortOrder: Int = 0,
)

data class Todo(
    val id: Long = 0,
    val title: String,
    val kind: TodoKind = TodoKind.NORMAL,
    val timerMode: TimerMode = TimerMode.COUNTDOWN,
    val durationMin: Int = 25,
    val setId: Long? = null,
    /** 定目标：总目标小时数 */
    val targetHours: Float = 0f,
    val startDate: String? = null,
    val endDate: String? = null,
    /** 养习惯：每天目标分钟 */
    val dailyMin: Int = 0,
    val note: String = "",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

data class FocusSession(
    val id: Long = 0,
    val todoId: Long?,
    val title: String,
    val startTs: Long,
    val endTs: Long,
    val seconds: Int,
    /** focus / lock / relax */
    val kind: String = "focus",
    val abandoned: Boolean = false,
)
