package com.gamelaunch.frontend.domain.platform

import com.gamelaunch.frontend.domain.model.Platform
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlatformDetector @Inject constructor() {

    // Extensions used by lots of non-game data files (saves, dumps, disc tracks, system files).
    // For these we never trust the extension alone — the file must live inside a recognised
    // platform folder AND the extension must be valid for that platform.
    private val ambiguousExtensions = setOf(".bin", ".iso", ".img", ".chd", ".cso", ".cue")

    fun detect(file: File, parentFolderName: String): Platform? {
        val ext = ".${file.extension.lowercase()}"

        // Walk the full ancestry looking for a folder named after a known platform.
        var dir: File? = file.parentFile
        var folderMatch: Platform? = null
        while (dir != null && folderMatch == null) {
            folderMatch = PlatformDefinitions.byFolderName[dir.name.lowercase()]
            dir = dir.parentFile
        }

        if (folderMatch != null) {
            // A stray data file inside a system folder (e.g. a .bin save under psp/) has an
            // extension the platform doesn't use — reject it so it never shows up as a "game".
            val validForPlatform = folderMatch.extensions.any { it.equals(ext, ignoreCase = true) }
            return if (validForPlatform) folderMatch else null
        }

        // No recognised folder in the path: only trust unambiguous ROM extensions.
        if (ext in ambiguousExtensions) return null
        return PlatformDefinitions.byExtension[ext]
    }
}
