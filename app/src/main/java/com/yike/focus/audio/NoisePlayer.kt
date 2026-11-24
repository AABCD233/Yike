package com.yike.focus.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * 白噪音播放器：不打包音频文件，直接算噪声样本写进 AudioTrack。
 * white = 白噪音，pink = 低频更厚的粉噪音，brown = 更闷的棕噪音。
 */
object NoisePlayer {

    private const val SAMPLE_RATE = 44100
    private var track: AudioTrack? = null
    private var thread: Thread? = null

    @Volatile
    private var running = false

    fun isPlaying(): Boolean = running

    fun start(kind: String) {
        if (kind == "none") {
            stop()
            return
        }
        stop()
        running = true
        thread = Thread {
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )
            val bufferSize = maxOf(minBuf, SAMPLE_RATE / 4)
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            track = audioTrack
            audioTrack.play()

            val chunk = ShortArray(2048)
            var low = 0.0
            var brown = 0.0
            while (running) {
                for (i in chunk.indices) {
                    val white = Random.nextDouble(-1.0, 1.0)
                    val sample = when (kind) {
                        "pink" -> {
                            low = 0.985 * low + 0.015 * white
                            (low * 6.0).coerceIn(-1.0, 1.0)
                        }
                        "brown" -> {
                            brown = (brown + 0.02 * white).coerceIn(-1.0, 1.0)
                            brown * 3.5
                        }
                        else -> white
                    }
                    // 音量压到 35%，白噪音开满会吵
                    chunk[i] = (sample * 0.35 * Short.MAX_VALUE).roundToInt().toShort()
                }
                val written = audioTrack.write(chunk, 0, chunk.size)
                if (written < 0) break
            }
            runCatching {
                audioTrack.stop()
                audioTrack.release()
            }
            track = null
        }.also { it.isDaemon = true; it.start() }
    }

    fun stop() {
        running = false
        runCatching { thread?.join(300) }
        thread = null
        runCatching { track?.stop() }
        runCatching { track?.release() }
        track = null
    }
}
