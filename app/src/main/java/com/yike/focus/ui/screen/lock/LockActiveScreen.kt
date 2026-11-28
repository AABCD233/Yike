package com.yike.focus.ui.screen.lock

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.yike.focus.data.AppGuard
import com.yike.focus.data.FocusLook
import com.yike.focus.data.FocusTimer
import com.yike.focus.data.LockSettings
import com.yike.focus.data.MusicPlaylist
import com.yike.focus.ui.util.Notify
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * 锁机页：全屏、无退出按钮、隐藏系统栏；
 * 显示当前时间与本次锁机时段，底部可开音乐、白名单应用。
 */
@Composable
fun LockActiveScreen(onExit: () -> Unit) {
    val state by FocusTimer.state.collectAsState()
    val view = LocalView.current
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }
    var showApps by remember { mutableStateOf(false) }
    var showMusic by remember { mutableStateOf(false) }
    var showBg by remember { mutableStateOf(false) }
    var confirmQuit by remember { mutableStateOf(false) }
    var musicOn by remember { mutableStateOf(LockSettings.musicOn) }
    var bgIndex by remember { mutableIntStateOf(LockSettings.bgPreset) }
    var bgImage by remember { mutableStateOf(LockSettings.imagePath) }
    val pickImage = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@runCatching
            val file = java.io.File(context.filesDir, "lock_bg_${System.currentTimeMillis()}.jpg")
            file.writeBytes(bytes)
            LockSettings.imagePath = file.absolutePath
            bgImage = file.absolutePath
        }
    }

    // 锁机页在前台时必须撤掉遮罩，否则按钮会被盖住点不了
    val lifecycle = androidx.compose.ui.platform.LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                com.yike.focus.service.LockShield.hide()
            }
        }
        lifecycle.addObserver(observer)
        com.yike.focus.service.LockShield.hide()
        onDispose { lifecycle.removeObserver(observer) }
    }

    // 隐藏状态栏和导航栏 + 屏幕常亮
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        val activity = view.context as? android.app.Activity
        val controller = activity?.let { WindowCompat.getInsetsController(it.window, view) }
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            view.keepScreenOn = false
            controller?.show(WindowInsetsCompat.Type.systemBars())
            com.yike.focus.service.LockShield.hide()
        }
    }

    // 抢一次音频焦点，让其他应用的音乐停下来
    DisposableEffect(Unit) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val request = am?.let {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .build()
        }
        if (am != null && request != null) runCatching { am.requestAudioFocus(request) }
        onDispose {
            if (am != null && request != null) runCatching { am.abandonAudioFocusRequest(request) }
            MusicPlaylist.release()
        }
    }

    LaunchedEffect(musicOn) {
        if (musicOn) {
            if (!MusicPlaylist.isPlaying) {
                if (MusicPlaylist.tracks.isEmpty()) {
                    // 没有歌单时不播放，提示在设置页里选目录
                    musicOn = false
                    LockSettings.musicOn = false
                } else {
                    MusicPlaylist.playAt(if (MusicPlaylist.index >= 0) MusicPlaylist.index else 0)
                }
            }
        } else if (MusicPlaylist.isPlaying) {
            MusicPlaylist.toggle()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(250)
            tick++
        }
    }

    BackHandler(enabled = true) { }

    val s = state
    if (s == null) {
        if (done) {
            OverlayDialog(
                show = true,
                title = "锁机结束",
                summary = "时间到了，可以拿起手机了。",
                onDismissRequest = onExit,
            ) {
                top.yukonga.miuix.kmp.basic.TextButton(
                    text = "好",
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth(),
                    colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        } else {
            LaunchedEffect(Unit) { onExit() }
        }
        return
    }

    val remaining = remember(tick, s) { FocusTimer.remainingSeconds(s) }
    val startLabel = remember(s) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(s.startWall))
    }
    val endLabel = remember(s) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(s.startWall + s.totalSeconds * 1000L))
    }
    val nowClock = remember(tick) { SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()) }
    val nowDate = remember(tick) {
        SimpleDateFormat("yyyy-MM-dd EEEE", Locale.CHINA).format(Date())
    }

    LaunchedEffect(remaining) {
        if (s.isCountdown && remaining <= 0) {
            FocusTimer.stop(abandoned = false)
            Notify.focusDone(context, "锁机", s.totalSeconds)
            done = true
        }
    }

    val bg = Color(
        FocusLook.backgroundColors.getOrElse(bgIndex) { 0xFF15171C }
            .let { if ((it ushr 24) == 0L) 0xFF15171C else it }
    )
    val bgBitmap = remember(bgImage) {
        bgImage?.let { path ->
            runCatching {
                android.graphics.BitmapFactory.decodeFile(path, android.graphics.BitmapFactory.Options().apply { inSampleSize = 2 })
                    ?.asImageBitmap()
            }.getOrNull()
        }
    }

    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(bg),
    ) {
        val compact = maxHeight < 560.dp
        bgBitmap?.let {
            androidx.compose.foundation.Image(
                bitmap = it,
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)))
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal = 24.dp, vertical = if (compact) 8.dp else 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            Text("锁机中", fontSize = 15.sp, color = Color.White.copy(alpha = 0.75f))
            Spacer(Modifier.height(6.dp))
            Text(
                "剩余 ${formatClock(remaining)}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.55f),
            )

            Spacer(Modifier.height(if (compact) 16.dp else 36.dp))
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    nowClock,
                    fontSize = if (compact) 40.sp else 56.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                )
                Spacer(Modifier.height(8.dp))
                Text(nowDate, fontSize = 15.sp, color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(if (compact) 12.dp else 28.dp))
                Text("本次锁机时间", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
                Text(
                    "$startLabel - $endLabel",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
            Spacer(Modifier.height(if (compact) 18.dp else 40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundAction(
                    label = "音乐",
                    active = musicOn,
                    onClick = { showMusic = true },
                )
                Spacer(Modifier.width(16.dp))
                RoundAction(label = "背景", active = false, onClick = { showBg = true })
                if (LockSettings.allowWhitelist) {
                    Spacer(Modifier.width(16.dp))
                    RoundAction(label = "白名单", active = false, onClick = { showApps = true })
                }
                Spacer(Modifier.width(16.dp))
                RoundAction(label = "提前结束", active = false, onClick = {
                    if (com.yike.focus.data.LockLimit.remaining() > 0) {
                        confirmQuit = true
                    } else {
                        android.widget.Toast.makeText(
                            context, "本月主动退出次数已用完", android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
            Spacer(Modifier.height(28.dp))
            Text(
                "放下手机，做点别的事；这期间退出会回到这里",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
        }
    }

    if (showApps) {
        val apps = remember { AppGuard.whitelist().filter { it != context.packageName } }
        OverlayDialog(
            show = true,
            title = "白名单应用",
            summary = "只能打开这些应用，其他都会被拉回来",
            onDismissRequest = { showApps = false },
        ) {
            Column {
                if (apps.isEmpty()) {
                    Text("还没有设置白名单应用", fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary)
                }
                apps.take(8).forEach { pkg ->
                    val label = remember(pkg) {
                        runCatching {
                            val pm = context.packageManager
                            pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                        }.getOrDefault(pkg)
                    }
                    Text(
                        label,
                        fontSize = 14.sp,
                        color = colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showApps = false
                                runCatching {
                                    context.packageManager.getLaunchIntentForPackage(pkg)?.let {
                                        it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(it)
                                    }
                                }
                            }
                            .padding(vertical = 10.dp),
                    )
                }
            }
        }
    }

    if (confirmQuit) {
        com.yike.focus.ui.component.miuix.ConfirmDialog(
            show = true,
            title = "提前结束锁机",
            summary = "本月还可以主动退出 ${com.yike.focus.data.LockLimit.remaining()} 次，用掉这次就少一次。",
            confirmText = "确认退出",
            dismissText = "继续锁机",
            onConfirm = {
                com.yike.focus.data.LockLimit.consume()
                confirmQuit = false
                FocusTimer.stop(abandoned = true)
                onExit()
            },
            onDismiss = { confirmQuit = false },
        )
    }

    if (showMusic) {
        val track = MusicPlaylist.current()
        OverlayDialog(
            show = true,
            title = "音乐",
            summary = if (track == null) "还没有歌单，去「音乐」页选一个文件夹" else track.title,
            onDismissRequest = { showMusic = false },
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    top.yukonga.miuix.kmp.basic.TextButton(
                        text = "上一首",
                        onClick = { MusicPlaylist.previous() },
                    )
                    top.yukonga.miuix.kmp.basic.TextButton(
                        text = if (MusicPlaylist.isPlaying) "暂停" else "播放",
                        onClick = {
                            if (MusicPlaylist.tracks.isEmpty()) {
                                showMusic = false
                            } else if (MusicPlaylist.isPlaying) {
                                MusicPlaylist.toggle()
                                musicOn = false
                                LockSettings.musicOn = false
                            } else {
                                if (MusicPlaylist.index >= 0) MusicPlaylist.toggle()
                                else MusicPlaylist.playAt(0)
                                musicOn = true
                                LockSettings.musicOn = true
                            }
                        },
                        colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary(),
                    )
                    top.yukonga.miuix.kmp.basic.TextButton(
                        text = "下一首",
                        onClick = { MusicPlaylist.next() },
                    )
                }
            }
        }
    }

    if (showBg) {
        OverlayDialog(
            show = true,
            title = "锁机背景",
            summary = "选一个纯色，或者用自己的图片",
            onDismissRequest = { showBg = false },
        ) {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FocusLook.backgroundColors.forEachIndexed { index, value ->
                        Box(
                            modifier = Modifier
                                .size(if (index == bgIndex) 36.dp else 30.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(value))
                                .clickable {
                                    bgIndex = index
                                    LockSettings.bgPreset = index
                                    LockSettings.imagePath = null
                                    bgImage = null
                                },
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                top.yukonga.miuix.kmp.basic.TextButton(
                    text = "从相册选图",
                    onClick = {
                        showBg = false
                        pickImage.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun RoundAction(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = if (active) 0.28f else 0.14f))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Text(label, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
    }
}

private fun formatClock(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
}
