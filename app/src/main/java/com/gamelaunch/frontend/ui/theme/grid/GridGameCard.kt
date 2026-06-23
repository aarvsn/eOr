package com.gamelaunch.frontend.ui.theme.grid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gamelaunch.frontend.domain.model.Game
import com.gamelaunch.frontend.domain.model.GameMedia
import com.gamelaunch.frontend.ui.component.AsyncGameArtwork

@Composable
fun GridGameCard(
    game: Game,
    media: GameMedia? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(6.dp)
    Column(
        modifier = Modifier
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        AsyncGameArtwork(
            localPath = media?.boxArtLocalPath,
            remoteUrl = media?.boxArtRemoteUrl,
            contentDescription = game.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(shape)
        )
        Text(
            text = game.title,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp, start = 2.dp, end = 2.dp)
        )
    }
}
