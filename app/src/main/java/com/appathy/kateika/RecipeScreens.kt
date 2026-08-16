package com.appathy.kateika

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecipeListScreen(push: (Screen) -> Unit, pop: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar("レシピ", Color(0xFFE07A5F), pop)
        val cats = listOf("主食", "主菜", "副菜", "汁物", "デザート")
        cats.forEach { cat ->
            val list = DataRepo.recipes.filter { it.category == cat }
            if (list.isNotEmpty()) {
                Text(
                    cat,
                    modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 2.dp),
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink
                )
                list.forEach { r ->
                    SectionCard(onClick = { push(Screen.RecipeD(r)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(r.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink, modifier = Modifier.weight(1f))
                            Text("むずかしさ " + "★".repeat(r.difficulty), fontSize = 12.sp, color = Color(0xFFE07A5F))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun Stepper(value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(44.dp).background(Ink, CircleShape).clickable { if (value > 1) onChange(value - 1) },
            contentAlignment = Alignment.Center
        ) { Text("−", color = Color.White, fontSize = 22.sp) }
        Text(
            "$value 人分",
            modifier = Modifier.width(90.dp),
            textAlign = TextAlign.Center,
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink
        )
        Box(
            Modifier.size(44.dp).background(Ink, CircleShape).clickable { if (value < 12) onChange(value + 1) },
            contentAlignment = Alignment.Center
        ) { Text("＋", color = Color.White, fontSize = 22.sp) }
    }
}

@Composable
fun RecipeDetailScreen(r: Recipe, push: (Screen) -> Unit, pop: () -> Unit) {
    var servings by remember { mutableIntStateOf(r.baseServings) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(r.name, Color(0xFFE07A5F), pop)
        SectionCard(bg = Color(0xFFFFE8D6)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("何人分つくる？", fontSize = 13.sp, color = Ink)
                Spacer(Modifier.height(6.dp))
                Stepper(servings) { servings = it }
            }
        }
        Text(
            "材料（$servings 人分）",
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink
        )
        SectionCard {
            Column {
                r.ingredients.forEach { ing ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(ing.name, modifier = Modifier.weight(1f), fontSize = 15.sp, color = Ink)
                        Text(formatAmount(ing, servings, r.baseServings), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                    }
                }
            }
        }
        Text(
            "つくり方",
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink
        )
        r.steps.forEachIndexed { i, s ->
            SectionCard {
                Row {
                    Box(
                        Modifier.size(26.dp).background(Color(0xFFE07A5F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text("${i + 1}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                    Column(Modifier.padding(start = 10.dp)) {
                        Text(s.text, fontSize = 14.sp, lineHeight = 21.sp, color = Ink)
                        Text(
                            "⏱ ${s.time}　🔥 ${s.heat}",
                            fontSize = 12.sp, color = Ink.copy(alpha = 0.6f), modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }
        }
        if (r.point.isNotEmpty()) {
            SectionCard(bg = Color(0xFFFFF3D6)) {
                Text("💡 ポイント：" + r.point, fontSize = 13.sp, lineHeight = 20.sp, color = Ink)
            }
        }
        Button(
            onClick = { push(Screen.AddLog(r.name, servings)) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81B29A))
        ) {
            Text("この料理の実習記録をつける 📷", fontSize = 15.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}
