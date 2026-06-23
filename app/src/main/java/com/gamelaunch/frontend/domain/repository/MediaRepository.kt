package com.gamelaunch.frontend.domain.repository

import com.gamelaunch.frontend.domain.model.GameMedia
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    suspend fun getMediaForGame(gameId: Long): GameMedia?
    fun observeMediaForGame(gameId: Long): Flow<GameMedia?>
    suspend fun upsertMedia(media: GameMedia)
    suspend fun downloadAndCacheBoxArt(gameId: Long, url: String): String?
    suspend fun downloadAndCacheVideo(gameId: Long, url: String): String?
    suspend fun downloadAndCacheScreenshot(gameId: Long, url: String): String?
    suspend fun downloadAndCacheWheelLogo(gameId: Long, url: String): String?
    suspend fun deleteMediaForGame(gameId: Long)
}
