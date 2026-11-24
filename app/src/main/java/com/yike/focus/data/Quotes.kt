package com.yike.focus.data

import android.content.Context
import com.yike.focus.templateApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

/** 一言（hitokoto）：优先用缓存，拿不到就用内置的句子。 */
object Quotes {

    private val fallback = listOf(
        "不积跬步，无以至千里。",
        "纸上得来终觉浅，绝知此事要躬行。",
        "长风破浪会有时，直挂云帆济沧海。",
        "把注意力放在能控制的事情上。",
        "今天多学一点，明天就少求人一点。",
        "路虽远，行则将至。",
    )

    private fun prefs() = templateApp.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun cached(): String =
        prefs().getString("quote_cached", null) ?: fallback[(System.currentTimeMillis() / 60000 % fallback.size).toInt()]

    fun cachedFrom(): String? = prefs().getString("quote_from", null)

    /** 联网拿一条新的，失败就返回 null（调用方继续用缓存） */
    suspend fun fetch(): Pair<String, String>? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("https://v1.hitokoto.cn/?c=i&c=d&c=k&encode=json&charset=utf-8")
                .build()
            templateApp.okhttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val json = JSONObject(response.body.string())
                val text = json.optString("hitokoto").trim()
                val from = json.optString("from").trim()
                if (text.isEmpty()) null else {
                    prefs().edit()
                        .putString("quote_cached", text)
                        .putString("quote_from", from)
                        .apply()
                    text to from
                }
            }
        }.getOrNull()
    }
}
