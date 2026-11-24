package com.yike.focus.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val DB_NAME = "yike.db"
private const val DB_VERSION = 4
private val DAY_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

class TodoDb(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE todo_set(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                color INTEGER NOT NULL DEFAULT 0,
                sort_order INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE todo(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                kind TEXT NOT NULL DEFAULT 'NORMAL',
                timer_mode TEXT NOT NULL DEFAULT 'COUNTDOWN',
                duration_min INTEGER NOT NULL DEFAULT 25,
                set_id INTEGER,
                target_hours REAL NOT NULL DEFAULT 0,
                start_date TEXT,
                end_date TEXT,
                daily_min INTEGER NOT NULL DEFAULT 0,
                note TEXT NOT NULL DEFAULT '',
                sort_order INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL DEFAULT 0,
                bg_path TEXT,
                break_min INTEGER NOT NULL DEFAULT 5,
                long_break_min INTEGER NOT NULL DEFAULT 15,
                rounds INTEGER NOT NULL DEFAULT 4,
                reminder_enabled INTEGER NOT NULL DEFAULT 0,
                reminder_hour INTEGER NOT NULL DEFAULT 8,
                reminder_minute INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE session(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                todo_id INTEGER,
                title TEXT NOT NULL DEFAULT '',
                start_ts INTEGER NOT NULL,
                end_ts INTEGER NOT NULL,
                seconds INTEGER NOT NULL,
                kind TEXT NOT NULL DEFAULT 'focus',
                abandoned INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_session_start ON session(start_ts)")
        db.execSQL("CREATE INDEX idx_todo_set ON todo(set_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 只做加列，不重建表：重建会把用户已有的待办和记录全清掉
        if (oldVersion < 2) {
            runCatching { db.execSQL("ALTER TABLE todo ADD COLUMN bg_path TEXT") }
        }
        if (oldVersion < 3) {
            runCatching { db.execSQL("ALTER TABLE todo ADD COLUMN break_min INTEGER NOT NULL DEFAULT 5") }
            runCatching { db.execSQL("ALTER TABLE todo ADD COLUMN long_break_min INTEGER NOT NULL DEFAULT 15") }
            runCatching { db.execSQL("ALTER TABLE todo ADD COLUMN rounds INTEGER NOT NULL DEFAULT 4") }
        }
        if (oldVersion < 4) {
            runCatching { db.execSQL("ALTER TABLE todo ADD COLUMN reminder_enabled INTEGER NOT NULL DEFAULT 0") }
            runCatching { db.execSQL("ALTER TABLE todo ADD COLUMN reminder_hour INTEGER NOT NULL DEFAULT 8") }
            runCatching { db.execSQL("ALTER TABLE todo ADD COLUMN reminder_minute INTEGER NOT NULL DEFAULT 0") }
        }
    }
}

/** 所有读写都走这里，UI 层不直接碰 SQL。 */
object TodoStore {

    @Volatile
    private var helper: TodoDb? = null

    fun init(context: Context) {
        if (helper == null) {
            synchronized(this) {
                if (helper == null) helper = TodoDb(context.applicationContext)
            }
        }
    }

    private val db: SQLiteDatabase
        get() = requireNotNull(helper) { "TodoStore.init() 未调用" }.writableDatabase

    fun dayKey(date: LocalDate = LocalDate.now()): String = date.format(DAY_FMT)

    fun dayStartMillis(date: LocalDate = LocalDate.now()): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun dayEndMillis(date: LocalDate = LocalDate.now()): Long =
        date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    // ---------- 待办 ----------

    fun todos(setId: Long? = null): List<Todo> {
        val where = if (setId == null) null else "set_id = ?"
        val args = if (setId == null) null else arrayOf(setId.toString())
        val out = mutableListOf<Todo>()
        db.query("todo", null, where, args, null, null, "sort_order ASC, id ASC").use { c ->
            while (c.moveToNext()) {
                out += Todo(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    title = c.getString(c.getColumnIndexOrThrow("title")),
                    kind = runCatching {
                        TodoKind.valueOf(c.getString(c.getColumnIndexOrThrow("kind")))
                    }.getOrDefault(TodoKind.NORMAL),
                    timerMode = runCatching {
                        TimerMode.valueOf(c.getString(c.getColumnIndexOrThrow("timer_mode")))
                    }.getOrDefault(TimerMode.COUNTDOWN),
                    durationMin = c.getInt(c.getColumnIndexOrThrow("duration_min")),
                    setId = c.isNull(c.getColumnIndexOrThrow("set_id"))
                        .let { if (it) null else c.getLong(c.getColumnIndexOrThrow("set_id")) },
                    targetHours = c.getFloat(c.getColumnIndexOrThrow("target_hours")),
                    startDate = c.getString(c.getColumnIndexOrThrow("start_date")),
                    endDate = c.getString(c.getColumnIndexOrThrow("end_date")),
                    dailyMin = c.getInt(c.getColumnIndexOrThrow("daily_min")),
                    note = c.getString(c.getColumnIndexOrThrow("note")),
                    sortOrder = c.getInt(c.getColumnIndexOrThrow("sort_order")),
                    createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
                    bgPath = if (c.isNull(c.getColumnIndexOrThrow("bg_path"))) null
                    else c.getString(c.getColumnIndexOrThrow("bg_path")),
                    breakMin = c.getInt(c.getColumnIndexOrThrow("break_min")),
                    longBreakMin = c.getInt(c.getColumnIndexOrThrow("long_break_min")),
                    rounds = c.getInt(c.getColumnIndexOrThrow("rounds")),
                    reminderEnabled = c.getInt(c.getColumnIndexOrThrow("reminder_enabled")) != 0,
                    reminderHour = c.getInt(c.getColumnIndexOrThrow("reminder_hour")),
                    reminderMinute = c.getInt(c.getColumnIndexOrThrow("reminder_minute")),
                )
            }
        }
        return out
    }

    fun todo(id: Long): Todo? = todos().firstOrNull { it.id == id }

    fun insertTodo(todo: Todo): Long {
        val values = ContentValues().apply {
            put("title", todo.title)
            put("kind", todo.kind.name)
            put("timer_mode", todo.timerMode.name)
            put("duration_min", todo.durationMin)
            if (todo.setId != null) put("set_id", todo.setId) else putNull("set_id")
            put("target_hours", todo.targetHours)
            put("start_date", todo.startDate)
            put("end_date", todo.endDate)
            put("daily_min", todo.dailyMin)
            put("note", todo.note)
            put("sort_order", todo.sortOrder)
            put("created_at", todo.createdAt)
            if (todo.bgPath != null) put("bg_path", todo.bgPath) else putNull("bg_path")
            put("break_min", todo.breakMin)
            put("long_break_min", todo.longBreakMin)
            put("rounds", todo.rounds)
            put("reminder_enabled", if (todo.reminderEnabled) 1 else 0)
            put("reminder_hour", todo.reminderHour)
            put("reminder_minute", todo.reminderMinute)
        }
        return db.insert("todo", null, values)
    }

    fun updateTodo(todo: Todo) {
        val values = ContentValues().apply {
            put("title", todo.title)
            put("kind", todo.kind.name)
            put("timer_mode", todo.timerMode.name)
            put("duration_min", todo.durationMin)
            if (todo.setId != null) put("set_id", todo.setId) else putNull("set_id")
            put("target_hours", todo.targetHours)
            put("start_date", todo.startDate)
            put("end_date", todo.endDate)
            put("daily_min", todo.dailyMin)
            put("note", todo.note)
            put("sort_order", todo.sortOrder)
            if (todo.bgPath != null) put("bg_path", todo.bgPath) else putNull("bg_path")
            put("break_min", todo.breakMin)
            put("long_break_min", todo.longBreakMin)
            put("rounds", todo.rounds)
            put("reminder_enabled", if (todo.reminderEnabled) 1 else 0)
            put("reminder_hour", todo.reminderHour)
            put("reminder_minute", todo.reminderMinute)
        }
        db.update("todo", values, "id = ?", arrayOf(todo.id.toString()))
    }

    fun moveTodo(todo: Todo, up: Boolean) {
        val list = todos()
        val index = list.indexOfFirst { it.id == todo.id }
        if (index < 0) return
        val target = if (up) index - 1 else index + 1
        if (target !in list.indices) return
        val a = list[index]
        val b = list[target]
        updateOrder(a.id, b.sortOrder.let { if (it == 0) target else it })
        updateOrder(b.id, a.sortOrder.let { if (it == 0) index else it })
        // 两条 sort_order 相同的兜底：直接写入下标
        updateOrder(a.id, target)
        updateOrder(b.id, index)
    }

    private fun updateOrder(id: Long, order: Int) {
        val values = ContentValues().apply { put("sort_order", order) }
        db.update("todo", values, "id = ?", arrayOf(id.toString()))
    }

    fun deleteTodo(id: Long) {
        db.delete("todo", "id = ?", arrayOf(id.toString()))
        // 记录保留：历史统计不该因为删待办而消失
    }

    // ---------- 待办集 ----------

    fun todoSets(): List<TodoSet> {
        val out = mutableListOf<TodoSet>()
        db.query("todo_set", null, null, null, null, null, "sort_order ASC, id ASC").use { c ->
            while (c.moveToNext()) {
                out += TodoSet(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    name = c.getString(c.getColumnIndexOrThrow("name")),
                    color = c.getInt(c.getColumnIndexOrThrow("color")),
                    sortOrder = c.getInt(c.getColumnIndexOrThrow("sort_order")),
                )
            }
        }
        return out
    }

    fun insertTodoSet(set: TodoSet): Long {
        val values = ContentValues().apply {
            put("name", set.name)
            put("color", set.color)
            put("sort_order", set.sortOrder)
        }
        return db.insert("todo_set", null, values)
    }

    fun renameTodoSet(id: Long, name: String) {
        val values = ContentValues().apply { put("name", name) }
        db.update("todo_set", values, "id = ?", arrayOf(id.toString()))
    }

    fun deleteTodoSet(id: Long) {
        db.delete("todo_set", "id = ?", arrayOf(id.toString()))
        val values = ContentValues().apply { putNull("set_id") }
        db.update("todo", values, "set_id = ?", arrayOf(id.toString()))
    }

    fun countTodoInSet(setId: Long): Int {
        db.rawQuery("SELECT COUNT(*) FROM todo WHERE set_id = ?", arrayOf(setId.toString())).use { c ->
            return if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    // ---------- 专注记录 ----------

    fun insertSession(session: FocusSession): Long {
        val values = ContentValues().apply {
            if (session.todoId != null) put("todo_id", session.todoId) else putNull("todo_id")
            put("title", session.title)
            put("start_ts", session.startTs)
            put("end_ts", session.endTs)
            put("seconds", session.seconds)
            put("kind", session.kind)
            put("abandoned", if (session.abandoned) 1 else 0)
        }
        return db.insert("session", null, values)
    }

    fun deleteSession(id: Long) {
        db.delete("session", "id = ?", arrayOf(id.toString()))
    }

    fun sessionsBetween(from: Long, to: Long, kind: String? = "focus"): List<FocusSession> {
        val where = StringBuilder("start_ts >= ? AND start_ts < ?")
        val args = mutableListOf(from.toString(), to.toString())
        if (kind != null) {
            where.append(" AND kind = ?")
            args += kind
        }
        val out = mutableListOf<FocusSession>()
        db.query(
            "session", null, where.toString(), args.toTypedArray(), null, null, "start_ts ASC"
        ).use { c ->
            while (c.moveToNext()) {
                out += FocusSession(
                    id = c.getLong(c.getColumnIndexOrThrow("id")),
                    todoId = if (c.isNull(c.getColumnIndexOrThrow("todo_id"))) null
                    else c.getLong(c.getColumnIndexOrThrow("todo_id")),
                    title = c.getString(c.getColumnIndexOrThrow("title")),
                    startTs = c.getLong(c.getColumnIndexOrThrow("start_ts")),
                    endTs = c.getLong(c.getColumnIndexOrThrow("end_ts")),
                    seconds = c.getInt(c.getColumnIndexOrThrow("seconds")),
                    kind = c.getString(c.getColumnIndexOrThrow("kind")),
                    abandoned = c.getInt(c.getColumnIndexOrThrow("abandoned")) != 0,
                )
            }
        }
        return out
    }

    /** 今天某个待办已经专注的次数 */
    fun todayCount(todoId: Long): Int {
        db.rawQuery(
            "SELECT COUNT(*) FROM session WHERE todo_id = ? AND kind='focus' AND abandoned=0 AND start_ts >= ? AND start_ts < ?",
            arrayOf(todoId.toString(), dayStartMillis().toString(), dayEndMillis().toString())
        ).use { c -> return if (c.moveToFirst()) c.getInt(0) else 0 }
    }

    fun todaySeconds(todoId: Long): Int {
        db.rawQuery(
            "SELECT COALESCE(SUM(seconds),0) FROM session WHERE todo_id = ? AND kind='focus' AND abandoned=0 AND start_ts >= ? AND start_ts < ?",
            arrayOf(todoId.toString(), dayStartMillis().toString(), dayEndMillis().toString())
        ).use { c -> return if (c.moveToFirst()) c.getInt(0) else 0 }
    }

    data class Summary(val count: Int, val seconds: Int, val abandoned: Int)

    fun summary(from: Long, to: Long, kind: String = "focus"): Summary {
        db.rawQuery(
            "SELECT COUNT(*), COALESCE(SUM(seconds),0), COALESCE(SUM(abandoned),0) FROM session WHERE kind = ? AND start_ts >= ? AND start_ts < ?",
            arrayOf(kind, from.toString(), to.toString())
        ).use { c ->
            return if (c.moveToFirst()) Summary(c.getInt(0), c.getInt(1), c.getInt(2))
            else Summary(0, 0, 0)
        }
    }

    /** 按待办标题汇总时长，用于饼图 */
    fun secondsByTitle(from: Long, to: Long): List<Pair<String, Int>> {
        val out = mutableListOf<Pair<String, Int>>()
        db.rawQuery(
            "SELECT title, SUM(seconds) AS s FROM session WHERE kind='focus' AND abandoned=0 AND start_ts >= ? AND start_ts < ? GROUP BY title ORDER BY s DESC",
            arrayOf(from.toString(), to.toString())
        ).use { c ->
            while (c.moveToNext()) out += c.getString(0) to c.getInt(1)
        }
        return out
    }

    /** 每个小时的专注分钟数（0..23） */
    fun hourlyMinutes(from: Long, to: Long): IntArray {
        val arr = IntArray(24)
        db.rawQuery(
            "SELECT start_ts, seconds FROM session WHERE kind='focus' AND abandoned=0 AND start_ts >= ? AND start_ts < ?",
            arrayOf(from.toString(), to.toString())
        ).use { c ->
            while (c.moveToNext()) {
                val ts = c.getLong(0)
                val sec = c.getInt(1)
                val hour = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(ts), ZoneId.systemDefault()
                ).hour
                arr[hour] += sec / 60
            }
        }
        return arr
    }

    /** 最近 n 天每天的专注秒数，index 0 = 最早 */
    fun dailySeconds(days: Int, endDate: LocalDate = LocalDate.now()): List<Pair<LocalDate, Int>> {
        val out = mutableListOf<Pair<LocalDate, Int>>()
        for (i in days - 1 downTo 0) {
            val d = endDate.minusDays(i.toLong())
            val s = summary(dayStartMillis(d), dayEndMillis(d)).seconds
            out += d to s
        }
        return out
    }
}
