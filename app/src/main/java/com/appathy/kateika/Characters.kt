package com.appathy.kateika

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONObject

data class Character(
    val id: String,
    val name: String,
    val file: String,
    val role: String,
    val domain: String,
    val unlockType: String,
    val unlockValue: String,
    val unlockText: String,
    val greet: String,
    val tip: String,
    val praise: String,
    val cheer: String
)

object CharRepo {
    var characters: List<Character> = emptyList(); private set
    var roomImage: String = "img/room_kateika.jpg"; private set
    private var loaded = false
    private val cache = HashMap<String, Bitmap?>()

    fun load(ctx: Context) {
        if (loaded) return
        loaded = true
        val text = ctx.assets.open("data/characters.json").bufferedReader().use { it.readText() }
        val root = JSONObject(text)
        roomImage = root.optString("room", roomImage)
        val arr = root.getJSONArray("characters")
        characters = (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Character(
                o.getString("id"), o.getString("name"), o.getString("file"),
                o.getString("role"), o.optString("domain"),
                o.getString("unlockType"), o.optString("unlockValue"), o.getString("unlockText"),
                o.getString("greet"), o.getString("tip"), o.getString("praise"), o.getString("cheer")
            )
        }
    }

    fun image(ctx: Context, path: String): Bitmap? {
        if (cache.containsKey(path)) return cache[path]
        val bmp = try {
            ctx.assets.open(path).use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            null
        }
        cache[path] = bmp
        return bmp
    }

    fun byId(id: String): Character? = characters.firstOrNull { it.id == id }

    fun forDomain(domainId: String): Character? =
        characters.firstOrNull { it.domain == domainId }

    fun isUnlocked(c: Character): Boolean = when (c.unlockType) {
        "always" -> true
        "domain_read" -> {
            val d = DataRepo.domains.firstOrNull { it.id == c.unlockValue }
            d != null && d.lessons.all { Store.isRead(it.id) }
        }
        "all_read" -> DataRepo.domains.all { d -> d.lessons.all { Store.isRead(it.id) } }
        "game_count" -> {
            val n = c.unlockValue.toIntOrNull() ?: 1
            DataRepo.domains.count { Store.gameBest(it.gameId) >= 0 } >= n
        }
        "log_count" -> {
            val n = c.unlockValue.toIntOrNull() ?: 1
            Store.logs().size >= n
        }
        else -> false
    }

    fun unlockedList(): List<Character> = characters.filter { isUnlocked(it) }

    fun newlyUnlocked(): List<Character> {
        val seen = Store.seenChars()
        return unlockedList().filter { !seen.contains(it.id) }
    }

    fun current(): Character {
        val id = Store.selectedChar()
        val c = byId(id)
        if (c != null && isUnlocked(c)) return c
        return characters.first()
    }
}
