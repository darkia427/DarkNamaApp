package com.dark.darknama.utils

import android.content.Context
import android.util.Log
import com.dark.darknama.data.model.HistoryEntry
import com.dark.darknama.data.model.PlaybackPosition
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/** Holds the content key of the video that is about to be played (read by the player). */
object PlaybackSession {
    @Volatile var contentKey: String? = null

    /** Returns the pending key and clears it. */
    fun take(): String? {
        val k = contentKey
        contentKey = null
        return k
    }
}

/** Watch history + resume positions, stored as JSON in filesDir. */
object HistoryStorage {
    private const val TAG = "HistoryStorage"
    private val lock = Any()
    private val json = Json { ignoreUnknownKeys = true }

    private fun historyFile(c: Context) = File(c.filesDir, "history.json")
    private fun positionsFile(c: Context) = File(c.filesDir, "positions.json")

    // ---------------- history ----------------
    fun loadHistory(context: Context): List<HistoryEntry> = synchronized(lock) { readHistory(context) }

    private fun readHistory(context: Context): List<HistoryEntry> = try {
        val f = historyFile(context)
        if (f.exists()) json.decodeFromString<List<HistoryEntry>>(f.readText()) else emptyList()
    } catch (e: Exception) {
        Log.e(TAG, "Error loading history", e); emptyList()
    }

    private fun writeHistory(context: Context, list: List<HistoryEntry>) {
        try { historyFile(context).writeText(json.encodeToString(list)) }
        catch (e: Exception) { Log.e(TAG, "Error saving history", e) }
    }

    fun addHistory(context: Context, entry: HistoryEntry) = synchronized(lock) {
        val list = readHistory(context).toMutableList()
        list.removeAll { it.item.id == entry.item.id && it.item.type == entry.item.type }
        list.add(0, entry)
        writeHistory(context, list.take(200))
    }

    fun removeHistory(context: Context, id: Int, type: String) = synchronized(lock) {
        writeHistory(context, readHistory(context).filterNot { it.item.id == id && it.item.type == type })
    }

    fun clearHistory(context: Context) = synchronized(lock) {
        historyFile(context).delete(); Unit
    }

    // ---------------- resume positions ----------------
    private fun readPositions(context: Context): MutableMap<String, PlaybackPosition> = try {
        val f = positionsFile(context)
        if (f.exists()) json.decodeFromString<List<PlaybackPosition>>(f.readText())
            .associateBy { it.key }.toMutableMap()
        else mutableMapOf()
    } catch (e: Exception) { mutableMapOf() }

    fun savePosition(context: Context, key: String, positionMs: Long, durationMs: Long) = synchronized(lock) {
        try {
            val map = readPositions(context)
            if (durationMs > 0 && positionMs > durationMs * 0.95) map.remove(key)   // practically finished
            else if (positionMs > 5_000) map[key] = PlaybackPosition(key, positionMs, durationMs)
            val trimmed = map.values.sortedByDescending { it.updatedAt }.take(500)
            positionsFile(context).writeText(json.encodeToString(trimmed))
        } catch (e: Exception) { Log.e(TAG, "Error saving position", e) }
    }

    fun getPositionInfo(context: Context, key: String): PlaybackPosition? =
        synchronized(lock) { readPositions(context)[key] }

    fun getPosition(context: Context, key: String): Long = getPositionInfo(context, key)?.positionMs ?: 0L
}
