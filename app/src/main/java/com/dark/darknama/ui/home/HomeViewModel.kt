package com.dark.darknama.ui.home

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.darknama.R
import com.dark.darknama.data.model.FilterType
import com.dark.darknama.data.model.Movie
import com.dark.darknama.data.model.Series
import com.dark.darknama.data.repository.GenreRepository
import com.dark.darknama.data.repository.MovieRepository
import com.dark.darknama.data.repository.SeriesRepository
import com.dark.darknama.utils.LanguageUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class HomeItem(
    val id: Int,
    val isMovie: Boolean,
    val title: String,
    val image: String,
    val cover: String,
    val year: Int,
    val imdb: Double,
    val movie: Movie? = null,
    val series: Series? = null
)

data class HomeRow(
    @StringRes val titleRes: Int? = null,
    val title: String? = null,
    val items: List<HomeItem>
)

/** Data for the "Latest" screen: a slider of the newest titles + several category rows. */
class HomeViewModel : ViewModel() {
    private val movies = MovieRepository()
    private val series = SeriesRepository()
    private val genreRepo = GenreRepository()

    var slides by mutableStateOf<List<HomeItem>>(emptyList())
        private set

    var rows by mutableStateOf<List<HomeRow>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    init { load() }

    private fun Movie.asItem() = HomeItem(id, true, title, image, cover, year, imdb, movie = this)
    private fun Series.asItem() = HomeItem(id, false, title, image, cover, year, imdb, series = this)

    fun load() {
        viewModelScope.launch {
            isLoading = true
            val latestM = async { runCatching { movies.getMovies(0, 0, FilterType.DEFAULT) }.getOrDefault(emptyList()) }
            val latestS = async { runCatching { series.getSeries(0, 0, FilterType.DEFAULT) }.getOrDefault(emptyList()) }
            val topM = async { runCatching { movies.getMovies(0, 0, FilterType.BY_IMDB) }.getOrDefault(emptyList()) }
            val topS = async { runCatching { series.getSeries(0, 0, FilterType.BY_IMDB) }.getOrDefault(emptyList()) }
            val genres = runCatching { genreRepo.getGenres() }.getOrDefault(emptyList())
            val picked = listOf("Action", "Comedy", "Drama")
                .mapNotNull { n -> genres.find { it.title.equals(n, ignoreCase = true) } }
                .ifEmpty { genres.take(3) }
            val genreRows = picked.map { g ->
                g to async {
                    runCatching { movies.getMovies(0, g.id, FilterType.DEFAULT) }.getOrDefault(emptyList())
                }
            }

            fun List<Movie>.m() = filter { LanguageUtils.shouldDisplayTitle(it.title) }.map { it.asItem() }
            fun List<Series>.s() = filter { LanguageUtils.shouldDisplayTitle(it.title) }.map { it.asItem() }

            val lm = latestM.await().m()
            val ls = latestS.await().s()

            // Slider: newest movies and series, alternating
            val sm = lm.filter { it.cover.isNotBlank() }.take(5)
            val ss = ls.filter { it.cover.isNotBlank() }.take(5)
            slides = buildList {
                for (i in 0 until maxOf(sm.size, ss.size)) {
                    sm.getOrNull(i)?.let { add(it) }
                    ss.getOrNull(i)?.let { add(it) }
                }
            }

            rows = buildList {
                add(HomeRow(titleRes = R.string.row_latest_movies, items = lm))
                add(HomeRow(titleRes = R.string.row_latest_series, items = ls))
                add(HomeRow(titleRes = R.string.row_top_movies, items = topM.await().m()))
                add(HomeRow(titleRes = R.string.row_top_series, items = topS.await().s()))
                genreRows.forEach { (g, d) -> add(HomeRow(title = g.title, items = d.await().m())) }
            }.filter { it.items.isNotEmpty() }

            isLoading = false
        }
    }
}
