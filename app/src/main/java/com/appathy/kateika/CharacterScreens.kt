package com.appathy.kateika

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CharImage(path: String, modifier: Modifier = Modifier, silhouette: Boolean = false) {
    val ctx = LocalContext.current
    val bmp = remember(path) { CharRepo.image(ctx, path) }
    if (bmp != null) {
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit,
            colorFilter = if (silhouette) ColorFilter.tint(Color(0xFFBBB6AE)) else null
        )
    }
}

@Composable
fun SpeechBubble(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Text(text, fontSize = 13.sp, lineHeight = 19.sp, color = Ink)
    }
}

@Composable
fun CharacterListScreen(push: (Screen) -> Unit, pop: () -> Unit) {
    val cur = CharRepo.current()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar("登録キャラクター", Color(0xFFB98CD9), pop)
        val unlocked = CharRepo.unlockedList().size
        SectionCard(bg = Color(0xFFF2E9FA)) {
            Column {
                Text(
                    "登録 $unlocked / ${CharRepo.characters.size} 人",
                    fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink
                )
                Text(
                    "学習を進めたり実習を記録すると、新しい仲間が家庭科室にやってきます。",
                    fontSize = 12.sp, lineHeight = 18.sp, color = Ink.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        CharRepo.characters.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
                pair.forEach { c ->
                    val ok = CharRepo.isUnlocked(c)
                    Box(Modifier.weight(1f)) {
                        SectionCard(
                            bg = if (c.id == cur.id) Color(0xFFF2E9FA) else Color.White,
                            onClick = { if (ok) push(Screen.CharDetail(c)) }
                        ) {
                            Column(
                                Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(Modifier.height(120.dp), contentAlignment = Alignment.Center) {
                                    CharImage(c.file, Modifier.height(118.dp), silhouette = !ok)
                                }
                                Text(
                                    if (ok) c.name else "？？？",
                                    fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    if (ok) c.role else c.unlockText,
                                    fontSize = 11.sp, lineHeight = 16.sp,
                                    color = Ink.copy(alpha = 0.65f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                if (c.id == cur.id) {
                                    Text(
                                        "ホームに表示中",
                                        fontSize = 11.sp, color = Color(0xFFB98CD9),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun CharDetailScreen(c: Character, pop: () -> Unit) {
    val ctx = LocalContext.current
    val room = remember { CharRepo.image(ctx, CharRepo.roomImage) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(c.name, Color(0xFFB98CD9), pop)
        Box(
            Modifier.fillMaxWidth().height(320.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (room != null) {
                Image(
                    bitmap = room.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            CharImage(c.file, Modifier.height(300.dp))
        }
        SectionCard {
            Column {
                Text(c.role, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink)
                Text(
                    "「" + c.greet + "」",
                    fontSize = 14.sp, lineHeight = 21.sp, color = Ink,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        SectionCard(bg = Color(0xFFFFF3D6)) {
            Text("💡 " + c.tip, fontSize = 13.sp, lineHeight = 20.sp, color = Ink)
        }
        val isCur = CharRepo.current().id == c.id
        Button(
            onClick = { Store.setSelectedChar(c.id); pop() },
            enabled = !isCur,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB98CD9))
        ) { Text(if (isCur) "ホームに表示中" else "この子をホームに出す") }
        Spacer(Modifier.height(16.dp))
    }
}
