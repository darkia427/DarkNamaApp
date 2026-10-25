package com.dark.darknama.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dark.darknama.R
import com.dark.darknama.components.SettingsOptionRow
import com.dark.darknama.components.SettingsSectionTitle
import com.dark.darknama.components.SettingsSubPage
import com.dark.darknama.utils.ProxyManager
import com.dark.darknama.utils.ServerManager

/**
 * Server selection (names only - addresses are never shown) and
 * the list of proxies used for live TV when direct playback fails.
 */
@Composable
fun ServerSettingsScreen(navController: NavController?) {
    val context = LocalContext.current
    var selected by remember { mutableIntStateOf(ServerManager.selected()) }
    var editing by remember { mutableStateOf<Int?>(null) }
    var version by remember { mutableIntStateOf(0) }       // bumped when a custom address changes
    var proxyText by remember { mutableStateOf(ProxyManager.list(context).joinToString("\n")) }

    SettingsSubPage(
        title = stringResource(R.string.server_settings),
        navController = navController,
        icon = Icons.Default.Dns
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            SettingsSectionTitle(stringResource(R.string.server_settings))

            for (i in 0 until ServerManager.count()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        SettingsOptionRow(
                            label = stringResource(R.string.server_n, i + 1),
                            description = if (version >= 0 && ServerManager.hasCustom(i))
                                stringResource(R.string.server_custom) else null,
                            isSelected = selected == i,
                            onSelect = {
                                selected = i
                                ServerManager.select(i)
                            }
                        )
                    }
                    IconButton(onClick = { editing = i }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.server_custom))
                    }
                }
            }

            Text(
                text = stringResource(R.string.server_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ---------- Live TV proxies ----------
            SettingsSectionTitle(stringResource(R.string.proxy_settings))
            OutlinedTextField(
                value = proxyText,
                onValueChange = { proxyText = it },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                label = { Text(stringResource(R.string.proxy_hint)) }
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    ProxyManager.save(context, proxyText.split('\n'))
                    proxyText = ProxyManager.list(context).joinToString("\n")
                }) { Text(stringResource(R.string.save)) }
                TextButton(onClick = {
                    ProxyManager.reset(context)
                    proxyText = ProxyManager.list(context).joinToString("\n")
                }) { Text(stringResource(R.string.server_reset)) }
            }
            Text(
                text = stringResource(R.string.proxy_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    editing?.let { index ->
        // The current address is intentionally not pre-filled / shown
        var text by remember(index) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(stringResource(R.string.server_n, index + 1)) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.server_custom_hint)) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    ServerManager.setCustom(index, text)
                    version++
                    editing = null
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    ServerManager.setCustom(index, null)
                    version++
                    editing = null
                }) { Text(stringResource(R.string.server_reset)) }
            }
        )
    }
}
