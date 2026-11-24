package com.yike.focus.data

import android.media.MediaPlayer

/** 本地音频循环播放，专注页背景音用 */
object MusicPlayer {

    private var player: MediaPlayer? = null

    fun playing(): Boolean = player?.isPlaying == true

    fun start(path: String) {
        stop()
        runCatching {
            player = MediaPlayer().apply {
                setDataSource(path)
                isLooping = true
                setVolume(0.5f, 0.5f)
                prepare()
                start()
            }
        }
    }

    fun stop() {
        runCatching {
            player?.stop()
            player?.release()
        }
        player = null
    }
}
