package com.gamelaunch.frontend.ui.component

import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun PlatformTabRow(
    platforms: List<String>,
    selectedPlatform: String?,
    onPlatformSelected: (String) -> Unit
) {
    if (platforms.isEmpty()) return

    val selectedIndex = platforms.indexOf(selectedPlatform).coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 16.dp
    ) {
        platforms.forEachIndexed { index, platformId ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onPlatformSelected(platformId) },
                text = { Text(platformId.uppercase()) }
            )
        }
    }
}
