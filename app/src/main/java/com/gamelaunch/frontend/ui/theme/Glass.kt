package com.gamelaunch.frontend.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// ── Light, playful liquid-glass + 3DS palette ───────────────────────────────
val LightBg   = Color(0xFFEDEFF4)   // very light cool grey base
val TileText  = Color(0xFF20242E)   // dark slate for labels on light/pastel tiles
val TileSub   = Color(0xFF5A6173)   // muted slate for subtitles
val BrandBlue = Color(0xFF3E7BFF)   // accent for selected chips / branding

// Cheerful 3DS-ish tile palette — assigned per tile so the grid reads colourful.
val TilePalette = listOf(
    Color(0xFF4FB7F5), // sky
    Color(0xFF7C8CFF), // periwinkle
    Color(0xFFB07BFF), // lavender
    Color(0xFFFF7AA8), // rose
    Color(0xFFFF9F66), // coral
    Color(0xFFFFC04D), // amber
    Color(0xFF3FD3A6), // mint
    Color(0xFF53CFE0), // teal
)
fun tileColor(index: Int): Color = TilePalette[index % TilePalette.size]

/** Soft pastel ambient over a light grey base — gentle colour washes for depth. */
@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier
            .fillMaxSize()
            .background(LightBg)
            .drawBehind {
                fun glow(color: Color, cx: Float, cy: Float, r: Float) = drawRect(
                    Brush.radialGradient(
                        colors = listOf(color, Color.Transparent),
                        center = Offset(size.width * cx, size.height * cy),
                        radius = size.minDimension * r
                    )
                )
                glow(Color(0xFF6FC4FF).copy(alpha = 0.22f), 0.08f, 0.02f, 0.95f)
                glow(Color(0xFFB58CFF).copy(alpha = 0.20f), 0.98f, 0.08f, 1.05f)
                glow(Color(0xFF59E0B8).copy(alpha = 0.16f), 0.55f, 1.05f, 1.05f)
                glow(Color(0xFFFF9CC0).copy(alpha = 0.14f), 0.18f, 0.95f, 0.75f)
            },
        content = content
    )
}

/**
 * Colourful frosted-glass tile: a translucent tinted fill that lets the light base soften it to a
 * pastel, a bright specular highlight along the top edge, and a soft floating shadow (tinted when
 * focused). Focused tiles turn vivid and lift, 3DS-style.
 */
fun Modifier.glassTile(
    shape: Shape,
    color: Color,
    selected: Boolean = false
): Modifier = this
    .shadow(
        elevation = if (selected) 16.dp else 6.dp,
        shape = shape,
        ambientColor = if (selected) color else Color(0xFF2A3550),
        spotColor = if (selected) color else Color(0xFF2A3550),
        clip = false
    )
    .clip(shape)
    .background(
        Brush.verticalGradient(
            if (selected) listOf(color.copy(alpha = 0.95f), color.copy(alpha = 0.70f))
            else listOf(color.copy(alpha = 0.60f), color.copy(alpha = 0.38f))
        )
    )
    .border(
        width = if (selected) 1.5.dp else 1.dp,
        brush = Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (selected) 0.9f else 0.75f),
                Color.White.copy(alpha = if (selected) 0.25f else 0.15f)
            )
        ),
        shape = shape
    )

/**
 * Neutral frosted chip for tabs and icon buttons. Frosted white by default; an accent fill when
 * selected.
 */
fun Modifier.glassChip(
    shape: Shape,
    selected: Boolean = false,
    accent: Color = BrandBlue
): Modifier = this
    .shadow(
        elevation = if (selected) 10.dp else 3.dp,
        shape = shape,
        ambientColor = if (selected) accent else Color(0xFF2A3550),
        spotColor = if (selected) accent else Color(0xFF2A3550),
        clip = false
    )
    .clip(shape)
    .background(
        Brush.verticalGradient(
            if (selected) listOf(accent.copy(alpha = 0.95f), accent.copy(alpha = 0.78f))
            else listOf(Color.White.copy(alpha = 0.72f), Color.White.copy(alpha = 0.48f))
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.3f))
        ),
        shape = shape
    )
