package com.gamelaunch.frontend.data.repository

import com.gamelaunch.frontend.data.network.ScreenScraperApi
import com.gamelaunch.frontend.data.network.dto.GameInfoDto
import com.gamelaunch.frontend.domain.model.ScraperConfig
import com.gamelaunch.frontend.domain.repository.ScraperRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScraperRepositoryImpl @Inject constructor(
    private val api: ScreenScraperApi
) : ScraperRepository {

    override suspend fun scrapeGame(
        config: ScraperConfig,
        systemId: Int,
        romName: String,
        md5: String?,
        crc: String?
    ): Result<GameInfoDto> = runCatching {
        val response = api.getGameInfo(
            devId = config.devid,
            devPassword = config.devpassword,
            softName = config.softname,
            ssId = config.ssid,
            ssPassword = config.sspassword,
            systemId = systemId,
            romName = romName,
            md5 = md5,
            crc = crc
        )
        when {
            response.code() == 429 -> throw RateLimitException("ScreenScraper rate limit exceeded")
            response.code() == 404 -> throw GameNotFoundException("Game not found: $romName")
            !response.isSuccessful -> throw ScraperException("API error ${response.code()}: ${response.message()}")
            else -> response.body()?.response?.game
                ?: throw ScraperException("Empty response body for $romName")
        }
    }

    override suspend fun validateCredentials(config: ScraperConfig): Result<Boolean> = runCatching {
        val response = api.validateCredentials(
            devId = config.devid,
            devPassword = config.devpassword,
            softName = config.softname,
            ssId = config.ssid,
            ssPassword = config.sspassword
        )
        response.isSuccessful && response.body()?.header?.success == "true"
    }
}

class RateLimitException(message: String) : Exception(message)
class GameNotFoundException(message: String) : Exception(message)
class ScraperException(message: String) : Exception(message)
