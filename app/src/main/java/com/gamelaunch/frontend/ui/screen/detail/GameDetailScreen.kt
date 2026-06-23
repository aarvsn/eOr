package com.gamelaunch.frontend.ui.screen.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelaunch.frontend.ui.component.AsyncGameArtwork
import com.gamelaunch.frontend.ui.component.VideoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    onBack: () -> Unit,
    viewModel: GameDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.game?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (state.isFavorite) "Remove from favorites" else "Add to favorites"
                        )
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

        val game = state.game ?: return@Scaffold
        val media = state.media

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Video / artwork header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                if (media?.effectiveVideo != null) {
                    VideoPlayer(
                        videoPath = media.effectiveVideo,
                        shouldPlay = state.shouldPlayVideo,
                        isMuted = state.videoMuted,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncGameArtwork(
                        localPath = media?.screenshotLocalPath ?: media?.boxArtLocalPath,
                        remoteUrl = media?.screenshotRemoteUrl ?: media?.boxArtRemoteUrl,
                        contentDescription = game.title,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (media?.effectiveVideo != null) {
                    IconButton(
                        onClick = viewModel::toggleMute,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                    ) {
                        Icon(
                            if (state.videoMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = null
                        )
                    }
                }
            }

            // Info row: box art + metadata
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncGameArtwork(
                    localPath = media?.boxArtLocalPath,
                    remoteUrl = media?.boxArtRemoteUrl,
                    contentDescription = game.title,
                    modifier = Modifier
                        .width(100.dp)
                        .aspectRatio(0.75f)
                        .clip(RoundedCornerShape(6.dp))
                )
                Column {
                    game.genre?.let { Text(it, style = MaterialTheme.typography.labelMedium) }
                    game.releaseYear?.let { Text("$it", style = MaterialTheme.typography.labelMedium) }
                    game.rating?.let {
                        Text("Rating: ${"%.1f".format(it)}/5", style = MaterialTheme.typography.labelMedium)
                    }
                    Text(game.platformId.uppercase(), style = MaterialTheme.typography.labelSmall)
                }
            }

            // Launch button
            Button(
                onClick = viewModel::launchGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Play")
            }

            // Description
            game.description?.let { desc ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        // Launch error dialog
        state.launchError?.let { error ->
            AlertDialog(
                onDismissRequest = viewModel::dismissError,
                title = { Text("Cannot Launch Game") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = viewModel::dismissError) { Text("OK") }
                }
            )
        }
    }
}
