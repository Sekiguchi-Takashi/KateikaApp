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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private val Veg = Color(0xFFE07A5F)
private val VegLight = Color(0xFFF6D9CF)

@Composable
fun FoodToolsScreen(d: Domain, pop: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("切り方", "相場", "冷蔵庫", "旬")
    Column(Modifier.fillMaxSize()) {
        TopBar("食のツール", Color(d.colorHex), pop)
        Row(Modifier.fillMaxWidth().background(Color(0xFFF3EEE6)).padding(6.dp)) {
            tabs.forEachIndexed { i, t ->
                Box(
                    Modifier.weight(1f).padding(horizontal = 3.dp)
                        .background(if (i == tab) Veg else Color.White, RoundedCornerShape(12.dp))
                        .clickable { tab = i }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        t, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = if (i == tab) Color.White else Ink
                    )
                }
            }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            when (tab) {
                0 -> CutsTab()
                1 -> PriceTab()
                2 -> FridgeTab()
                else -> SeasonTab()
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun CutShape(shape: String) {
    Canvas(Modifier.size(72.dp)) {
        val w = size.width
        val h = size.height
        val ink = Color(0xFF3D405B)
        val fill = Color(0xFFF2C6A0)
        fun rect(x: Float, y: Float, ww: Float, hh: Float) {
            drawRect(fill, Offset(x, y), Size(ww, hh))
            drawRect(ink, Offset(x, y), Size(ww, hh), style = Stroke(width = 3f))
        }
        when (shape) {
            "circle" -> {
                drawCircle(fill, radius = w * 0.36f, center = Offset(w / 2, h / 2))
                drawCircle(ink, radius = w * 0.36f, center = Offset(w / 2, h / 2), style = Stroke(width = 4f))
            }
            "half" -> {
                val p = Path()
                p.moveTo(w * 0.14f, h * 0.6f)
                p.lineTo(w * 0.86f, h * 0.6f)
                p.arcTo(
                    androidx.compose.ui.geometry.Rect(w * 0.14f, h * 0.24f, w * 0.86f, h * 0.96f),
                    0f, -180f, false
                )
                p.close()
                drawPath(p, fill)
                drawPath(p, ink, style = Stroke(width = 4f))
            }
            "quarter" -> {
                val p = Path()
                p.moveTo(w * 0.5f, h * 0.62f)
                p.lineTo(w * 0.88f, h * 0.62f)
                p.arcTo(
                    androidx.compose.ui.geometry.Rect(w * 0.12f, h * 0.26f, w * 0.88f, h * 0.98f),
                    0f, -90f, false
                )
                p.close()
                drawPath(p, fill)
                drawPath(p, ink, style = Stroke(width = 4f))
            }
            "smallcircle" -> {
                listOf(0.28f, 0.5f, 0.72f).forEach { x ->
                    drawCircle(fill, radius = w * 0.12f, center = Offset(w * x, h * 0.5f))
                    drawCircle(ink, radius = w * 0.12f, center = Offset(w * x, h * 0.5f), style = Stroke(width = 3f))
                }
            }
            "strips" -> {
                for (i in 0..4) rect(w * (0.16f + i * 0.14f), h * 0.18f, w * 0.05f, h * 0.64f)
            }
            "tanzaku" -> {
                rect(w * 0.2f, h * 0.24f, w * 0.28f, h * 0.52f)
                rect(w * 0.54f, h * 0.24f, w * 0.28f, h * 0.52f)
            }
            "stick" -> {
                for (i in 0..2) rect(w * (0.22f + i * 0.2f), h * 0.16f, w * 0.12f, h * 0.68f)
            }
            "cube" -> {
                for (i in 0..1) for (j in 0..1)
                    rect(w * (0.24f + i * 0.28f), h * (0.24f + j * 0.28f), w * 0.2f, h * 0.2f)
            }
            "dots" -> {
                val xs = listOf(0.26f, 0.4f, 0.54f, 0.68f, 0.33f, 0.47f, 0.61f, 0.4f, 0.54f)
                val ys = listOf(0.3f, 0.28f, 0.32f, 0.3f, 0.48f, 0.5f, 0.48f, 0.66f, 0.68f)
                xs.forEachIndexed { i, x ->
                    drawCircle(ink, radius = w * 0.045f, center = Offset(w * x, h * ys[i]))
                }
            }
            "random" -> {
                val p = Path()
                p.moveTo(w * 0.2f, h * 0.4f)
                p.lineTo(w * 0.48f, h * 0.2f)
                p.lineTo(w * 0.8f, h * 0.42f)
                p.lineTo(w * 0.66f, h * 0.78f)
                p.lineTo(w * 0.3f, h * 0.72f)
                p.close()
                drawPath(p, fill)
                drawPath(p, ink, style = Stroke(width = 4f))
            }
            "wedge" -> {
                val p = Path()
                p.moveTo(w * 0.5f, h * 0.18f)
                p.lineTo(w * 0.74f, h * 0.82f)
                p.lineTo(w * 0.26f, h * 0.82f)
                p.close()
                drawPath(p, fill)
                drawPath(p, ink, style = Stroke(width = 4f))
                drawLine(ink, Offset(w * 0.5f, h * 0.18f), Offset(w * 0.5f, h * 0.82f), strokeWidth = 3f)
            }
            "slant" -> {
                val p = Path()
                p.moveTo(w * 0.18f, h * 0.62f)
                p.lineTo(w * 0.62f, h * 0.26f)
                p.lineTo(w * 0.82f, h * 0.44f)
                p.lineTo(w * 0.38f, h * 0.78f)
                p.close()
                drawPath(p, fill)
                drawPath(p, ink, style = Stroke(width = 4f))
            }
        }
    }
}

@Composable
fun CutsTab() {
    SectionCard(bg = VegLight) {
        Text(
            "包丁の切り方には名前があります。レシピに出てくる「○○切り」を形で覚えよう。",
            fontSize = 13.sp, lineHeight = 20.sp, color = Ink
        )
    }
    FoodTools.cuts.forEach { c ->
        SectionCard {
            Row {
                CutShape(c.shape)
                Column(Modifier.padding(start = 10.dp)) {
                    Text(c.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                    Text(c.desc, fontSize = 13.sp, lineHeight = 20.sp, color = Ink, modifier = Modifier.padding(top = 3.dp))
                    Text(
                        "使う料理：" + c.use,
                        fontSize = 12.sp, color = Veg, modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PriceRow(p: PriceItem) {
    val col = when {
        p.ratio == 0 -> Ink
        p.ratio >= 120 -> Color(0xFFB00020)
        p.ratio <= 95 -> Color(0xFF2E7D32)
        else -> Ink
    }
    SectionCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.weight(1f))
                Text("${p.price} ${p.unit}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
            if (p.ratio > 0) {
                Text(
                    "平年比 ${p.ratio}%" + when {
                        p.ratio >= 120 -> "　高い"
                        p.ratio <= 95 -> "　安い"
                        else -> "　平年なみ"
                    },
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = col,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(p.hint, fontSize = 12.sp, lineHeight = 18.sp, color = Ink.copy(alpha = 0.7f), modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
fun PriceTab() {
    SectionCard(bg = VegLight) {
        Column {
            Text("いまの相場", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
            Text(
                FoodTools.surveyDate + "　" + FoodTools.priceSource,
                fontSize = 11.sp, lineHeight = 17.sp, color = Ink.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(FoodTools.priceNote, fontSize = 11.sp, color = Ink.copy(alpha = 0.75f), modifier = Modifier.padding(top = 2.dp))
        }
    }
    Text(
        "野菜",
        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink
    )
    FoodTools.vegPrices.forEach { PriceRow(it) }
    Text(
        "肉",
        modifier = Modifier.padding(start = 16.dp, top = 10.dp),
        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink
    )
    FoodTools.meatPrices.forEach { PriceRow(it) }
    Text(
        "買い物のヒント",
        modifier = Modifier.padding(start = 16.dp, top = 10.dp),
        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink
    )
    FoodTools.topics.forEach { t ->
        SectionCard(bg = Color(0xFFFFF3D6)) {
            Text("💡 " + t, fontSize = 13.sp, lineHeight = 20.sp, color = Ink)
        }
    }
}

@Composable
fun FridgeTab() {
    val picked = remember { mutableStateListOf<String>() }
    SectionCard(bg = VegLight) {
        Text(
            "冷蔵庫にあるものをえらぶと、つくれる料理が出てきます。使い切れば食品ロスも減らせます。",
            fontSize = 13.sp, lineHeight = 20.sp, color = Ink
        )
    }
    FoodTools.fridgeItems.chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)) {
            row.forEach { item ->
                val on = picked.contains(item)
                Box(
                    Modifier.weight(1f).padding(horizontal = 3.dp)
                        .background(if (on) Veg else Color.White, RoundedCornerShape(12.dp))
                        .clickable { if (on) picked.remove(item) else picked.add(item) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        item, fontSize = 13.sp,
                        color = if (on) Color.White else Ink,
                        fontWeight = if (on) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
    if (picked.isNotEmpty()) {
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                .background(Color(0xFFEDE7DF), RoundedCornerShape(12.dp))
                .clickable { picked.clear() }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) { Text("えらび直す", fontSize = 13.sp, color = Ink) }
    }
    val results = FoodTools.suggest(picked.toList())
    Text(
        if (picked.isEmpty()) "食材をえらんでください"
        else "つくれる料理 ${results.size} 品",
        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink
    )
    if (picked.isNotEmpty() && results.isEmpty()) {
        SectionCard {
            Text(
                "この組み合わせでつくれる料理は見つかりませんでした。卵・ごはん・キャベツなど、よく使うものを足してみよう。",
                fontSize = 13.sp, lineHeight = 20.sp, color = Ink
            )
        }
    }
    results.forEach { (dish, _) ->
        SectionCard {
            Column {
                Text(dish.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink)
                Text(
                    "つかう：" + dish.need.joinToString("・"),
                    fontSize = 12.sp, color = Veg, modifier = Modifier.padding(top = 3.dp)
                )
                val extra = dish.plus.filter { picked.contains(it) }
                if (extra.isNotEmpty()) {
                    Text(
                        "足すともっとよい：" + extra.joinToString("・"),
                        fontSize = 12.sp, color = Color(0xFF2E7D32), modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(dish.how, fontSize = 13.sp, lineHeight = 20.sp, color = Ink, modifier = Modifier.padding(top = 5.dp))
                val recipe = DataRepo.recipes.firstOrNull { it.name == dish.name }
                if (recipe != null) {
                    Text(
                        "▶ レシピにも登録されています",
                        fontSize = 12.sp, color = Ink.copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SeasonTab() {
    val nowMonth = remember { Calendar.getInstance().get(Calendar.MONTH) + 1 }
    val curId = remember(nowMonth) { FoodTools.currentSeasonId(nowMonth) }
    var open by remember { mutableStateOf(curId) }
    SectionCard(bg = VegLight) {
        Text(FoodTools.seasonNote, fontSize = 13.sp, lineHeight = 20.sp, color = Ink)
    }
    FoodTools.seasons.forEach { s ->
        val isNow = s.id == curId
        val expanded = open == s.id
        SectionCard(
            bg = if (expanded) Color(s.colorHex).copy(alpha = 0.16f) else Color.White,
            onClick = { open = if (expanded) "" else s.id }
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(14.dp).background(Color(s.colorHex), RoundedCornerShape(7.dp)))
                    Text(
                        "  " + s.name + "（" + s.months + "）",
                        fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink,
                        modifier = Modifier.weight(1f)
                    )
                    if (isNow) {
                        Box(
                            Modifier.background(Veg, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) { Text("いまの季節", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    }
                }
                if (expanded) {
                    Text(
                        "野菜",
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(s.vegetables.joinToString("・"), fontSize = 14.sp, lineHeight = 22.sp, color = Ink)
                    Text(
                        "くだもの",
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(s.fruits.joinToString("・"), fontSize = 14.sp, lineHeight = 22.sp, color = Ink)
                    Text(
                        s.note,
                        fontSize = 12.sp, lineHeight = 19.sp, color = Ink.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    Text(
                        s.vegetables.take(4).joinToString("・") + " ほか",
                        fontSize = 12.sp, color = Ink.copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
