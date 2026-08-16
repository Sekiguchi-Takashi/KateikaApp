package com.appathy.kateika

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class QuizQ(val q: String, val choices: List<String>, val answer: Int, val note: String)
data class Lesson(val id: String, val title: String, val body: List<String>, val quiz: List<QuizQ>)
data class Domain(
    val id: String, val name: String, val colorHex: Long, val desc: String,
    val gameId: String, val gameName: String, val lessons: List<Lesson>
)

data class Ingredient(val name: String, val amount: Double, val unit: String, val scalable: Boolean, val round: String)
data class StepItem(val text: String, val time: String, val heat: String)
data class Recipe(
    val id: String, val name: String, val category: String, val difficulty: Int,
    val baseServings: Int, val point: String,
    val ingredients: List<Ingredient>, val steps: List<StepItem>
)

data class SortItem(val name: String, val group: String)
data class LaundryQ(val symbol: String, val q: String, val choices: List<String>, val answer: Int, val note: String)
data class OrderTask(val title: String, val steps: List<String>)
data class ShopItem(val name: String, val price: Int, val group: String)
data class Scenario(val title: String, val situation: String, val choices: List<String>, val best: Int, val note: String)

object DataRepo {
    var domains: List<Domain> = emptyList(); private set
    var recipes: List<Recipe> = emptyList(); private set
    var sortItems: List<SortItem> = emptyList(); private set
    var laundry: List<LaundryQ> = emptyList(); private set
    var orders: List<OrderTask> = emptyList(); private set
    var shopItems: List<ShopItem> = emptyList(); private set
    var shopBudget: Int = 500; private set
    var shopGoal: String = ""; private set
    var scenarios: List<Scenario> = emptyList(); private set
    private var loaded = false

    fun load(ctx: Context) {
        if (loaded) return
        loaded = true
        domains = parseDomains(readAsset(ctx, "data/domains.json"))
        recipes = parseRecipes(readAsset(ctx, "data/recipes.json"))
        parseGames(readAsset(ctx, "data/games.json"))
    }

    private fun readAsset(ctx: Context, path: String): String =
        ctx.assets.open(path).bufferedReader().use { it.readText() }

    private fun strList(a: JSONArray): List<String> = (0 until a.length()).map { a.getString(it) }

    private fun parseQuiz(a: JSONArray): List<QuizQ> = (0 until a.length()).map { i ->
        val o = a.getJSONObject(i)
        QuizQ(o.getString("q"), strList(o.getJSONArray("choices")), o.getInt("answer"), o.optString("note"))
    }

    private fun parseDomains(text: String): List<Domain> {
        val arr = JSONObject(text).getJSONArray("domains")
        return (0 until arr.length()).map { i ->
            val d = arr.getJSONObject(i)
            val lessonsArr = d.getJSONArray("lessons")
            val lessons = (0 until lessonsArr.length()).map { j ->
                val l = lessonsArr.getJSONObject(j)
                Lesson(
                    l.getString("id"), l.getString("title"),
                    strList(l.getJSONArray("body")),
                    parseQuiz(l.getJSONArray("quiz"))
                )
            }
            Domain(
                d.getString("id"), d.getString("name"),
                0xFF000000L or java.lang.Long.parseLong(d.getString("color"), 16),
                d.getString("desc"), d.getString("gameId"), d.getString("gameName"), lessons
            )
        }
    }

    private fun parseRecipes(text: String): List<Recipe> {
        val arr = JSONObject(text).getJSONArray("recipes")
        return (0 until arr.length()).map { i ->
            val r = arr.getJSONObject(i)
            val ings = r.getJSONArray("ingredients")
            val steps = r.getJSONArray("steps")
            Recipe(
                r.getString("id"), r.getString("name"), r.getString("category"),
                r.getInt("difficulty"), r.getInt("baseServings"), r.optString("point"),
                (0 until ings.length()).map { j ->
                    val o = ings.getJSONObject(j)
                    Ingredient(o.getString("name"), o.getDouble("amount"), o.getString("unit"),
                        o.getBoolean("scalable"), o.getString("round"))
                },
                (0 until steps.length()).map { j ->
                    val o = steps.getJSONObject(j)
                    StepItem(o.getString("text"), o.optString("time"), o.optString("heat"))
                }
            )
        }
    }

    private fun parseGames(text: String) {
        val root = JSONObject(text)
        val s = root.getJSONArray("sortItems")
        sortItems = (0 until s.length()).map { i ->
            val o = s.getJSONObject(i); SortItem(o.getString("name"), o.getString("group"))
        }
        val l = root.getJSONArray("laundry")
        laundry = (0 until l.length()).map { i ->
            val o = l.getJSONObject(i)
            LaundryQ(o.getString("symbol"), o.getString("q"), strList(o.getJSONArray("choices")),
                o.getInt("answer"), o.optString("note"))
        }
        val od = root.getJSONArray("orders")
        orders = (0 until od.length()).map { i ->
            val o = od.getJSONObject(i); OrderTask(o.getString("title"), strList(o.getJSONArray("steps")))
        }
        val shop = root.getJSONObject("shop")
        shopBudget = shop.getInt("budget")
        shopGoal = shop.getString("goal")
        val si = shop.getJSONArray("items")
        shopItems = (0 until si.length()).map { i ->
            val o = si.getJSONObject(i); ShopItem(o.getString("name"), o.getInt("price"), o.getString("group"))
        }
        val sc = root.getJSONArray("scenarios")
        scenarios = (0 until sc.length()).map { i ->
            val o = sc.getJSONObject(i)
            Scenario(o.getString("title"), o.getString("situation"), strList(o.getJSONArray("choices")),
                o.getInt("best"), o.optString("note"))
        }
    }
}

fun formatAmount(ing: Ingredient, servings: Int, base: Int): String {
    if (ing.amount <= 0.0) return ing.unit
    val raw = if (ing.scalable) ing.amount * servings / base else ing.amount
    val v = when (ing.round) {
        "int" -> {
            val r = kotlin.math.round(raw)
            if (r < 1.0) 1.0 else r
        }
        "half" -> kotlin.math.round(raw * 2.0) / 2.0
        else -> kotlin.math.round(raw * 10.0) / 10.0
    }
    val num = if (v == kotlin.math.floor(v)) v.toLong().toString() else {
        val s = String.format("%.1f", v)
        if (s.endsWith("0")) s.dropLast(2) else s
    }
    return num + ing.unit
}
