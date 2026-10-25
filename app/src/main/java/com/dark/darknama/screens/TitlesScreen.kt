package com.dark.darknama.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dark.darknama.R
import com.dark.darknama.components.NetworkLogoButton
import com.dark.darknama.components.SettingsIconButton
import com.dark.darknama.ui.movies.MoviesViewModel
import com.dark.darknama.ui.series.SeriesViewModel

/** One screen for Movies and Series with a switcher between them. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitlesScreen(
    moviesViewModel: MoviesViewModel,
    seriesViewModel: SeriesViewModel,
    navController: NavController
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.titles),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                NetworkLogoButton()
                Spacer(modifier = Modifier.width(8.dp))
                SettingsIconButton(navController = navController)
            }
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            SegmentedButton(
                selected = tab == 0,
                onClick = { tab = 0 },
                shape = SegmentedButtonDefaults.itemShape(0, 2)
            ) { Text(stringResource(R.string.movies)) }
            SegmentedButton(
                selected = tab == 1,
                onClick = { tab = 1 },
                shape = SegmentedButtonDefaults.itemShape(1, 2)
            ) { Text(stringResource(R.string.series)) }
        }

        if (tab == 0) {
            MoviesScreen(viewModel = moviesViewModel, navController = navController, showHeader = false)
        } else {
            SeriesScreen(viewModel = seriesViewModel, navController = navController, showHeader = false)
        }
    }
}
