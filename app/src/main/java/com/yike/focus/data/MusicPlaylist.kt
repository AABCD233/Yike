package com.yike.focus.data

import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yike.focus.templateApp
import java.io.File
import kotlin.random.Random

/**
 * 极简播放器：选一个目录 → 把这个目录（含一层子目录）里的音频排进列表，
 * 支持列表循环、单曲循环、随机、顺序播完就停。
 */
object MusicPlaylist {

    enum class Mode(val label: String) {
        LIST_LOOP("列表循环"),
        ONE("单曲循环"),
        SHUFFLE("随机播放"),
        SEQUENCE("顺序播放"),
    }

    data class Track(val path: String, val title: String)

    val tracks = mutableStateListOf<Track>()
    var index by mutableIntStateOf(-1)
    var mode by mutableStateOf(Mode.LIST_LOOP)
    var isPlaying by mutableStateOf(false)

    private var player: MediaPlayer? = null

    private val audioExt = setOf("mp3", "m4a", "aac", "ogg", "wav", "flac", "opus")

    fun current(): Track? = tracks.getOrNull(index)

    fun setDirectory(path: String) {
        val dir = File(path)
        val found = mutableListOf<Track>()
        fun scan(d: File, depth: Int) {
            val files = d.listFiles() ?: return
            files.forEach { f ->
                when {
                    f.isFile && f.extension.lowercase() in audioExt ->
                        found += Track(f.absolutePath, f.nameWithoutExtension)
                    f.isDirectory && depth < 1 -> scan(f, depth + 1)
                }
            }
        }
        scan(dir, 0)
        tracks.clear()
        tracks.addAll(found.sortedBy { it.title.lowercase() })
        index = -1
        if (tracks.isNotEmpty()) playAt(0)
    }

    fun playAt(i: Int) {
        if (tracks.isEmpty()) return
        index = i.coerceIn(0, tracks.lastIndex)
        val path = tracks[index].path
        release()
        runCatching {
            player = MediaPlayer().apply {
                setDataSource(path)
                setOnCompletionListener { onTrackEnd() }
                prepare()
                start()
            }
            isPlaying = true
        }
        MusicPlayer.stop()
    }

    fun toggle() {
        val p = player ?: return
        if (p.isPlaying) {
            p.pause()
            isPlaying = false
        } else {
            p.start()
            isPlaying = true
        }
    }

    fun next() {
        if (tracks.isEmpty()) return
        when (mode) {
            Mode.SHUFFLE -> playAt(Random.nextInt(tracks.size))
            Mode.ONE, Mode.LIST_LOOP -> playAt((index + 1) % tracks.size)
            Mode.SEQUENCE -> {
                if (index + 1 < tracks.size) playAt(index + 1) else release()
            }
        }
    }

    fun previous() {
        if (tracks.isEmpty()) return
        playAt((index - 1 + tracks.size) % tracks.size)
    }

    fun setMode(m: Mode) {
        mode = m
        prefs().edit().putString("music_mode", m.name).apply()
    }

    private fun onTrackEnd() {
        when (mode) {
            Mode.ONE -> playAt(index)
            else -> next()
        }
    }

    fun release() {
        runCatching {
            player?.stop()
            player?.release()
        }
        player = null
        isPlaying = false
    }

    private fun prefs() = templateApp.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)

    fun restoreMode() {
        val name = prefs().getString("music_mode", null) ?: return
        runCatching { mode = Mode.valueOf(name) }
    }

    fun savedDirectory(): String? = prefs().getString("music_dir", null)

    fun saveDirectory(path: String) {
        prefs().edit().putString("music_dir", path).apply()
    }
}
