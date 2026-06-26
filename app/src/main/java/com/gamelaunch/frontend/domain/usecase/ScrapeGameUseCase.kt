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
import javax.inject.Inject

sealed class ScrapeResult {
    data class Success(val gameId: Long) : ScrapeResult()
    data class NotFound(val gameId: Long, val romName: String) : ScrapeResult()
    data class RateLimited(val gameId: Long) : ScrapeResult()
    data class Error(val gameId: Long, val cause: Throwable) : ScrapeResult()
}

class ScrapeGameUseCase @Inject constructor(
    private val scraperRepository: ScraperRepository,
    private val scrapeLaunchBoxUseCase: ScrapeLaunchBoxUseCase,
    private val gameRepository: GameRepository,
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(game: Game, config: ScraperConfig): ScrapeResult {
        val platform = PlatformDefinitions.byId[game.platformId]
            ?: return ScrapeResult.Error(game.id, IllegalArgumentException("Unknown platform: ${game.platformId}"))

        // ── ScreenScraper ────────────────────────────────────────────────
        if (config.isConfigured) {
            val ssResult = scraperRepository.scrapeGame(
                config   = config,
                systemId = platform.scraperSystemId,
                romName  = game.romFilename,
                md5      = game.md5
            )

            ssResult.onSuccess { gameInfo ->
                val title = gameInfo.getBestName(config.preferredRegion) ?: game.title
                gameRepository.updateScrapedMetadata(
                    gameId        = game.id,
                    scraperGameId = gameInfo.id?.toLongOrNull(),
                    title         = title,
                    description   = gameInfo.getBestSynopsis(config.preferredRegion),
                    genre         = gameInfo.getPrimaryGenre(),
                    releaseYear   = gameInfo.getReleaseYear(config.preferredRegion),
                    rating        = gameInfo.getRating()
                )
                val media = GameMedia(
                    gameId             = game.id,
                    boxArtRemoteUrl    = if (config.scrapeBoxArt)     gameInfo.getMediaUrl("box-2D", config.preferredRegion) else null,
                    screenshotRemoteUrl= if (config.scrapeScreenshots) gameInfo.getMediaUrl("ss",    config.preferredRegion) else null,
                    wheelLogoRemoteUrl = if (config.scrapeWheelLogos)  gameInfo.getMediaUrl("wheel", config.preferredRegion) else null,
                    videoRemoteUrl     = if (config.scrapeVideos)
                        gameInfo.getMediaUrl("video-normalized", config.preferredRegion)
                            ?: gameInfo.getMediaUrl("video", config.preferredRegion)
                        else null,
                    scraperTimestampMs = System.currentTimeMillis()
                )
                mediaRepository.upsertMedia(media)
                media.boxArtRemoteUrl?.let     { mediaRepository.downloadAndCacheBoxArt(game.id, it) }
                media.screenshotRemoteUrl?.let { mediaRepository.downloadAndCacheScreenshot(game.id, it) }
                media.wheelLogoRemoteUrl?.let  { mediaRepository.downloadAndCacheWheelLogo(game.id, it) }
                media.videoRemoteUrl?.let      { mediaRepository.downloadAndCacheVideo(game.id, it) }
                return ScrapeResult.Success(game.id)
            }

            ssResult.exceptionOrNull()?.let { e ->
                when (e) {
                    is RateLimitException -> return ScrapeResult.RateLimited(game.id)
                    // On NotFound or generic error fall through to LaunchBox
                    !is GameNotFoundException -> return ScrapeResult.Error(game.id, e)
                }
            }
        }

        // ── LaunchBox fallback ───────────────────────────────────────────
        val lbMedia = runCatching {
            scrapeLaunchBoxUseCase(game.title, game.platformId)
        }.getOrNull()

        if (lbMedia != null) {
            // Update metadata from LaunchBox
            gameRepository.updateScrapedMetadata(
                gameId        = game.id,
                scraperGameId = null,
                title         = lbMedia.title,
                description   = lbMedia.overview,
                genre         = null,
                releaseYear   = lbMedia.releaseYear,
                rating        = lbMedia.rating
            )
            val media = GameMedia(
                gameId              = game.id,
                boxArtRemoteUrl     = lbMedia.boxFrontUrl,
                screenshotRemoteUrl = lbMedia.screenshotUrl,
                wheelLogoRemoteUrl  = lbMedia.logoUrl,
                videoRemoteUrl      = null, // LaunchBox has no hosted videos
                scraperTimestampMs  = System.currentTimeMillis()
            )
            mediaRepository.upsertMedia(media)
            lbMedia.boxFrontUrl?.let     { mediaRepository.downloadAndCacheBoxArt(game.id, it) }
            lbMedia.screenshotUrl?.let   { mediaRepository.downloadAndCacheScreenshot(game.id, it) }
            lbMedia.logoUrl?.let         { mediaRepository.downloadAndCacheWheelLogo(game.id, it) }
            return ScrapeResult.Success(game.id)
        }

        return ScrapeResult.NotFound(game.id, game.romFilename)
    }
}
