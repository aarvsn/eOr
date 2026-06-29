package com.gamelaunch.frontend.domain.usecase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.gamelaunch.frontend.domain.model.Game
import com.gamelaunch.frontend.domain.repository.GameRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ScanAndroidGamesUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gameRepository: GameRepository
) {
    // Package prefixes that belong to the OS or this launcher — skip them.
    private val systemPrefixes = setOf(
        "android", "com.android", "com.google.android", "com.google.ar",
        "com.samsung", "com.miui", "com.huawei", "com.gamelaunch.frontend"
    )

    operator fun invoke(): Flow<ScanProgress> = flow {
        val pm = context.packageManager
        // Primary: apps that declare CATEGORY_GAME.
        val gameIntent = Intent(Intent.ACTION_MAIN).addCategory("android.intent.category.GAME")
        val fromCategory = pm.queryIntentActivities(gameIntent, 0)
            .map { it.activityInfo.packageName }
            .distinct()

        // Filter out system packages.
        val packages = fromCategory.filter { pkg ->
            systemPrefixes.none { pkg == it || pkg.startsWith("$it.") } &&
            runCatching {
                val info = pm.getPackageInfo(pkg, 0)
                (info.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0
            }.getOrDefault(false)
        }

        emit(ScanProgress(0, packages.size, "Scanning installed games…"))

        val validPaths = mutableListOf<String>()
        var added = 0

        packages.forEachIndexed { index, pkg ->
            val label = runCatching {
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            }.getOrDefault(pkg)

            emit(ScanProgress(index, packages.size, label, added))

            val romPath = "package:$pkg"
            validPaths.add(romPath)

            val game = Game(
                title       = label,
                romPath     = romPath,
                romFilename = pkg,
                platformId  = "android"
            )
            val id = gameRepository.insertGame(game)
            if (id > 0) added++
        }

        // Remove android-platform games whose packages are no longer installed.
        gameRepository.deleteAndroidGamesNotIn(validPaths)

        emit(ScanProgress(packages.size, packages.size, added = added))
    }.flowOn(Dispatchers.IO)
}
