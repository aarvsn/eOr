package com.gamelaunch.frontend.ui.component

import androidx.annotation.DrawableRes
import com.gamelaunch.frontend.R
import com.gamelaunch.frontend.domain.platform.PlatformDefinitions

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

/** Short pill label for a platform (falls back to the upper-cased id). */
fun platformLabel(platformId: String): String =
    platformLabels[platformId] ?: platformId.uppercase()

/** Full display name for a platform (falls back to the pill label). */
fun platformDisplayName(platformId: String): String =
    PlatformDefinitions.byId[platformId]?.displayName ?: platformLabel(platformId)

/** A controller silhouette that fits each console family — a bit of whimsy. */
@DrawableRes
fun platformPadIcon(platformId: String): Int = when (platformId) {
    "nes", "famicom", "fds" -> R.drawable.ic_pad_nes
    "gb", "gbc", "gba", "nds", "3ds", "psp", "gg", "lynx", "ngp", "ws" ->
        R.drawable.ic_pad_handheld
    "arcade", "mame", "neogeo", "cps", "cps1", "cps2", "cps3", "fbneo" ->
        R.drawable.ic_pad_arcade
    else -> R.drawable.ic_pad_gamepad   // snes, n64, genesis, sms, ps*, dc, saturn, switch, …
}
