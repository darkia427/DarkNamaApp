package com.dark.darknama.utils

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.ResolvingDataSource
import java.net.URLDecoder
import java.net.URLEncoder

/** Routes every request (HLS playlists AND segments) through a proxy template. */
@UnstableApi
class ProxyResolver(private val template: String) : ResolvingDataSource.Resolver {
    private val prefix = template.substringBefore("{url}")
    private val suffix = template.substringAfter("{url}")

    override fun resolveDataSpec(dataSpec: DataSpec): DataSpec {
        val proxied = template.replace("{url}", URLEncoder.encode(dataSpec.uri.toString(), "UTF-8"))
        return dataSpec.withUri(Uri.parse(proxied))
    }

    // Report the original address so relative playlist links resolve correctly.
    override fun resolveReportedUri(uri: Uri): Uri {
        val s = uri.toString()
        if (!s.startsWith(prefix) || !s.endsWith(suffix)) return uri
        val enc = s.removePrefix(prefix).removeSuffix(suffix)
        return try { Uri.parse(URLDecoder.decode(enc, "UTF-8")) } catch (e: Exception) { uri }
    }
}
