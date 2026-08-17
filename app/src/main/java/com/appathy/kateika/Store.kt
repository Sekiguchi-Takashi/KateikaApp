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

    fun selectedChar(): String = sp.getString("char", "akari") ?: "akari"
    fun setSelectedChar(id: String) = sp.edit().putString("char", id).apply()

    fun seenChars(): Set<String> = sp.getStringSet("seenChars", emptySet()) ?: emptySet()
    fun markSeen(ids: List<String>) {
        val next = HashSet(seenChars())
        next.addAll(ids)
        sp.edit().putStringSet("seenChars", next).apply()
    }

    fun myRecipes(): List<Recipe> {
        val arr = JSONArray(sp.getString("myRecipes", "[]"))
        return (0 until arr.length()).map { i -> recipeFromJson(arr.getJSONObject(i)) }
    }

    fun saveMyRecipe(r: Recipe) {
        val arr = JSONArray(sp.getString("myRecipes", "[]"))
        val out = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("id") != r.id) out.put(o)
        }
        out.put(recipeToJson(r))
        sp.edit().putString("myRecipes", out.toString()).apply()
    }

    fun deleteMyRecipe(id: String) {
        val arr = JSONArray(sp.getString("myRecipes", "[]"))
        val out = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("id") != id) out.put(o)
        }
        sp.edit().putString("myRecipes", out.toString()).apply()
    }

    private fun recipeToJson(r: Recipe): JSONObject {
        val o = JSONObject()
        o.put("id", r.id)
        o.put("name", r.name)
        o.put("category", r.category)
        o.put("difficulty", r.difficulty)
        o.put("baseServings", r.baseServings)
        o.put("point", r.point)
        val ings = JSONArray()
        r.ingredients.forEach { ing ->
            val io = JSONObject()
            io.put("name", ing.name)
            io.put("amount", ing.amount)
            io.put("unit", ing.unit)
            io.put("scalable", ing.scalable)
            io.put("round", ing.round)
            ings.put(io)
        }
        o.put("ingredients", ings)
        val steps = JSONArray()
        r.steps.forEach { s ->
            val so = JSONObject()
            so.put("text", s.text)
            so.put("time", s.time)
            so.put("heat", s.heat)
            steps.put(so)
        }
        o.put("steps", steps)
        return o
    }

    private fun recipeFromJson(o: JSONObject): Recipe {
        val ings = o.getJSONArray("ingredients")
        val steps = o.getJSONArray("steps")
        return Recipe(
            o.getString("id"), o.getString("name"), o.getString("category"),
            o.getInt("difficulty"), o.getInt("baseServings"), o.optString("point"),
            (0 until ings.length()).map { i ->
                val io = ings.getJSONObject(i)
                Ingredient(
                    io.getString("name"), io.getDouble("amount"), io.getString("unit"),
                    io.getBoolean("scalable"), io.getString("round")
                )
            },
            (0 until steps.length()).map { i ->
                val so = steps.getJSONObject(i)
                StepItem(so.getString("text"), so.optString("time"), so.optString("heat"))
            }
        )
    }

    fun newPhotoFile(ctx: Context): File {
        val dir = File(ctx.filesDir, "photos")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "IMG_" + System.currentTimeMillis() + ".jpg")
    }
}
