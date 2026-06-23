package com.gamelaunch.frontend.ui.theme.carousel

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.gamelaunch.frontend.domain.model.Game
import com.gamelaunch.frontend.domain.model.GameMedia
import com.gamelaunch.frontend.ui.component.AsyncGameArtwork

@Composable
fun CarouselGameCard(
    game: Game,
    media: GameMedia?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 0.9f,
        label = "card_scale"
    )
    val shape = RoundedCornerShape(8.dp)

    AsyncGameArtwork(
        localPath = media?.boxArtLocalPath,
        remoteUrl = media?.boxArtRemoteUrl,
        contentDescription = game.title,
        modifier = Modifier
            .width(120.dp)
            .height(160.dp)
            .scale(scale)
            .clip(shape)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                else Modifier
            )
            .clickable(onClick = onClick)
    )
}
