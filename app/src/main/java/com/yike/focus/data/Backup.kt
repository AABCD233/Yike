package com.yike.focus.data

import org.json.JSONArray
import org.json.JSONObject

/** 本地备份：一份 JSON 装下待办集、待办和专注记录。 */
object Backup {

    private const val VERSION = 1

    fun exportJson(): String {
        val root = JSONObject()
        root.put("version", VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        val setsArray = JSONArray()
        TodoStore.todoSets().forEach { set ->
            setsArray.put(
                JSONObject().apply {
                    put("name", set.name)
                    put("color", set.color)
                    put("sortOrder", set.sortOrder)
                }
            )
        }
        root.put("sets", setsArray)

        val todosArray = JSONArray()
        TodoStore.todos().forEach { todo ->
            todosArray.put(
                JSONObject().apply {
                    put("title", todo.title)
                    put("kind", todo.kind.name)
                    put("timerMode", todo.timerMode.name)
                    put("durationMin", todo.durationMin)
                    put("setId", todo.setId ?: JSONObject.NULL)
                    put("targetHours", todo.targetHours.toDouble())
                    put("startDate", todo.startDate ?: JSONObject.NULL)
                    put("endDate", todo.endDate ?: JSONObject.NULL)
                    put("dailyMin", todo.dailyMin)
                    put("note", todo.note)
                    put("createdAt", todo.createdAt)
                }
            )
        }
        root.put("todos", todosArray)

        val sessionsArray = JSONArray()
        TodoStore.sessionsBetween(0L, Long.MAX_VALUE / 2, kind = null).forEach { s ->
            sessionsArray.put(
                JSONObject().apply {
                    put("title", s.title)
                    put("startTs", s.startTs)
                    put("endTs", s.endTs)
                    put("seconds", s.seconds)
                    put("kind", s.kind)
                    put("abandoned", s.abandoned)
                }
            )
        }
        root.put("sessions", sessionsArray)
        return root.toString(2)
    }

    data class ImportResult(val sets: Int, val todos: Int, val sessions: Int)

    /** 恢复时按名字合并待办集，待办和记录直接追加 */
    fun importJson(text: String): ImportResult {
        val root = JSONObject(text)
        var sets = 0
        var todos = 0
        var sessions = 0

        val nameToId = TodoStore.todoSets().associate { it.name to it.id }.toMutableMap()
        val setsJson = root.optJSONArray("sets") ?: JSONArray()
        for (i in 0 until setsJson.length()) {
            val o = setsJson.getJSONObject(i)
            val name = o.optString("name").ifBlank { "未命名集合" }
            if (nameToId.containsKey(name)) continue
            val id = TodoStore.insertTodoSet(
                TodoSet(name = name, color = o.optInt("color", 0), sortOrder = o.optInt("sortOrder", 0))
            )
            nameToId[name] = id
            sets++
        }

        val todosJson = root.optJSONArray("todos") ?: JSONArray()
        for (i in 0 until todosJson.length()) {
            val o = todosJson.getJSONObject(i)
            val title = o.optString("title")
            if (title.isBlank()) continue
            todoStoreInsert(title, o)
            todos++
        }

        val sessionsJson = root.optJSONArray("sessions") ?: JSONArray()
        for (i in 0 until sessionsJson.length()) {
            val o = sessionsJson.getJSONObject(i)
            TodoStore.insertSession(
                FocusSession(
                    todoId = null,
                    title = o.optString("title"),
                    startTs = o.optLong("startTs"),
                    endTs = o.optLong("endTs"),
                    seconds = o.optInt("seconds"),
                    kind = o.optString("kind", "focus"),
                    abandoned = o.optBoolean("abandoned", false),
                )
            )
            sessions++
        }
        return ImportResult(sets, todos, sessions)
    }

    private fun todoStoreInsert(title: String, o: JSONObject) {
        TodoStore.insertTodo(
            Todo(
                title = title,
                kind = runCatching { TodoKind.valueOf(o.optString("kind", "NORMAL")) }
                    .getOrDefault(TodoKind.NORMAL),
                timerMode = runCatching { TimerMode.valueOf(o.optString("timerMode", "COUNTDOWN")) }
                    .getOrDefault(TimerMode.COUNTDOWN),
                durationMin = o.optInt("durationMin", 25),
                setId = null,
                targetHours = o.optDouble("targetHours", 0.0).toFloat(),
                startDate = o.optString("startDate").takeIf { it.isNotBlank() && it != "null" },
                endDate = o.optString("endDate").takeIf { it.isNotBlank() && it != "null" },
                dailyMin = o.optInt("dailyMin", 0),
                note = o.optString("note"),
                createdAt = o.optLong("createdAt", System.currentTimeMillis()),
            )
        )
    }
}
