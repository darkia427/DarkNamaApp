package com.dark.darknama.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Three API servers. The selected one is tried first, the others are used as fallbacks.
 * Each can be overridden by a custom address; default addresses are never shown in the UI.
 */
object ServerManager {
    private val defaults = listOf(
        "https://server-hi-speed-iran.info",
        "http://server-majazi-iran.info",
        "http://server-win-iran.info"
    )
    private var prefs: SharedPreferences? = null
    @Volatile private var ordered: List<String> = defaults

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("server_prefs", Context.MODE_PRIVATE)
        refresh()
    }

    fun count(): Int = defaults.size

    fun selected(): Int = (prefs?.getInt("selected", 0) ?: 0).coerceIn(0, defaults.lastIndex)

    fun select(index: Int) {
        prefs?.edit()?.putInt("selected", index)?.apply()
        refresh()
    }

    fun hasCustom(index: Int): Boolean = !prefs?.getString("custom_$index", null).isNullOrBlank()

    /** null / blank = back to default */
    fun setCustom(index: Int, url: String?) {
        var v = url?.trim()?.trimEnd('/')
        if (!v.isNullOrBlank() && !v.startsWith("http://") && !v.startsWith("https://")) v = "http://$v"
        prefs?.edit()?.apply {
            if (v.isNullOrBlank()) remove("custom_$index") else putString("custom_$index", v)
        }?.apply()
        refresh()
    }

    private fun base(index: Int): String =
        prefs?.getString("custom_$index", null)?.takeIf { it.isNotBlank() } ?: defaults[index]

    private fun refresh() {
        val s = selected()
        ordered = listOf(base(s)) + defaults.indices.filter { it != s }.map { base(it) }
    }

    /** Selected server first, then the rest as fallbacks. */
    fun orderedBases(): List<String> = ordered
}
