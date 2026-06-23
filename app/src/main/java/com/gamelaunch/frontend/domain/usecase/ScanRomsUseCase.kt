package com.gamelaunch.frontend.domain.usecase

import com.gamelaunch.frontend.domain.model.Game
import com.gamelaunch.frontend.domain.platform.PlatformDetector
import com.gamelaunch.frontend.domain.repository.GameRepository
import com.gamelaunch.frontend.util.StorageUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

data class ScanProgress(
    val scanned: Int,
    val total: Int,
    val currentFile: String = "",
    val added: Int = 0
)

class ScanRomsUseCase @Inject constructor(
    private val gameRepository: GameRepository,
    private val platformDetector: PlatformDetector
) {
    private val skipExtensions = setOf(
        ".txt", ".xml", ".cue", ".nfo", ".jpg", ".png", ".mp4", ".zip", ".7z", ".rar"
    )

    operator fun invoke(rootPath: String): Flow<ScanProgress> = flow {
        // Resolve SAF URIs and broken /tree/... paths to real filesystem paths
        val resolvedPath = StorageUtils.resolveStoredPath(rootPath)
        val rootDir = File(resolvedPath)
        if (!rootDir.exists() || !rootDir.isDirectory) {
            emit(ScanProgress(0, 0, "Root folder not found: $resolvedPath"))
            return@flow
        }

        val romFiles = rootDir.walkTopDown()
            .filter { it.isFile && ".${it.extension.lowercase()}" !in skipExtensions }
            .toList()

        val validPaths = mutableListOf<String>()
        var added = 0

        romFiles.forEachIndexed { index, file ->
            emit(ScanProgress(index, romFiles.size, file.name, added))

            val platform = platformDetector.detect(file, file.parentFile?.name ?: "") ?: return@forEachIndexed

            validPaths.add(file.absolutePath)

            val md5 = computeMd5Partial(file)
            val title = file.nameWithoutExtension
                .replace(Regex("\\(.*?\\)"), "")  // strip (USA), (E), etc.
                .replace(Regex("\\[.*?]"), "")     // strip [!], [b], etc.
                .trim()

            val game = Game(
                title = title,
                romPath = file.absolutePath,
                romFilename = file.name,
                platformId = platform.id,
                md5 = md5
            )

            val insertedId = gameRepository.insertGame(game)
            if (insertedId > 0) added++
        }

        // Remove DB entries for ROMs that no longer exist on disk
        if (validPaths.isNotEmpty()) {
            gameRepository.deleteGamesNotInPaths(validPaths)
        }

        emit(ScanProgress(romFiles.size, romFiles.size, added = added))
    }

    private fun computeMd5Partial(file: File): String? = runCatching {
        val md = MessageDigest.getInstance("MD5")
        file.inputStream().use { stream ->
            val buffer = ByteArray(8 * 1024 * 1024) // hash first 8 MB
            val read = stream.read(buffer)
            if (read > 0) md.update(buffer, 0, read)
        }
        md.digest().joinToString("") { "%02x".format(it) }
    }.getOrNull()
}
