package com.appathy.kateika

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameScreen(d: Domain, pop: () -> Unit) {
    when (d.gameId) {
        "sort" -> SortGame(d, pop)
        "laundry" -> LaundryGame(d, pop)
        "order" -> OrderGame(d, pop)
        "shop" -> ShopGame(d, pop)
        "scenario" -> ScenarioGame(d, pop)
        else -> pop()
    }
}

@Composable
fun GameResult(d: Domain, score: Int, max: Int, gameId: String, onRetry: () -> Unit, pop: () -> Unit) {
    Store.setGameBest(gameId, score)
    SectionCard(bg = Color(0xFFFFF3D6)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("スコア", fontSize = 14.sp, color = Ink)
            Text("$score / $max", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text("ベスト: ${Store.gameBest(gameId)}", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
        }
    }
    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onRetry, modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(d.colorHex))) { Text("もう一回") }
        Button(onClick = pop, modifier = Modifier.weight(1f)) { Text("もどる") }
    }
}

@Composable
fun SortGame(d: Domain, pop: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf<String?>(null) }
    val items = remember(round) { DataRepo.sortItems.shuffled() }
    val finished = index >= items.size

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(d.gameName, Color(d.colorHex), pop)
        SectionCard(bg = Color(0xFFEFF3FF)) {
            Text("食べものを3色食品群に仕分けよう！\n赤＝体をつくる　黄＝エネルギー　緑＝調子を整える",
                fontSize = 13.sp, lineHeight = 20.sp, color = Ink)
        }
        if (finished) {
            GameResult(d, score, items.size, "sort",
                onRetry = { round++; index = 0; score = 0; feedback = null }, pop = pop)
        } else {
            Text("${index + 1} / ${items.size}　正解 $score", fontSize = 13.sp,
                color = Ink.copy(alpha = 0.6f), modifier = Modifier.padding(start = 16.dp, top = 8.dp))
            SectionCard {
                Text(items[index].name, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
            val groups = listOf(
                Triple("R", "赤：体をつくる", Color(0xFFE07A5F)),
                Triple("Y", "黄：エネルギー", Color(0xFFE0B25F)),
                Triple("G", "緑：調子を整える", Color(0xFF81B29A))
            )
            groups.forEach { (g, label, c) ->
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
                        .background(c, RoundedCornerShape(12.dp))
                        .clickable {
                            val it0 = items[index]
                            feedback = if (it0.group == g) { score++; "⭕ ${it0.name} は正解！" }
                            else {
                                val correct = when (it0.group) { "R" -> "赤"; "Y" -> "黄"; else -> "緑" }
                                "❌ ${it0.name} は「$correct」のグループ"
                            }
                            index++
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) { Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            }
            if (feedback != null) {
                SectionCard { Text(feedback!!, fontSize = 14.sp, color = Ink) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun LaundrySymbol(key: String) {
    val stroke = Stroke(width = 7f)
    Canvas(Modifier.size(120.dp)) {
        val w = size.width
        val h = size.height
        val ink = androidx.compose.ui.graphics.Color(0xFF3D405B)
        fun bucket() {
            drawLine(ink, Offset(w * 0.12f, h * 0.3f), Offset(w * 0.88f, h * 0.3f), strokeWidth = 7f)
            drawLine(ink, Offset(w * 0.18f, h * 0.3f), Offset(w * 0.3f, h * 0.85f), strokeWidth = 7f)
            drawLine(ink, Offset(w * 0.82f, h * 0.3f), Offset(w * 0.7f, h * 0.85f), strokeWidth = 7f)
            drawLine(ink, Offset(w * 0.3f, h * 0.85f), Offset(w * 0.7f, h * 0.85f), strokeWidth = 7f)
        }
        fun cross() {
            drawLine(androidx.compose.ui.graphics.Color(0xFFB00020), Offset(w * 0.15f, h * 0.15f), Offset(w * 0.85f, h * 0.85f), strokeWidth = 8f)
            drawLine(androidx.compose.ui.graphics.Color(0xFFB00020), Offset(w * 0.85f, h * 0.15f), Offset(w * 0.15f, h * 0.85f), strokeWidth = 8f)
        }
        fun centerText(t: String, ySh: Float = 0f) {
            drawContext.canvas.nativeCanvas.apply {
                val p = android.graphics.Paint()
                p.color = android.graphics.Color.rgb(61, 64, 91)
                p.textSize = w * 0.28f
                p.textAlign = android.graphics.Paint.Align.CENTER
                p.isFakeBoldText = true
                drawText(t, w / 2f, h * 0.68f + ySh, p)
            }
        }
        when (key) {
            "wash40" -> { bucket(); centerText("40") }
            "hand" -> {
                bucket()
                drawOval(ink, topLeft = Offset(w * 0.38f, h * 0.05f), size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.14f))
                drawLine(ink, Offset(w * 0.5f, h * 0.19f), Offset(w * 0.5f, h * 0.45f), strokeWidth = 7f)
            }
            "nobleach" -> {
                val p = Path()
                p.moveTo(w * 0.5f, h * 0.12f)
                p.lineTo(w * 0.9f, h * 0.85f)
                p.lineTo(w * 0.1f, h * 0.85f)
                p.close()
                drawPath(p, ink, style = stroke)
                cross()
            }
            "iron2" -> {
                val p = Path()
                p.moveTo(w * 0.12f, h * 0.75f)
                p.lineTo(w * 0.88f, h * 0.75f)
                p.lineTo(w * 0.8f, h * 0.42f)
                p.quadraticBezierTo(w * 0.5f, h * 0.2f, w * 0.25f, h * 0.42f)
                p.close()
                drawPath(p, ink, style = stroke)
                drawCircle(ink, radius = w * 0.035f, center = Offset(w * 0.42f, h * 0.6f))
                drawCircle(ink, radius = w * 0.035f, center = Offset(w * 0.58f, h * 0.6f))
            }
            "nodry" -> {
                drawRect(ink, topLeft = Offset(w * 0.15f, h * 0.15f), size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.7f), style = stroke)
                drawCircle(ink, radius = w * 0.26f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                cross()
            }
        }
    }
}

@Composable
fun LaundryGame(d: Domain, pop: () -> Unit) {
    var round by remember { mutableIntStateOf(0) }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    val qs = remember(round) { DataRepo.laundry.shuffled() }
    val finished = index >= qs.size

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(d.gameName, Color(d.colorHex), pop)
        if (finished) {
            GameResult(d, score, qs.size, "laundry",
                onRetry = { round++; index = 0; score = 0; selected = -1 }, pop = pop)
        } else {
            val q = qs[index]
            Text("${index + 1} / ${qs.size}", fontSize = 13.sp, color = Ink.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp))
            SectionCard {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    LaundrySymbol(q.symbol)
                    Text(q.q, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 6.dp))
                }
            }
            q.choices.forEachIndexed { i, c ->
                val answered = selected >= 0
                val bg = when {
                    !answered -> Color.White
                    i == q.answer -> Color(0xFFC8E6C9)
                    i == selected -> Color(0xFFFFCDD2)
                    else -> Color.White
                }
                SectionCard(bg = bg, onClick = {
                    if (selected < 0) { selected = i; if (i == q.answer) score++ }
                }) { Text(c, fontSize = 14.sp, color = Ink) }
            }
            if (selected >= 0) {
                SectionCard(bg = Color(0xFFEFF3FF)) { Text(q.note, fontSize = 13.sp, lineHeight = 20.sp, color = Ink) }
                Button(
                    onClick = { index++; selected = -1 },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(d.colorHex))
                ) { Text(if (index + 1 >= qs.size) "けっかを見る" else "つぎへ") }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun OrderGame(d: Domain, pop: () -> Unit) {
    var taskIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }
    val tasks = DataRepo.orders
    val finished = taskIndex >= tasks.size

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(d.gameName, Color(d.colorHex), pop)
        if (finished) {
            GameResult(d, score, tasks.size, "order",
                onRetry = { taskIndex = 0; score = 0; done = false }, pop = pop)
        } else {
            val task = tasks[taskIndex]
            val shuffled = remember(taskIndex) { task.steps.shuffled() }
            val picked = remember(taskIndex) { mutableStateListOf<String>() }
            var judged by remember(taskIndex) { mutableStateOf<Boolean?>(null) }

            Text("${taskIndex + 1} / ${tasks.size}", fontSize = 13.sp, color = Ink.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp))
            SectionCard { Text(task.title + "\n正しい順にタップしよう", fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp, color = Ink) }
            SectionCard(bg = Color(0xFFEFF3FF)) {
                Column {
                    Text("あなたの順番：", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
                    if (picked.isEmpty()) Text("（まだ選んでいません）", fontSize = 13.sp, color = Ink.copy(alpha = 0.5f))
                    picked.forEachIndexed { i, s -> Text("${i + 1}. $s", fontSize = 14.sp, color = Ink, modifier = Modifier.padding(top = 2.dp)) }
                }
            }
            shuffled.forEach { s ->
                if (!picked.contains(s)) {
                    SectionCard(onClick = { if (judged == null) picked.add(s) }) {
                        Text(s, fontSize = 14.sp, color = Ink)
                    }
                }
            }
            if (picked.size == task.steps.size && judged == null) {
                Button(
                    onClick = {
                        val ok = picked.toList() == task.steps
                        judged = ok
                        if (ok) score++
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(d.colorHex))
                ) { Text("判定する") }
            }
            if (judged != null) {
                SectionCard(bg = if (judged == true) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)) {
                    Column {
                        Text(if (judged == true) "⭕ 正解！" else "❌ ざんねん。正しい順番は：", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink)
                        if (judged == false) {
                            task.steps.forEachIndexed { i, s -> Text("${i + 1}. $s", fontSize = 13.sp, color = Ink, modifier = Modifier.padding(top = 2.dp)) }
                        }
                    }
                }
                Button(
                    onClick = { taskIndex++ },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(d.colorHex))
                ) { Text(if (taskIndex + 1 >= tasks.size) "けっかを見る" else "つぎのお題へ") }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ShopGame(d: Domain, pop: () -> Unit) {
    val picked = remember { mutableStateListOf<ShopItem>() }
    var judged by remember { mutableStateOf<String?>(null) }
    var lastScore by remember { mutableIntStateOf(0) }
    val total = picked.sumOf { it.price }
    val budget = DataRepo.shopBudget

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(d.gameName, Color(d.colorHex), pop)
        SectionCard(bg = Color(0xFFEFF3FF)) {
            Text(DataRepo.shopGoal, fontSize = 13.sp, lineHeight = 20.sp, color = Ink)
        }
        SectionCard(bg = if (total > budget) Color(0xFFFFCDD2) else Color(0xFFFFF3D6)) {
            Row(Modifier.fillMaxWidth()) {
                Text("合計 $total 円", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Ink, modifier = Modifier.weight(1f))
                Text("予算 $budget 円", fontSize = 14.sp, color = Ink)
            }
        }
        DataRepo.shopItems.forEach { item ->
            val sel = picked.contains(item)
            val gc = when (item.group) {
                "R" -> Color(0xFFE07A5F); "Y" -> Color(0xFFE0B25F); "G" -> Color(0xFF81B29A); else -> Color(0xFF9E9E9E)
            }
            SectionCard(bg = if (sel) gc.copy(alpha = 0.25f) else Color.White, onClick = {
                if (judged == null) { if (sel) picked.remove(item) else picked.add(item) }
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(12.dp).background(gc, RoundedCornerShape(6.dp)))
                    Text((if (sel) "✓ " else "") + item.name, fontSize = 14.sp, color = Ink, modifier = Modifier.weight(1f).padding(start = 8.dp))
                    Text("${item.price}円", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
            }
        }
        if (judged == null) {
            Button(
                onClick = {
                    val hasR = picked.any { it.group == "R" }
                    val hasY = picked.any { it.group == "Y" }
                    val hasG = picked.any { it.group == "G" }
                    val junk = picked.count { it.group == "-" }
                    val within = total <= budget
                    var s = 0
                    if (hasR) s += 25
                    if (hasY) s += 25
                    if (hasG) s += 25
                    if (within) s += 25
                    s -= junk * 10
                    if (s < 0) s = 0
                    lastScore = s
                    val msgs = mutableListOf<String>()
                    if (!within) msgs.add("予算オーバー！（${total - budget}円こえた）")
                    if (!hasR) msgs.add("赤（体をつくる）が足りない")
                    if (!hasY) msgs.add("黄（エネルギー）が足りない")
                    if (!hasG) msgs.add("緑（調子を整える）が足りない")
                    if (junk > 0) msgs.add("おかし・ジュースは今回の目的には不要（−${junk * 10}点）")
                    judged = if (msgs.isEmpty()) "🎉 パーフェクト！バランスよく予算内で買えた！（残り ${budget - total} 円）"
                    else msgs.joinToString("\n")
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(d.colorHex))
            ) { Text("レジに行く（判定）") }
        } else {
            SectionCard(bg = Color(0xFFFFF3D6)) { Text(judged!!, fontSize = 14.sp, lineHeight = 21.sp, color = Ink) }
            GameResult(d, lastScore, 100, "shop",
                onRetry = { picked.clear(); judged = null }, pop = pop)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun ScenarioGame(d: Domain, pop: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    val list = DataRepo.scenarios
    val finished = index >= list.size

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(d.gameName, Color(d.colorHex), pop)
        if (finished) {
            GameResult(d, score, list.size, "scenario",
                onRetry = { index = 0; score = 0; selected = -1 }, pop = pop)
        } else {
            val sc = list[index]
            Text("${index + 1} / ${list.size}", fontSize = 13.sp, color = Ink.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp))
            SectionCard(bg = Color(d.colorHex).copy(alpha = 0.12f)) {
                Column {
                    Text(sc.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                    Text(sc.situation, fontSize = 14.sp, lineHeight = 21.sp, color = Ink, modifier = Modifier.padding(top = 6.dp))
                }
            }
            sc.choices.forEachIndexed { i, c ->
                val answered = selected >= 0
                val bg = when {
                    !answered -> Color.White
                    i == sc.best -> Color(0xFFC8E6C9)
                    i == selected -> Color(0xFFFFCDD2)
                    else -> Color.White
                }
                SectionCard(bg = bg, onClick = {
                    if (selected < 0) { selected = i; if (i == sc.best) score++ }
                }) { Text(c, fontSize = 14.sp, lineHeight = 20.sp, color = Ink) }
            }
            if (selected >= 0) {
                SectionCard(bg = Color(0xFFEFF3FF)) { Text(sc.note, fontSize = 13.sp, lineHeight = 20.sp, color = Ink) }
                Button(
                    onClick = { index++; selected = -1 },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(d.colorHex))
                ) { Text(if (index + 1 >= list.size) "けっかを見る" else "つぎのシナリオへ") }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
