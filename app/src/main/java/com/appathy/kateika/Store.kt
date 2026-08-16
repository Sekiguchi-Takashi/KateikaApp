package com.appathy.kateika

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class CookLog(
    val id: Long,
    val recipeName: String,
    val servings: Int,
    val stars: Int,
    val comment: String,
    val photoPath: String?,
    val dateMillis: Long
)

object Store {
    private lateinit var sp: SharedPreferences

    fun init(ctx: Context) {
        if (!::sp.isInitialized) sp = ctx.getSharedPreferences("kateika", Context.MODE_PRIVATE)
    }

    fun isRead(lessonId: String) = sp.getBoolean("read_$lessonId", false)
    fun setRead(lessonId: String) = sp.edit().putBoolean("read_$lessonId", true).apply()

    fun quizBest(lessonId: String) = sp.getInt("quiz_$lessonId", -1)
    fun setQuizBest(lessonId: String, score: Int) {
        if (score > quizBest(lessonId)) sp.edit().putInt("quiz_$lessonId", score).apply()
    }

    fun gameBest(gameId: String) = sp.getInt("game_$gameId", -1)
    fun setGameBest(gameId: String, score: Int) {
        if (score > gameBest(gameId)) sp.edit().putInt("game_$gameId", score).apply()
    }

    fun logs(): List<CookLog> {
        val arr = JSONArray(sp.getString("logs", "[]"))
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            CookLog(
                o.getLong("id"), o.getString("recipe"), o.getInt("servings"),
                o.getInt("stars"), o.getString("comment"),
                if (o.has("photo")) o.getString("photo") else null,
                o.getLong("date")
            )
        }.sortedByDescending { it.dateMillis }
    }

    fun addLog(log: CookLog) {
        val arr = JSONArray(sp.getString("logs", "[]"))
        val o = JSONObject()
        o.put("id", log.id)
        o.put("recipe", log.recipeName)
        o.put("servings", log.servings)
        o.put("stars", log.stars)
        o.put("comment", log.comment)
        if (log.photoPath != null) o.put("photo", log.photoPath)
        o.put("date", log.dateMillis)
        arr.put(o)
        sp.edit().putString("logs", arr.toString()).apply()
    }

    fun deleteLog(id: Long) {
        val arr = JSONArray(sp.getString("logs", "[]"))
        val out = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getLong("id") == id) {
                if (o.has("photo")) File(o.getString("photo")).delete()
            } else out.put(o)
        }
        sp.edit().putString("logs", out.toString()).apply()
    }

    fun newPhotoFile(ctx: Context): File {
        val dir = File(ctx.filesDir, "photos")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "IMG_" + System.currentTimeMillis() + ".jpg")
    }
}
