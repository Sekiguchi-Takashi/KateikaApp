package com.appathy.kateika

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

object CookTimer {
    var label by mutableStateOf("")
    var totalSec by mutableIntStateOf(0)
    var remain by mutableIntStateOf(0)
    var endAt by mutableLongStateOf(0L)
    var running by mutableStateOf(false)
    var paused by mutableStateOf(false)
    var finished by mutableStateOf(false)
    var recipeId by mutableStateOf("")
    var stepIndex by mutableIntStateOf(-1)
    var stepText by mutableStateOf("")
    var stepHeat by mutableStateOf("")
    private var ringtone: Ringtone? = null

    fun start(name: String, seconds: Int, recipe: String = "", index: Int = -1, text: String = "", heat: String = "") {
        stopSound()
        label = name
        totalSec = seconds
        remain = seconds
        endAt = System.currentTimeMillis() + seconds * 1000L
        running = true
        paused = false
        finished = false
        recipeId = recipe
        stepIndex = index
        stepText = text
        stepHeat = heat
    }

    fun pause() {
        if (running && !paused) {
            remain = ((endAt - System.currentTimeMillis()) / 1000L).toInt().coerceAtLeast(0)
            paused = true
        }
    }

    fun resume() {
        if (running && paused) {
            endAt = System.currentTimeMillis() + remain * 1000L
            paused = false
        }
    }

    fun resumeOrPause() {
        if (paused) resume() else pause()
    }

    fun addMinute() {
        if (!running) return
        if (paused) remain += 60 else endAt += 60000L
    }

    fun clear() {
        stopSound()
        running = false
        paused = false
        finished = false
        remain = 0
        label = ""
        recipeId = ""
        stepIndex = -1
        stepText = ""
        stepHeat = ""
    }

    fun ring(ctx: Context) {
        finished = true
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(ctx.applicationContext, uri)
            r?.play()
            ringtone = r
        } catch (e: Exception) {
        }
        try {
            val v = ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            val pattern = longArrayOf(0, 500, 300, 500, 300, 500)
            if (Build.VERSION.SDK_INT >= 26) {
                v?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                v?.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
        }
    }

    fun stopSound() {
        try {
            ringtone?.stop()
        } catch (e: Exception) {
        }
        ringtone = null
    }
}

fun parseSeconds(text: String): Int {
    if (text.isEmpty()) return 0
    var total = 0
    val re = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*(時間|分|秒)")
    for (m in re.findAll(text)) {
        val v = m.groupValues[1].toDoubleOrNull() ?: continue
        total += when (m.groupValues[2]) {
            "時間" -> (v * 3600).toInt()
            "分" -> (v * 60).toInt()
            else -> v.toInt()
        }
    }
    return total
}

fun fmtClock(sec: Int): String {
    val s = sec.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val r = s % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, r) else String.format("%02d:%02d", m, r)
}

@Composable
fun TimerBar() {
    val ctx = LocalContext.current
    if (!CookTimer.running) return
    LaunchedEffect(CookTimer.endAt, CookTimer.paused, CookTimer.running) {
        while (CookTimer.running && !CookTimer.paused) {
            val left = ((CookTimer.endAt - System.currentTimeMillis()) / 1000L).toInt()
            CookTimer.remain = left.coerceAtLeast(0)
            if (left <= 0) {
                if (!CookTimer.finished) CookTimer.ring(ctx)
                break
            }
            delay(300)
        }
    }
    val done = CookTimer.finished
    Column(
        Modifier.fillMaxWidth()
            .background(if (done) Color(0xFFE07A5F) else Ink)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            if (!done) {
                TimerChip(
                    if (CookTimer.paused) "▶ 再開" else "❚❚ 一時停止",
                    Modifier.weight(1f)
                ) { CookTimer.resumeOrPause() }
                Spacer(Modifier.width(8.dp))
                TimerChip("＋1分", Modifier.weight(1f)) { CookTimer.addMinute() }
                Spacer(Modifier.width(8.dp))
            }
            TimerChip(if (done) "止める" else "やめる", Modifier.weight(1f)) { CookTimer.clear() }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            if (done) "⏰ 00:00" else fmtClock(CookTimer.remain),
            color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold
        )
        Text(
            CookTimer.label,
            color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp
        )
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier.fillMaxWidth()
                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    if (done) "この工程はおわりです" else "いまの工程",
                    color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp
                )
                Text(
                    CookTimer.stepText,
                    color = Color.White, fontSize = 15.sp, lineHeight = 22.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (CookTimer.stepHeat.isNotEmpty() && CookTimer.stepHeat != "－") {
                    Text(
                        "🔥 " + CookTimer.stepHeat,
                        color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TimerChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
