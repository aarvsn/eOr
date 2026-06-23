package com.gamelaunch.frontend.domain.usecase

import com.gamelaunch.frontend.data.repository.GameNotFoundException
import com.gamelaunch.frontend.data.repository.RateLimitException
import com.gamelaunch.frontend.domain.model.Game
import com.gamelaunch.frontend.domain.model.GameMedia
import com.gamelaunch.frontend.domain.model.ScraperConfig
import com.gamelaunch.frontend.domain.platform.PlatformDefinitions
import com.gamelaunch.frontend.domain.repository.GameRepository
import com.gamelaunch.frontend.domain.repository.MediaRepository
import com.gamelaunch.frontend.domain.repository.ScraperRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

sealed class ScrapeResult {
    data class Success(val gameId: Long) : ScrapeResult()
    data class NotFound(val gameId: Long, val romName: String) : ScrapeResult()
    data class RateLimited(val gameId: Long) : ScrapeResult()
    data class Error(val gameId: Long, val cause: Throwable) : ScrapeResult()
}

class ScrapeGameUseCase @Inject constructor(
    private val scraperRepository: ScraperRepository,
    private val gameRepository: GameRepository,
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(game: Game, config: ScraperConfig): ScrapeResult {
        val platform = PlatformDefinitions.byId[game.platformId]
            ?: return ScrapeResult.Error(game.id, IllegalArgumentException("Unknown platform: ${game.platformId}"))

        return scraperRepository.scrapeGame(
            config = config,
            systemId = platform.scraperSystemId,
            romName = game.romFilename,
            md5 = game.md5
        ).fold(
            onSuccess = { gameInfo ->
                val title = gameInfo.getBestName(config.preferredRegion) ?: game.title
                gameRepository.updateScrapedMetadata(
                    gameId = game.id,
                    scraperGameId = gameInfo.id?.toLongOrNull(),
                    title = title,
                    description = gameInfo.getBestSynopsis(config.preferredRegion),
                    genre = gameInfo.getPrimaryGenre(),
                    releaseYear = gameInfo.getReleaseYear(config.preferredRegion),
                    rating = gameInfo.getRating()
                )

                // Build media entity with remote URLs, then cache locally
                val media = GameMedia(
                    gameId = game.id,
                    boxArtRemoteUrl = if (config.scrapeBoxArt) gameInfo.getMediaUrl("box-2D", config.preferredRegion) else null,
                    screenshotRemoteUrl = if (config.scrapeScreenshots) gameInfo.getMediaUrl("ss", config.preferredRegion) else null,
                    wheelLogoRemoteUrl = if (config.scrapeWheelLogos) gameInfo.getMediaUrl("wheel", config.preferredRegion) else null,
                    videoRemoteUrl = if (config.scrapeVideos) gameInfo.getMediaUrl("video-normalized", config.preferredRegion)
                        ?: gameInfo.getMediaUrl("video", config.preferredRegion) else null,
                    scraperTimestampMs = System.currentTimeMillis()
                )
                mediaRepository.upsertMedia(media)

                // Download and cache each asset
                media.boxArtRemoteUrl?.let { mediaRepository.downloadAndCacheBoxArt(game.id, it) }
                media.screenshotRemoteUrl?.let { mediaRepository.downloadAndCacheScreenshot(game.id, it) }
                media.wheelLogoRemoteUrl?.let { mediaRepository.downloadAndCacheWheelLogo(game.id, it) }
                media.videoRemoteUrl?.let { mediaRepository.downloadAndCacheVideo(game.id, it) }

                ScrapeResult.Success(game.id)
            },
            onFailure = { e ->
                when (e) {
                    is RateLimitException -> ScrapeResult.RateLimited(game.id)
                    is GameNotFoundException -> ScrapeResult.NotFound(game.id, game.romFilename)
                    else -> ScrapeResult.Error(game.id, e)
                }
            }
        )
    }
}
