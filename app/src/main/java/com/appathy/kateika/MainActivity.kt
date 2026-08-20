package com.appathy.kateika

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class Screen {
    data object Home : Screen()
    data class DomainS(val d: Domain) : Screen()
    data class LessonS(val d: Domain, val l: Lesson) : Screen()
    data class QuizS(val d: Domain, val l: Lesson) : Screen()
    data object RecipeList : Screen()
    data class RecipeD(val r: Recipe) : Screen()
    data object LogList : Screen()
    data class AddLog(val recipeName: String, val servings: Int) : Screen()
    data class Game(val d: Domain) : Screen()
    data object Chars : Screen()
    data class CharDetail(val c: Character) : Screen()
    data class RecipeEdit(val base: Recipe?) : Screen()
}

val Ink = Color(0xFF3D405B)
val Paper = Color(0xFFFBF6EF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataRepo.load(this)
        Store.init(this)
        CharRepo.load(this)
        FoodTools.load(this)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFFE07A5F),
                    secondary = Color(0xFF81B29A),
                    background = Paper,
                    surface = Paper
                )
            ) {
                val stack = remember { mutableStateListOf<Screen>(Screen.Home) }
                val push: (Screen) -> Unit = { stack.add(it) }
                val pop: () -> Unit = { if (stack.size > 1) stack.removeAt(stack.size - 1) }
                BackHandler(enabled = stack.size > 1) { pop() }
                Surface(Modifier.fillMaxSize(), color = Paper) {
                  Column(Modifier.fillMaxSize()) {
                    TimerBar()
                    Box(Modifier.weight(1f)) {
                    when (val cur = stack.last()) {
                        is Screen.Home -> HomeScreen(push)
                        is Screen.DomainS -> DomainScreen(cur.d, push, pop)
                        is Screen.LessonS -> LessonScreen(cur.d, cur.l, push, pop)
                        is Screen.QuizS -> QuizScreen(cur.d, cur.l, pop)
                        is Screen.RecipeList -> RecipeListScreen(push, pop)
                        is Screen.RecipeD -> RecipeDetailScreen(cur.r, push, pop)
                        is Screen.LogList -> LogListScreen(push, pop)
                        is Screen.AddLog -> AddLogScreen(cur.recipeName, cur.servings, pop)
                        is Screen.Game -> GameScreen(cur.d, pop)
                        is Screen.Chars -> CharacterListScreen(push, pop)
                        is Screen.CharDetail -> CharDetailScreen(cur.c, pop)
                        is Screen.RecipeEdit -> RecipeEditScreen(cur.base, pop)
                    }
                    }
                  }
                }
            }
        }
    }
}

@Composable
fun TopBar(title: String, color: Color = Ink, onBack: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().background(color).padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Text(
                "◀",
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.clickable { onBack() }.padding(end = 12.dp)
            )
        }
        Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SectionCard(bg: Color = Color.White, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val base = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
    val mod = if (onClick != null) base.clickable { onClick() } else base
    Card(
        modifier = mod,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.padding(14.dp)) { content() }
    }
}

@Composable
fun HomeScreen(push: (Screen) -> Unit) {
    val ctx = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    val room = remember { CharRepo.image(ctx, CharRepo.roomImage) }
    val fresh = remember(refresh) { CharRepo.newlyUnlocked() }
    val me = remember(refresh) { CharRepo.current() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar("家庭科室", Color(0xFFE07A5F))
        Box(Modifier.fillMaxWidth().height(230.dp)) {
            if (room != null) {
                Image(
                    bitmap = room.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Row(
                Modifier.fillMaxSize().padding(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                SpeechBubble(
                    if (fresh.isEmpty()) me.greet else fresh.first().name + "が家庭科室にやってきたよ！",
                    Modifier.weight(1f).padding(bottom = 20.dp, end = 6.dp)
                )
                Box(
                    Modifier.width(130.dp).fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    CharImage(
                        if (fresh.isEmpty()) me.file else fresh.first().file,
                        Modifier.height(210.dp)
                    )
                }
            }
        }
        if (fresh.isNotEmpty()) {
            SectionCard(bg = Color(0xFFF2E9FA), onClick = {
                Store.markSeen(fresh.map { it.id })
                refresh++
                push(Screen.Chars)
            }) {
                Column {
                    Text(
                        "🎉 新しい仲間が " + fresh.size + "人 登録されました",
                        fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink
                    )
                    Text(
                        fresh.joinToString("、") { it.name } + " — タップして確認",
                        fontSize = 12.sp, color = Ink.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        SectionCard(bg = Color(0xFFF2E9FA), onClick = { push(Screen.Chars) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CharImage(me.file, Modifier.height(52.dp))
                Column(Modifier.padding(start = 10.dp).weight(1f)) {
                    Text("登録キャラクター", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                    Text(
                        "${CharRepo.unlockedList().size} / ${CharRepo.characters.size} 人が登録ずみ",
                        fontSize = 12.sp, color = Ink.copy(alpha = 0.75f)
                    )
                }
            }
        }
        Text(
            "まなぶ",
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 2.dp),
            color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold
        )
        DataRepo.domains.forEach { d ->
            val total = d.lessons.size
            val read = d.lessons.count { Store.isRead(it.id) }
            SectionCard(bg = Color(d.colorHex).copy(alpha = 0.12f), onClick = { push(Screen.DomainS(d)) }) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(14.dp).background(Color(d.colorHex), RoundedCornerShape(7.dp)))
                        Text(
                            d.name,
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink
                        )
                        Text("  $read / $total", fontSize = 13.sp, color = Ink.copy(alpha = 0.6f))
                    }
                    Text(
                        d.desc, fontSize = 12.sp, color = Ink.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        Text(
            "実技（料理）",
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 2.dp),
            color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold
        )
        SectionCard(bg = Color(0xFFFFE8D6), onClick = { push(Screen.RecipeList) }) {
            Column {
                Text("レシピ（人数をきめて材料計算）", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                Text(
                    "オーソドックスな料理${DataRepo.recipes.size}品。人数を指定すると材料と手順を表示。",
                    fontSize = 12.sp, color = Ink.copy(alpha = 0.75f), modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        SectionCard(bg = Color(0xFFDDEBE3), onClick = { push(Screen.LogList) }) {
            Column {
                Text("実習記録アルバム", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                Text(
                    "つくった料理の写真・感想・日時をきろく（${Store.logs().size}件）",
                    fontSize = 12.sp, color = Ink.copy(alpha = 0.75f), modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
            Text("Appathy — Less Motivation, More Automation", fontSize = 10.sp, color = Ink.copy(alpha = 0.4f))
        }
    }
}
