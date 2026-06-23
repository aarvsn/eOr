package com.gamelaunch.frontend.launcher

import android.content.Context
import android.content.pm.PackageManager
import com.gamelaunch.frontend.domain.model.InstalledEmulator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val knownEmulators = listOf(
        "org.libretro.retroarch" to "RetroArch",
        "org.dolphinemu.dolphinemu" to "Dolphin",
        "org.ppsspp.ppsspp" to "PPSSPP",
        "com.github.stenzek.duckstation" to "DuckStation",
        "com.drastic.ds" to "DraStic",
        "com.explusalpha.GbcEmu" to "GBC.emu",
        "com.explusalpha.GbaEmu" to "GBA.emu",
        "com.explusalpha.Snes9xEmu" to "Snes9x EX+",
        "com.reicast.emulator" to "Reicast",
        "com.flycast.emulator" to "Flycast",
        "ru.playsoftware.j2meloader" to "J2ME Loader",
        "org.yuzu.yuzu_emu" to "Yuzu",
        "org.citra_emu.citra" to "Citra"
    )

    fun getInstalledEmulators(): List<InstalledEmulator> {
        val pm = context.packageManager
        return knownEmulators.mapNotNull { (pkg, name) ->
            runCatching { pm.getPackageInfo(pkg, 0) }
                .getOrNull()
                ?.let { InstalledEmulator(packageName = pkg, displayName = name) }
        }
    }

    fun isPackageInstalled(packageName: String): Boolean = runCatching {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    }.getOrDefault(false)
}
