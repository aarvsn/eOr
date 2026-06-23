package com.gamelaunch.frontend.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelaunch.frontend.ui.component.PlatformTabRow
import com.gamelaunch.frontend.ui.theme.LayoutMode
import com.gamelaunch.frontend.ui.theme.carousel.CarouselHomeContent
import com.gamelaunch.frontend.ui.theme.grid.GridHomeContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGameClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Launcher") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = {
                        val next = if (state.layoutMode == LayoutMode.CAROUSEL) LayoutMode.GRID else LayoutMode.CAROUSEL
                        // handled via settings but quick toggle here
                    }) {
                        Icon(
                            if (state.layoutMode == LayoutMode.CAROUSEL) Icons.Default.GridView else Icons.Default.ViewCarousel,
                            contentDescription = "Toggle layout"
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            PlatformTabRow(
                platforms = state.platforms,
                selectedPlatform = state.selectedPlatform,
                onPlatformSelected = viewModel::selectPlatform
            )

            when (state.layoutMode) {
                LayoutMode.CAROUSEL -> CarouselHomeContent(
                    games = state.games,
                    selectedGameMedia = state.selectedGameMedia,
                    selectedIndex = state.selectedGameIndex,
                    shouldPlayVideo = state.shouldPlayVideo,
                    videoMuted = state.videoMuted,
                    onGameSelected = viewModel::onGameSelected,
                    onGameClick = onGameClick,
                    onMuteToggle = viewModel::toggleMute,
                    modifier = Modifier.fillMaxSize()
                )
                LayoutMode.GRID -> GridHomeContent(
                    games = state.games,
                    onGameClick = onGameClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
