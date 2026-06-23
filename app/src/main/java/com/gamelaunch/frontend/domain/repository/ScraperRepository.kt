package com.gamelaunch.frontend.domain.repository

import com.gamelaunch.frontend.data.network.dto.GameInfoDto
import com.gamelaunch.frontend.domain.model.ScraperConfig

interface ScraperRepository {
    suspend fun scrapeGame(
        config: ScraperConfig,
        systemId: Int,
        romName: String,
        md5: String? = null,
        crc: String? = null
    ): Result<GameInfoDto>

    suspend fun validateCredentials(config: ScraperConfig): Result<Boolean>
}
