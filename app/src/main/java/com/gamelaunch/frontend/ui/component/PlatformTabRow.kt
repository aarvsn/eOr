package com.gamelaunch.frontend.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gamelaunch.frontend.R
import com.gamelaunch.frontend.ui.theme.ElectricBlue
import com.gamelaunch.frontend.ui.theme.NeonPurple

private val platformLabels = mapOf(
    "nes"      to "NES",    "snes"    to "SNES",    "n64"    to "N64",
    "gb"       to "GB",     "gbc"     to "GBC",     "gba"    to "GBA",
    "nds"      to "NDS",    "3ds"     to "3DS",     "switch" to "Switch",
    "ps1"      to "PS1",    "ps2"     to "PS2",     "ps3"    to "PS3",
    "psp"      to "PSP",    "dc"      to "DC",      "saturn" to "Saturn",
    "genesis"  to "GEN",    "gg"      to "GG",      "sms"    to "SMS",
    "pce"      to "PCE",    "neogeo"  to "Neo·Geo", "arcade" to "Arcade",
    "msx"      to "MSX",    "lynx"    to "Lynx",    "atari"  to "Atari"
)

// A bit of whimsy: a controller silhouette that fits each console family.
private fun platformPadIcon(platformId: String): Int = when (platformId) {
    "nes", "famicom", "fds" -> R.drawable.ic_pad_nes
    "gb", "gbc", "gba", "nds", "3ds", "psp", "gg", "lynx", "ngp", "ws" ->
        R.drawable.ic_pad_handheld
    "arcade", "mame", "neogeo", "cps", "cps1", "cps2", "cps3", "fbneo" ->
        R.drawable.ic_pad_arcade
    else -> R.drawable.ic_pad_gamepad   // snes, n64, genesis, sms, ps*, dc, saturn, switch, …
}

@Composable
fun PlatformTabRow(
    platforms: List<String>,
    selectedPlatform: String?,
    onPlatformSelected: (String) -> Unit
) {
    if (platforms.isEmpty()) return

    val pillShape = RoundedCornerShape(50)
    val gradient  = Brush.horizontalGradient(listOf(ElectricBlue, NeonPurple))
    val listState = rememberLazyListState()

    val selectedIndex = platforms.indexOf(selectedPlatform)

    // Keep the selected pill centred as the user bumpers through platforms.
    LaunchedEffect(selectedIndex) {
        if (selectedIndex < 0) return@LaunchedEffect
        val layout      = listState.layoutInfo
        val viewport    = layout.viewportEndOffset - layout.viewportStartOffset
        val itemSize    = layout.visibleItemsInfo.firstOrNull { it.index == selectedIndex }?.size
            ?: layout.visibleItemsInfo.firstOrNull()?.size
            ?: 0
        val centerOffset = ((viewport - itemSize) / 2).coerceAtLeast(0)
        listState.animateScrollToItem(selectedIndex, -centerOffset)
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(platforms, key = { _, id -> id }) { _, platformId ->
            val isSelected = platformId == selectedPlatform
            val label = platformLabels[platformId] ?: platformId.uppercase()
            val tint  = if (isSelected) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(pillShape)
                    .then(
                        if (isSelected)
                            Modifier.background(gradient)
                        else
                            Modifier
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, MaterialTheme.colorScheme.outline, pillShape)
                    )
                    .clickable { onPlatformSelected(platformId) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Icon(
                    painter = painterResource(platformPadIcon(platformId)),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = tint
                )
            }
        }
    }
}
