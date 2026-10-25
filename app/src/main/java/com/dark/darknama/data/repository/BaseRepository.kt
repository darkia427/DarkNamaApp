package com.dark.darknama.data.repository

import com.dark.darknama.utils.ServerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

open class BaseRepository {
    protected val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    protected val API_KEY = "4F5A9C3D9A86FA54EACEDDD635185"

    private val hostRegex = Regex("^https?://[^/]+")

    /**
     * Sends the request to the selected server first (Settings -> Servers),
     * then to the remaining servers as fallbacks.
     */
    protected suspend fun executeRequest(
        primaryUrl: String,
        requestBuilder: (String) -> Request
    ): String = withContext(Dispatchers.IO) {
        var last: Exception? = null
        for (base in ServerManager.orderedBases()) {
            try {
                val url = primaryUrl.replace(hostRegex, base)
                client.newCall(requestBuilder(url)).execute().use { response ->
                    if (response.isSuccessful) {
                        return@withContext response.body?.string()
                            ?: throw Exception("Empty response body")
                    }
                    throw Exception("Server returned error: ${response.code}")
                }
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: Exception("No server available")
    }
}
