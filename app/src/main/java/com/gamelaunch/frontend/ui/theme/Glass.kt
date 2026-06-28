package com.gamelaunch.frontend.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.ui.graphics.lerp
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

// "Back-ease" bezier — overshoots past the target then settles, for a natural little bounce.
val BounceEasing = CubicBezierEasing(0.34f, 1.8f, 0.45f, 1f)
const val BounceDurationMs = 420

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
 * Colourful glass tile: a solid colour fill with a soft top-to-bottom sheen, a thin bright edge
 * highlight and a soft floating shadow (colour-tinted when focused). Unselected tiles use a light
 * pastel shade; focused tiles deepen to the full colour and lift, 3DS-style. The fill is opaque so
 * the shadow never bleeds through.
 */
fun Modifier.glassTile(
    shape: Shape,
    color: Color,
    selected: Boolean = false
): Modifier {
    val base = if (selected) color else lerp(color, Color.White, 0.5f)
    return this
        .shadow(
            elevation = if (selected) 18.dp else 7.dp,
            shape = shape,
            ambientColor = if (selected) color else Color(0xFF2A3550),
            spotColor = if (selected) color else Color(0xFF2A3550),
            clip = false
        )
        .clip(shape)
        .background(
            Brush.verticalGradient(listOf(lerp(base, Color.White, 0.18f), base))
        )
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.1f))
            ),
            shape = shape
        )
}

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
