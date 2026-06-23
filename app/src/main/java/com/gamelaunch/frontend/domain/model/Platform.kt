package com.gamelaunch.frontend.domain.model

data class Platform(
    val id: String,
    val displayName: String,
    val scraperSystemId: Int,
    val extensions: List<String>,
    val folderNames: List<String>,
    val defaultEmulatorPackage: String? = null,
    val defaultCoreForRetroArch: String? = null
)
