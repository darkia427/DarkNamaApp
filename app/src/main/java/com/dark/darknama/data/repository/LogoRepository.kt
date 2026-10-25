package com.dark.darknama.data.repository

import com.dark.darknama.data.model.TvChannel
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

/** Fills missing channel logos from the iptv-org logo database. */
object LogoRepository {
    private const val LOGOS_URL = "https://iptv-org.github.io/api/logos.json"
    @Volatile private var map: Map<String, String>? = null

    /** Blocking - call from an IO dispatcher. */
    fun fill(client: OkHttpClient, channels: List<TvChannel>): List<TvChannel> {
        if (channels.none { it.logo.isEmpty() }) return channels
        val logos = load(client)
        if (logos.isEmpty()) return channels
        return channels.map { c ->
            if (c.logo.isNotEmpty()) c
            else c.copy(logo = logos[c.id.substringBefore('@')] ?: "")   // BBCOne.uk@SD -> BBCOne.uk
        }
    }

    private fun load(client: OkHttpClient): Map<String, String> {
        map?.let { return it }
        return try {
            val body = client.newCall(Request.Builder().url(LOGOS_URL).build()).execute()
                .use { it.body?.string().orEmpty() }
            val arr = JSONArray(body)
            val m = HashMap<String, String>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val ch = o.optString("channel")
                val url = o.optString("url")
                if (ch.isNotEmpty() && url.isNotEmpty()) m.putIfAbsent(ch, url)
            }
            m.also { map = it }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
