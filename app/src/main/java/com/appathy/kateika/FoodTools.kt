package com.appathy.kateika

import android.content.Context
import org.json.JSONObject

data class CutType(val id: String, val name: String, val shape: String, val desc: String, val use: String)
data class PriceItem(val name: String, val unit: String, val price: Int, val ratio: Int, val hint: String)
data class Dish(val name: String, val need: List<String>, val plus: List<String>, val how: String)
data class SeasonInfo(
    val id: String, val name: String, val months: String, val colorHex: Long,
    val vegetables: List<String>, val fruits: List<String>, val note: String
)

object FoodTools {
    var cuts: List<CutType> = emptyList(); private set
    var surveyDate: String = ""; private set
    var priceSource: String = ""; private set
    var priceNote: String = ""; private set
    var vegPrices: List<PriceItem> = emptyList(); private set
    var meatPrices: List<PriceItem> = emptyList(); private set
    var topics: List<String> = emptyList(); private set
    var fridgeItems: List<String> = emptyList(); private set
    var dishes: List<Dish> = emptyList(); private set
    var seasons: List<SeasonInfo> = emptyList(); private set
    var seasonNote: String = ""; private set
    private var loaded = false

    private fun strs(o: JSONObject, key: String): List<String> {
        val a = o.getJSONArray(key)
        return (0 until a.length()).map { a.getString(it) }
    }

    fun load(ctx: Context) {
        if (loaded) return
        loaded = true
        val text = ctx.assets.open("data/food_tools.json").bufferedReader().use { it.readText() }
        val root = JSONObject(text)

        val c = root.getJSONArray("cuts")
        cuts = (0 until c.length()).map { i ->
            val o = c.getJSONObject(i)
            CutType(o.getString("id"), o.getString("name"), o.getString("shape"), o.getString("desc"), o.getString("use"))
        }

        val p = root.getJSONObject("prices")
        surveyDate = p.getString("surveyDate")
        priceSource = p.getString("source")
        priceNote = p.getString("note")
        val pi = p.getJSONArray("items")
        vegPrices = (0 until pi.length()).map { i ->
            val o = pi.getJSONObject(i)
            PriceItem(o.getString("name"), o.getString("unit"), o.getInt("price"), o.getInt("ratio"), o.getString("hint"))
        }
        val mi = p.getJSONArray("meats")
        meatPrices = (0 until mi.length()).map { i ->
            val o = mi.getJSONObject(i)
            PriceItem(o.getString("name"), o.getString("unit"), o.getInt("price"), o.getInt("ratio"), o.getString("hint"))
        }
        topics = strs(p, "topics")

        val f = root.getJSONObject("fridge")
        fridgeItems = strs(f, "items")
        val da = f.getJSONArray("dishes")
        dishes = (0 until da.length()).map { i ->
            val o = da.getJSONObject(i)
            Dish(o.getString("name"), strs(o, "need"), strs(o, "plus"), o.getString("how"))
        }

        val sa = root.getJSONArray("seasons")
        seasons = (0 until sa.length()).map { i ->
            val o = sa.getJSONObject(i)
            SeasonInfo(
                o.getString("id"), o.getString("name"), o.getString("months"),
                0xFF000000L or java.lang.Long.parseLong(o.getString("color"), 16),
                strs(o, "vegetables"), strs(o, "fruits"), o.getString("note")
            )
        }
        seasonNote = root.getString("seasonNote")
    }

    fun suggest(selected: List<String>): List<Pair<Dish, Int>> {
        if (selected.isEmpty()) return emptyList()
        val list = dishes.mapNotNull { d ->
            if (!d.need.all { selected.contains(it) }) null
            else d to (d.need.size * 2 + d.plus.count { selected.contains(it) })
        }
        return list.sortedByDescending { it.second }
    }

    fun currentSeasonId(month: Int): String = when (month) {
        3, 4, 5 -> "spring"
        6, 7, 8 -> "summer"
        9, 10, 11 -> "autumn"
        else -> "winter"
    }
}
