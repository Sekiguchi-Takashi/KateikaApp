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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class IngRow(name: String, amount: String, unit: String) {
    var name by mutableStateOf(name)
    var amount by mutableStateOf(amount)
    var unit by mutableStateOf(unit)
}

class StepRow(text: String, minutes: String, heat: String) {
    var text by mutableStateOf(text)
    var minutes by mutableStateOf(minutes)
    var heat by mutableStateOf(heat)
}

private fun minutesOf(time: String): String {
    val sec = parseSeconds(time)
    if (sec <= 0) return ""
    val m = sec / 60.0
    return if (m == Math.floor(m)) m.toInt().toString() else String.format("%.1f", m)
}

@Composable
fun RecipeEditScreen(base: Recipe?, pop: () -> Unit) {
    val editing = base != null && base.id.startsWith("my_")
    var name by remember { mutableStateOf(if (base == null) "" else if (editing) base.name else base.name + "（マイ）") }
    var category by remember { mutableStateOf(base?.category ?: "主菜") }
    var servings by remember { mutableIntStateOf(base?.baseServings ?: 2) }
    var point by remember { mutableStateOf(base?.point ?: "") }
    val ings = remember {
        mutableStateListOf<IngRow>().also { list ->
            if (base == null) repeat(3) { list.add(IngRow("", "", "g")) }
            else base.ingredients.forEach { i ->
                val a = if (i.amount == Math.floor(i.amount)) i.amount.toInt().toString()
                else i.amount.toString()
                list.add(IngRow(i.name, if (i.amount <= 0.0) "" else a, i.unit))
            }
        }
    }
    val steps = remember {
        mutableStateListOf<StepRow>().also { list ->
            if (base == null) repeat(3) { list.add(StepRow("", "", "中火")) }
            else base.steps.forEach { s -> list.add(StepRow(s.text, minutesOf(s.time), s.heat)) }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(if (editing) "レシピを編集" else "マイレシピをつくる", Color(0xFFE07A5F), pop)
        SectionCard {
            Column {
                Text("料理名", fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(Modifier.height(10.dp))
                Text("分類", fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                Row(Modifier.padding(top = 4.dp)) {
                    listOf("主食", "主菜", "副菜", "汁物", "デザート").forEach { c ->
                        Box(
                            Modifier.padding(end = 6.dp)
                                .background(
                                    if (c == category) Color(0xFFE07A5F) else Color(0xFFEDE7DF),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { category = c }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                c, fontSize = 12.sp,
                                color = if (c == category) Color.White else Ink
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("基準の人数（この分量で入力）", fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                Spacer(Modifier.height(4.dp))
                Stepper(servings, { servings = it })
            }
        }
        Text(
            "材料",
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink
        )
        ings.forEachIndexed { i, row ->
            SectionCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${i + 1}", fontSize = 13.sp, color = Ink.copy(alpha = 0.6f), modifier = Modifier.width(20.dp))
                        OutlinedTextField(
                            value = row.name, onValueChange = { row.name = it },
                            modifier = Modifier.weight(1f), singleLine = true,
                            placeholder = { Text("材料名", fontSize = 13.sp) }
                        )
                    }
                    Row(Modifier.padding(top = 6.dp, start = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = row.amount, onValueChange = { row.amount = it },
                            modifier = Modifier.width(100.dp), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("数量", fontSize = 13.sp) }
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = row.unit, onValueChange = { row.unit = it },
                            modifier = Modifier.weight(1f), singleLine = true,
                            placeholder = { Text("単位（g・個・大さじ）", fontSize = 12.sp) }
                        )
                        Text(
                            "けす",
                            fontSize = 12.sp, color = Color(0xFFB00020),
                            modifier = Modifier.clickable { ings.removeAt(i) }.padding(8.dp)
                        )
                    }
                    Text(
                        "数量を空欄にすると「適量」になり、人数をかえても換算されません",
                        fontSize = 11.sp, color = Ink.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp, start = 20.dp)
                    )
                }
            }
        }
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                .background(Color(0xFFEDE7DF), RoundedCornerShape(12.dp))
                .clickable { ings.add(IngRow("", "", "g")) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) { Text("＋ 材料をふやす", fontSize = 13.sp, color = Ink) }

        Text(
            "つくり方とタイマー",
            modifier = Modifier.padding(start = 16.dp, top = 12.dp),
            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink
        )
        steps.forEachIndexed { i, row ->
            SectionCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${i + 1}", fontSize = 13.sp, color = Ink.copy(alpha = 0.6f), modifier = Modifier.width(20.dp))
                        OutlinedTextField(
                            value = row.text, onValueChange = { row.text = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("手順の内容", fontSize = 13.sp) }
                        )
                    }
                    Row(Modifier.padding(top = 6.dp, start = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = row.minutes, onValueChange = { row.minutes = it },
                            modifier = Modifier.width(110.dp), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("分", fontSize = 13.sp) }
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = row.heat, onValueChange = { row.heat = it },
                            modifier = Modifier.weight(1f), singleLine = true,
                            placeholder = { Text("火加減", fontSize = 13.sp) }
                        )
                        Text(
                            "けす",
                            fontSize = 12.sp, color = Color(0xFFB00020),
                            modifier = Modifier.clickable { steps.removeAt(i) }.padding(8.dp)
                        )
                    }
                    val sec = ((row.minutes.toDoubleOrNull() ?: 0.0) * 60).toInt()
                    Text(
                        if (sec > 0) "この手順に ${fmtClock(sec)} のタイマーがつきます"
                        else "分を入れるとタイマーがつきます（空欄ならタイマーなし）",
                        fontSize = 11.sp,
                        color = if (sec > 0) Color(0xFF81B29A) else Ink.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp, start = 20.dp)
                    )
                }
            }
        }
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                .background(Color(0xFFEDE7DF), RoundedCornerShape(12.dp))
                .clickable { steps.add(StepRow("", "", "中火")) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) { Text("＋ 手順をふやす", fontSize = 13.sp, color = Ink) }

        SectionCard {
            Column {
                Text("メモ・ポイント", fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                OutlinedTextField(
                    value = point, onValueChange = { point = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    placeholder = { Text("コツや次への気づき", fontSize = 13.sp) }
                )
            }
        }
        val canSave = name.isNotBlank() && ings.any { it.name.isNotBlank() } && steps.any { it.text.isNotBlank() }
        Button(
            onClick = {
                val id = if (editing) base!!.id else "my_" + System.currentTimeMillis()
                val recipe = Recipe(
                    id = id,
                    name = name.trim(),
                    category = category,
                    difficulty = 1,
                    baseServings = servings,
                    point = point.trim(),
                    ingredients = ings.filter { it.name.isNotBlank() }.map { row ->
                        val a = row.amount.toDoubleOrNull() ?: 0.0
                        Ingredient(
                            name = row.name.trim(),
                            amount = a,
                            unit = if (a <= 0.0) (if (row.unit.isBlank()) "適量" else row.unit.trim()) else row.unit.trim(),
                            scalable = a > 0.0,
                            round = if (a > 0.0 && a < 10) "half" else "int"
                        )
                    },
                    steps = steps.filter { it.text.isNotBlank() }.map { row ->
                        val m = row.minutes.toDoubleOrNull() ?: 0.0
                        StepItem(
                            text = row.text.trim(),
                            time = if (m > 0) (if (m == Math.floor(m)) "${m.toInt()}分" else "${m}分") else "－",
                            heat = if (row.heat.isBlank()) "－" else row.heat.trim()
                        )
                    }
                )
                Store.saveMyRecipe(recipe)
                pop()
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE07A5F))
        ) { Text("レシピとタイマーを保存する", fontSize = 15.sp) }
        Spacer(Modifier.height(16.dp))
    }
}
