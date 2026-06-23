package com.gamelaunch.frontend.ui.theme.carousel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gamelaunch.frontend.domain.model.Game
import com.gamelaunch.frontend.domain.model.GameMedia
import com.gamelaunch.frontend.ui.component.AsyncGameArtwork
import com.gamelaunch.frontend.ui.component.VideoPlayer
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CarouselHomeContent(
    games: List<Game>,
    selectedGameMedia: GameMedia?,
    selectedIndex: Int,
    shouldPlayVideo: Boolean,
    videoMuted: Boolean,
    onGameSelected: (Int) -> Unit,
    onGameClick: (Long) -> Unit,
    onMuteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Detect scroll-driven selection changes
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex
        }.distinctUntilChanged().collect { index ->
            if (index != selectedIndex) {
                onGameSelected(index)
            }
        }
    }

    // Scroll to match external selection (e.g. platform switch)
    LaunchedEffect(selectedIndex) {
        if (listState.firstVisibleItemIndex != selectedIndex) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    Box(modifier = modifier) {
        // Background: blurred box art OR video
        if (shouldPlayVideo && selectedGameMedia?.effectiveVideo != null) {
            VideoPlayer(
                videoPath = selectedGameMedia.effectiveVideo,
                shouldPlay = true,
                isMuted = videoMuted,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncGameArtwork(
                localPath = selectedGameMedia?.backgroundLocalPath ?: selectedGameMedia?.boxArtLocalPath,
                remoteUrl = selectedGameMedia?.effectiveBackground ?: selectedGameMedia?.boxArtRemoteUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Dark gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.7f))
                    )
                )
        )

        // Game title overlay at bottom
        games.getOrNull(selectedIndex)?.let { game ->
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 180.dp, start = 24.dp, end = 24.dp)
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                game.genre?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                }
            }
        }

        // Mute toggle
        IconButton(
            onClick = onMuteToggle,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) {
            Icon(
                imageVector = if (videoMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = if (videoMuted) "Unmute" else "Mute",
                tint = Color.White
            )
        }

        // Carousel of game cards
        if (games.isNotEmpty()) {
            LazyRow(
                state = listState,
                flingBehavior = snapBehavior,
                contentPadding = PaddingValues(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth()
            ) {
                itemsIndexed(games) { index, game ->
                    CarouselGameCard(
                        game = game,
                        media = if (index == selectedIndex) selectedGameMedia else null,
                        isSelected = index == selectedIndex,
                        onClick = {
                            if (index == selectedIndex) onGameClick(game.id)
                            else onGameSelected(index)
                        }
                    )
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No games found", color = Color.White)
            }
        }
    }
}
