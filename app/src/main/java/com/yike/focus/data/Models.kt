package com.yike.focus.data

/** 待办类型：普通番茄钟 / 定目标 / 养习惯 */
enum class TodoKind { NORMAL, GOAL, HABIT }

/** 计时方式：倒计时 / 正向计时 / 不计时 */
enum class TimerMode { COUNTDOWN, COUNTUP, NONE }

data class TodoSet(
    val id: Long = 0,
    val name: String,
    val color: Int = 0,
    val sortOrder: Int = 0,
    /** 完成单个待办后的休息分钟 */
    val restMin: Int = 5,
    /** 全部完成后的长休息分钟 */
    val longRestMin: Int = 15,
    /** 连续执行子待办（仅倒计时） */
    val autoNext: Boolean = false,
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
    /** 卡片背景图，存在 filesDir 下的绝对路径 */
    val bgPath: String? = null,
    /** 这个待办自己的循环参数 */
    val breakMin: Int = 5,
    val longBreakMin: Int = 15,
    val rounds: Int = 4,
    /** 每日定时提醒 */
    val reminderEnabled: Boolean = false,
    /** 完成多少轮算完成，0 = 不限 */
    val totalRounds: Int = 0,
    /** 养习惯频率：0 = 每天，1..7 = 每周几次 */
    val habitPerWeek: Int = 0,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
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
