package com.dark.darknama.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dark.darknama.R
import com.dark.darknama.components.SettingsIconButton
import com.dark.darknama.navigation.AppScreens
import com.dark.darknama.ui.home.HomeItem
import com.dark.darknama.ui.home.HomeViewModel
import com.dark.darknama.utils.StorageUtils
import kotlinx.coroutines.delay
import java.util.Locale

/** "Latest": slider banner of the newest movies/series + category rows below it. */
@Composable
fun LatestScreen(viewModel: HomeViewModel, navController: NavController) {
    val context = LocalContext.current

    fun openItem(item: HomeItem) {
        if (item.isMovie && item.movie != null) {
            StorageUtils.saveMovieToFile(context, item.movie)
            navController.navigate("single_movie/${item.id}")
        } else if (item.series != null) {
            StorageUtils.saveSeriesToFile(context, item.series)
            navController.navigate("single_series/${item.id}")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.latest),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                // Favorites + History live on their own screen now
                IconButton(onClick = { navController.navigate(AppScreens.Favorites.route) }) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = stringResource(R.string.favorites),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SettingsIconButton(navController = navController)
            }
        }

        if (viewModel.isLoading && viewModel.slides.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
        }

        if (viewModel.slides.isNotEmpty()) {
            item { SlideBanner(slides = viewModel.slides, onClick = ::openItem) }
        }

        items(viewModel.rows) { row ->
            Text(
                text = row.titleRes?.let { stringResource(it) } ?: row.title.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(row.items, key = { (if (it.isMovie) "m" else "s") + it.id }) { item ->
                    PosterCard(item = item, onClick = { openItem(item) })
                }
            }
        }
    }
}

@Composable
private fun SlideBanner(slides: List<HomeItem>, onClick: (HomeItem) -> Unit) {
    val pager = rememberPagerState { slides.size }

    // Auto-advance every 4 seconds
    LaunchedEffect(slides.size) {
        while (slides.size > 1) {
            delay(4000)
            pager.animateScrollToPage((pager.currentPage + 1) % slides.size)
        }
    }

    Column {
        HorizontalPager(
            state = pager,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val s = slides[page]
            Card(
                onClick = { onClick(s) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(200.dp)
            ) {
                Box {
                    AsyncImage(
                        model = s.cover,
                        contentDescription = s.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                    )
                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                        Text(
                            text = s.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${if (s.isMovie) "Movie" else "Series"} • ${s.year} • ★ " +
                                "%.1f".format(Locale.US, s.imdb),
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(slides.size) { i ->
                val selected = i == pager.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (selected) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else Color.Gray.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}

@Composable
private fun PosterCard(item: HomeItem, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.width(120.dp)) {
        Column {
            AsyncImage(
                model = item.image,
                contentDescription = item.title,
                modifier = Modifier.fillMaxWidth().height(170.dp),
                contentScale = ContentScale.Crop
            )
            Text(
                text = item.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
