package com.gamelaunch.frontend.domain.platform

import com.gamelaunch.frontend.domain.model.Platform
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlatformDetector @Inject constructor() {

    fun detect(file: File, parentFolderName: String): Platform? {
        // Parent folder name is more reliable — handles ambiguous extensions like .iso, .bin
        PlatformDefinitions.byFolderName[parentFolderName.lowercase()]?.let { return it }

        // Walk up one more level if direct parent didn't match (e.g. ROMs/PS1/subdir/game.bin)
        file.parentFile?.parentFile?.name?.let { grandparentName ->
            PlatformDefinitions.byFolderName[grandparentName.lowercase()]?.let { return it }
        }

        val ext = ".${file.extension.lowercase()}"
        return PlatformDefinitions.byExtension[ext]
    }
}
