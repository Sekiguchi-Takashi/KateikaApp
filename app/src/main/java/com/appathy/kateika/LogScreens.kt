package com.appathy.kateika

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun loadBitmap(path: String): Bitmap? {
    val f = File(path)
    if (!f.exists()) return null
    val opts = BitmapFactory.Options()
    opts.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, opts)
    var sample = 1
    val maxDim = 1024
    while (opts.outWidth / sample > maxDim || opts.outHeight / sample > maxDim) sample *= 2
    val opts2 = BitmapFactory.Options()
    opts2.inSampleSize = sample
    return BitmapFactory.decodeFile(path, opts2)
}

fun fmtDate(millis: Long): String =
    SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN).format(Date(millis))

@Composable
fun StarRow(stars: Int, onChange: ((Int) -> Unit)? = null) {
    Row {
        (1..5).forEach { i ->
            val mod = if (onChange != null) Modifier.clickable { onChange(i) }.padding(2.dp) else Modifier.padding(1.dp)
            Text(if (i <= stars) "★" else "☆", fontSize = if (onChange != null) 28.sp else 14.sp, color = Color(0xFFE0B25F), modifier = mod)
        }
    }
}

@Composable
fun LogListScreen(push: (Screen) -> Unit, pop: () -> Unit) {
    var refresh by remember { mutableIntStateOf(0) }
    val logs = remember(refresh) { Store.logs() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar("実習記録アルバム", Color(0xFF81B29A), pop)
        Button(
            onClick = { push(Screen.AddLog("", 2)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81B29A))
        ) { Text("＋ あたらしい記録をつける") }
        if (logs.isEmpty()) {
            SectionCard { Text("まだ記録がありません。料理をつくったら写真と感想をのこそう！", fontSize = 14.sp, color = Ink, lineHeight = 21.sp) }
        }
        logs.forEach { log ->
            SectionCard {
                Column {
                    val bmp = log.photoPath?.let { remember(it) { loadBitmap(it) } }
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = log.recipeName,
                            modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(log.recipeName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink, modifier = Modifier.weight(1f))
                        StarRow(log.stars)
                    }
                    Text("${fmtDate(log.dateMillis)}　${log.servings}人分", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f), modifier = Modifier.padding(top = 2.dp))
                    if (log.comment.isNotEmpty()) {
                        Text(log.comment, fontSize = 14.sp, lineHeight = 21.sp, color = Ink, modifier = Modifier.padding(top = 6.dp))
                    }
                    Text(
                        "この記録をけす",
                        fontSize = 12.sp, color = Color(0xFFB00020),
                        modifier = Modifier.padding(top = 8.dp).clickable {
                            Store.deleteLog(log.id)
                            refresh++
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun AddLogScreen(recipeName: String, servings: Int, pop: () -> Unit) {
    val ctx = LocalContext.current
    var name by remember { mutableStateOf(recipeName) }
    var num by remember { mutableIntStateOf(servings) }
    var stars by remember { mutableIntStateOf(3) }
    var comment by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var pendingFile by remember { mutableStateOf<File?>(null) }

    val takeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) photoPath = pendingFile?.absolutePath else pendingFile?.delete()
    }
    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val f = Store.newPhotoFile(ctx)
            ctx.contentResolver.openInputStream(uri)?.use { ins ->
                f.outputStream().use { outs -> ins.copyTo(outs) }
            }
            photoPath = f.absolutePath
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar("実習記録をつける", Color(0xFF81B29A), pop)
        SectionCard {
            Column {
                Text("料理名", fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(Modifier.height(10.dp))
                Text("つくった人数", fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                Spacer(Modifier.height(4.dp))
                Stepper(num) { num = it }
                Spacer(Modifier.height(10.dp))
                Text("できばえ", fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                StarRow(stars) { stars = it }
            }
        }
        SectionCard {
            Column {
                Text("写真", fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                Spacer(Modifier.height(6.dp))
                val bmp = photoPath?.let { remember(it) { loadBitmap(it) } }
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "photo",
                        modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Row {
                    OutlinedButton(onClick = {
                        val f = Store.newPhotoFile(ctx)
                        pendingFile = f
                        val uri = FileProvider.getUriForFile(ctx, "com.appathy.kateika.fileprovider", f)
                        takeLauncher.launch(uri)
                    }, modifier = Modifier.weight(1f)) { Text("カメラでとる") }
                    Spacer(Modifier.padding(4.dp))
                    OutlinedButton(onClick = {
                        pickLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }, modifier = Modifier.weight(1f)) { Text("アルバムから") }
                }
            }
        }
        SectionCard {
            Column {
                Text("感想", fontSize = 13.sp, color = Ink.copy(alpha = 0.7f))
                OutlinedTextField(
                    value = comment, onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("むずかしかったところ、次にためしたいこと…") }
                )
            }
        }
        Button(
            onClick = {
                if (name.isNotBlank()) {
                    Store.addLog(
                        CookLog(
                            id = System.currentTimeMillis(),
                            recipeName = name.trim(),
                            servings = num,
                            stars = stars,
                            comment = comment.trim(),
                            photoPath = photoPath,
                            dateMillis = System.currentTimeMillis()
                        )
                    )
                    pop()
                }
            },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81B29A))
        ) { Text("保存する（作成日時も記録されます）", fontSize = 15.sp) }
        Spacer(Modifier.height(16.dp))
    }
}
