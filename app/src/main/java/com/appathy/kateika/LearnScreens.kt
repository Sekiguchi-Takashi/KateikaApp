package com.appathy.kateika

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DomainScreen(d: Domain, push: (Screen) -> Unit, pop: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(d.name, Color(d.colorHex), pop)
        val guide = CharRepo.forDomain(d.id)
        if (guide != null) {
            SectionCard(bg = Color(d.colorHex).copy(alpha = 0.10f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CharImage(guide.file, Modifier.height(64.dp), silhouette = !CharRepo.isUnlocked(guide))
                    Column(Modifier.padding(start = 10.dp)) {
                        Text(
                            if (CharRepo.isUnlocked(guide)) guide.name else "？？？",
                            fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink
                        )
                        Text(
                            if (CharRepo.isUnlocked(guide)) "「" + guide.tip + "」" else guide.unlockText + "と登録されます",
                            fontSize = 12.sp, lineHeight = 18.sp, color = Ink.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
        val best = Store.gameBest(d.gameId)
        SectionCard(bg = Color(d.colorHex).copy(alpha = 0.15f), onClick = { push(Screen.Game(d)) }) {
            Column {
                Text((if (d.gameId == "foodtools") "🔧 " else "🎮 ") + d.gameName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                Text(
                    if (d.gameId == "foodtools") "切り方・相場・冷蔵庫・旬の4つの道具"
                    else if (best >= 0) "ベストスコア: $best 点" else "あそんで学ぼう（未プレイ）",
                    fontSize = 12.sp, color = Ink.copy(alpha = 0.7f), modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Text(
            "レッスン",
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 2.dp),
            fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink
        )
        d.lessons.forEachIndexed { i, l ->
            val read = Store.isRead(l.id)
            val qBest = Store.quizBest(l.id)
            SectionCard(onClick = { push(Screen.LessonS(d, l)) }) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (read) "✅" else "📖", fontSize = 16.sp)
                        Text(
                            " ${i + 1}. ${l.title}",
                            fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink
                        )
                    }
                    Text(
                        if (qBest >= 0) "クイズ ベスト: $qBest / ${l.quiz.size} 問" else "クイズ ${l.quiz.size} 問つき",
                        fontSize = 12.sp, color = Ink.copy(alpha = 0.6f), modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun LessonScreen(d: Domain, l: Lesson, push: (Screen) -> Unit, pop: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(l.title, Color(d.colorHex), pop)
        l.body.forEach { para ->
            SectionCard {
                Text(para, fontSize = 15.sp, lineHeight = 24.sp, color = Ink)
            }
        }
        Button(
            onClick = {
                Store.setRead(l.id)
                push(Screen.QuizS(d, l))
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(d.colorHex))
        ) {
            Text("クイズにちょうせん（${l.quiz.size}問）", fontSize = 16.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun QuizScreen(d: Domain, l: Lesson, pop: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var finished by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar("クイズ：${l.title}", Color(d.colorHex), pop)
        if (finished) {
            Store.setQuizBest(l.id, score)
            SectionCard(bg = Color(0xFFFFF3D6)) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("けっか", fontSize = 14.sp, color = Ink)
                    Text("$score / ${l.quiz.size} 問 正解！", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text(
                        when {
                            score == l.quiz.size -> "パーフェクト！すばらしい！"
                            score >= l.quiz.size / 2 -> "よくできました。まちがえた問題を見直そう。"
                            else -> "レッスンをもう一度読んでみよう。"
                        },
                        fontSize = 13.sp, color = Ink.copy(alpha = 0.75f), modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
            val voice = CharRepo.forDomain(d.id) ?: CharRepo.current()
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CharImage(voice.file, Modifier.height(70.dp))
                    Column(Modifier.padding(start = 10.dp)) {
                        Text(voice.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
                        Text(
                            "「" + (if (score >= (l.quiz.size + 1) / 2) voice.praise else voice.cheer) + "」",
                            fontSize = 14.sp, lineHeight = 21.sp, color = Ink,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            Button(onClick = pop, modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("もどる") }
        } else {
            val q = l.quiz[index]
            Text(
                "第 ${index + 1} 問 / ${l.quiz.size}",
                modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                fontSize = 13.sp, color = Ink.copy(alpha = 0.6f)
            )
            SectionCard { Text(q.q, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink, lineHeight = 24.sp) }
            q.choices.forEachIndexed { i, c ->
                val answered = selected >= 0
                val bg = when {
                    !answered -> Color.White
                    i == q.answer -> Color(0xFFC8E6C9)
                    i == selected -> Color(0xFFFFCDD2)
                    else -> Color.White
                }
                SectionCard(bg = bg, onClick = {
                    if (selected < 0) {
                        selected = i
                        if (i == q.answer) score++
                    }
                }) {
                    Text(c, fontSize = 15.sp, color = Ink)
                }
            }
            if (selected >= 0) {
                SectionCard(bg = Color(0xFFEFF3FF)) {
                    Text(
                        (if (selected == q.answer) "⭕ 正解！ " else "❌ ざんねん。 ") + q.note,
                        fontSize = 13.sp, color = Ink, lineHeight = 20.sp
                    )
                }
                Button(
                    onClick = {
                        if (index + 1 >= l.quiz.size) finished = true
                        else { index++; selected = -1 }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(d.colorHex))
                ) {
                    Text(if (index + 1 >= l.quiz.size) "けっかを見る" else "つぎの問題へ")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
