package com.gamelaunch.frontend.ui.screen.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelaunch.frontend.ui.theme.LayoutMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEmulatorConfigClick: () -> Unit,
    onScrapeAllClick: () -> Unit,
    onRescanClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { viewModel.setRomRootPath(it.path ?: it.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ROM Library
            Text("ROM Library", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.romRootPath.ifEmpty { "Not configured" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("ROM Folder") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { folderPicker.launch(null) }) {
                    Icon(Icons.Default.FolderOpen, contentDescription = "Pick folder")
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRescanClick, modifier = Modifier.weight(1f)) { Text("Rescan ROMs") }
                Button(onClick = onScrapeAllClick, modifier = Modifier.weight(1f)) { Text("Scrape All") }
            }

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // Layout
            Text("Display", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            SettingsSwitchRow(
                label = "Carousel layout",
                checked = state.layoutMode == LayoutMode.CAROUSEL,
                onCheckedChange = { viewModel.setLayoutMode(if (it) LayoutMode.CAROUSEL else LayoutMode.GRID) }
            )

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // ScreenScraper
            Text("ScreenScraper", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Sign up at screenscraper.fr to get credentials",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.ssId,
                onValueChange = viewModel::updateSsId,
                label = { Text("Username (ssid)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.ssPassword,
                onValueChange = viewModel::updateSsPassword,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { viewModel.saveCredentials() }, modifier = Modifier.weight(1f)) {
                    Text("Save")
                }
                FilledTonalButton(
                    onClick = { viewModel.validateCredentials() },
                    modifier = Modifier.weight(1f),
                    enabled = !state.credentialValidating
                ) {
                    if (state.credentialValidating) {
                        CircularProgressIndicator(modifier = Modifier.width(18.dp).height(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Validate")
                    }
                }
            }
            state.credentialValid?.let { valid ->
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (valid) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (valid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (valid) "Credentials valid" else "Invalid credentials",
                        color = if (valid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Scrape options", style = MaterialTheme.typography.labelLarge)
            SettingsSwitchRow("Box Art", state.scrapeBoxArt, viewModel::setScrapeBoxArt)
            SettingsSwitchRow("Screenshots", state.scrapeScreenshots, viewModel::setScrapeScreenshots)
            SettingsSwitchRow("Wheel Logos", state.scrapeWheelLogos, viewModel::setScrapeWheelLogos)
            SettingsSwitchRow("Video Previews", state.scrapeVideos, viewModel::setScrapeVideos)

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // Emulators
            Text("Emulators", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onEmulatorConfigClick, modifier = Modifier.fillMaxWidth()) {
                Text("Configure Emulators")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
