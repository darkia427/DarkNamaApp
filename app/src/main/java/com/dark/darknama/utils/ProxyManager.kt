package com.dark.darknama.utils

import android.content.Context

/**
 * Proxy templates used for live TV when direct playback fails.
 * "{url}" is replaced with the URL-encoded stream address.
 * The defaults are public proxies - not guaranteed to be stable, edit them in Settings.
 */
object ProxyManager {
    private val defaults = listOf(
        "https://corsproxy.io/?url={url}",
        "https://api.allorigins.win/raw?url={url}",
        "https://thingproxy.freeboard.io/fetch/{url}"
    )
    private const val PREFS = "server_prefs"
    private const val KEY = "proxies"

    fun list(ctx: Context): List<String> {
        val saved = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            ?: return defaults
        return saved.split('\n').map { it.trim() }.filter { it.contains("{url}") }
    }

    fun save(ctx: Context, items: List<String>) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, items.map { it.trim() }.filter { it.contains("{url}") }.joinToString("\n")).apply()
    }

    fun reset(ctx: Context) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
}
