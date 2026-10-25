package com.dark.darknama.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dark.darknama.R
import com.dark.darknama.data.model.Episode
import com.dark.darknama.data.model.HistoryEntry
import com.dark.darknama.data.model.Movie
import com.dark.darknama.data.model.Season
import com.dark.darknama.data.model.Series
import com.dark.darknama.data.model.toFavoriteItem
import com.dark.darknama.utils.HistoryStorage
import com.dark.darknama.utils.PlaybackSession

/** Call right before launching the player for a movie: records history + resume key. */
internal fun recordMoviePlay(ctx: Context, movie: Movie) {
    val key = "movie_${movie.id}"
    PlaybackSession.contentKey = key
    HistoryStorage.addHistory(ctx, HistoryEntry(item = movie.toFavoriteItem(), contentKey = key))
}

/** Call right before launching the player for an episode. */
internal fun recordSeriesPlay(ctx: Context, series: Series, season: Season, episode: Episode) {
    val key = "series_${series.id}_${season.id}_${episode.id}"
    PlaybackSession.contentKey = key
    HistoryStorage.addHistory(
        ctx,
        HistoryEntry(
            item = series.toFavoriteItem(),
            contentKey = key,
            episodeLabel = "${season.title} · ${episode.title}"
        )
    )
}

/** "Copy all" chips for one season: one per quality + one for every link. */
@Composable
internal fun SeasonCopyBar(season: Season) {
    val ctx = LocalContext.current
    val qualities = season.episodes.flatMap { it.sources }.map { it.quality }.distinct()
    if (qualities.isEmpty()) return

    fun copy(urls: List<String>) {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("Links", urls.joinToString("\n")))
        Toast.makeText(ctx, "${urls.size} links copied", Toast.LENGTH_SHORT).show()
    }

    LazyRow(
        modifier = Modifier.padding(top = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(qualities) { q ->
            val urls = season.episodes.mapNotNull { e -> e.sources.firstOrNull { it.quality == q }?.url }
            AssistChip(
                onClick = { copy(urls) },
                label = { Text(stringResource(R.string.copy_all_quality, q, urls.size)) },
                leadingIcon = { Icon(Icons.Default.ContentCopy, null, Modifier.size(16.dp)) }
            )
        }
        item {
            AssistChip(
                onClick = { copy(season.episodes.flatMap { e -> e.sources.map { it.url } }) },
                label = { Text(stringResource(R.string.copy_all_links)) },
                leadingIcon = { Icon(Icons.Default.ContentCopy, null, Modifier.size(16.dp)) }
            )
        }
    }
}

/** Channel logo that falls back to a letter avatar when missing or failing to load. */
@Composable
internal fun ChannelLogo(name: String, logo: String) {
    if (logo.isBlank()) {
        LetterAvatar(name)
        return
    }
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(logo).crossfade(true).build(),
        contentDescription = name,
        modifier = Modifier.fillMaxSize().padding(6.dp),
        contentScale = ContentScale.Fit,
        error = { LetterAvatar(name) }
    )
}

@Composable
internal fun LetterAvatar(name: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.trim().firstOrNull()?.uppercase() ?: "?",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
