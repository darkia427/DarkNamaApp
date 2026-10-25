package com.dark.darknama.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dark.darknama.R
import com.dark.darknama.utils.HistoryStorage
import com.dark.darknama.utils.StorageUtils

/** Everything the user has watched (newest first) with a progress bar for resumable items. */
@Composable
fun HistoryContent(navController: NavController) {
    val context = LocalContext.current
    var history by remember { mutableStateOf(HistoryStorage.loadHistory(context)) }

    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.no_history_yet),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = {
                HistoryStorage.clearHistory(context)
                history = emptyList()
            }) { Text(stringResource(R.string.clear_history)) }
        }
        items(history, key = { it.item.type + it.item.id }) { entry ->
            val info = HistoryStorage.getPositionInfo(context, entry.contentKey)
            val progress = if (info != null && info.durationMs > 0)
                (info.positionMs.toFloat() / info.durationMs).coerceIn(0f, 1f) else 0f

            Card(
                onClick = {
                    // Make the item available to the details screens, then open it
                    StorageUtils.saveFavoriteToDatabase(context, entry.item)
                    navController.navigate(
                        if (entry.item.type == "movie") "single_movie/${entry.item.id}"
                        else "single_series/${entry.item.id}"
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = entry.item.image,
                        contentDescription = entry.item.title,
                        modifier = Modifier
                            .width(60.dp)
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(
                            text = entry.item.title,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = entry.episodeLabel ?: entry.item.year.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (progress > 0f) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            )
                        }
                    }
                    IconButton(onClick = {
                        HistoryStorage.removeHistory(context, entry.item.id, entry.item.type)
                        history = HistoryStorage.loadHistory(context)
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove")
                    }
                }
            }
        }
    }
}
