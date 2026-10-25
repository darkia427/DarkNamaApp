package com.dark.darknama.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HistoryEntry(
    val item: FavoriteItem,
    val contentKey: String,            // movie_12  /  series_5_2_33
    val episodeLabel: String? = null,
    val watchedAt: Long = System.currentTimeMillis()
)

@Serializable
data class PlaybackPosition(
    val key: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long = System.currentTimeMillis()
)

fun Movie.toFavoriteItem() = FavoriteItem(
    id = id, type = "movie", title = title, description = description, year = year,
    imdb = imdb, rating = rating, duration = duration, image = image, cover = cover,
    genres = genres, country = country, sources = sources
)

fun Series.toFavoriteItem() = FavoriteItem(
    id = id, type = "series", title = title, description = description, year = year,
    imdb = imdb, rating = rating, duration = duration, image = image, cover = cover,
    genres = genres, country = country
)
