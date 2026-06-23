package com.gamelaunch.frontend.data.repository

import android.content.Context
import com.gamelaunch.frontend.data.db.dao.GameMediaDao
import com.gamelaunch.frontend.data.db.entity.GameMediaEntity
import com.gamelaunch.frontend.domain.model.GameMedia
import com.gamelaunch.frontend.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gameMediaDao: GameMediaDao,
    private val okHttpClient: OkHttpClient
) : MediaRepository {

    private val mediaDir: File
        get() = File(context.filesDir, "media").also { it.mkdirs() }

    override suspend fun getMediaForGame(gameId: Long): GameMedia? =
        gameMediaDao.getMediaForGame(gameId)?.toDomain()

    override fun observeMediaForGame(gameId: Long): Flow<GameMedia?> =
        gameMediaDao.observeMediaForGame(gameId).map { it?.toDomain() }

    override suspend fun upsertMedia(media: GameMedia) {
        gameMediaDao.upsertMedia(media.toEntity())
    }

    override suspend fun downloadAndCacheBoxArt(gameId: Long, url: String): String? =
        downloadFile(url, File(mediaDir, "boxart/${gameId}.jpg"))?.also { path ->
            gameMediaDao.updateBoxArtLocalPath(gameId, path)
        }

    override suspend fun downloadAndCacheVideo(gameId: Long, url: String): String? =
        downloadFile(url, File(mediaDir, "videos/${gameId}.mp4"))?.also { path ->
            gameMediaDao.updateVideoLocalPath(gameId, path)
        }

    override suspend fun downloadAndCacheScreenshot(gameId: Long, url: String): String? =
        downloadFile(url, File(mediaDir, "screenshots/${gameId}.jpg"))

    override suspend fun downloadAndCacheWheelLogo(gameId: Long, url: String): String? =
        downloadFile(url, File(mediaDir, "wheels/${gameId}.png"))

    override suspend fun deleteMediaForGame(gameId: Long) {
        gameMediaDao.deleteMediaForGame(gameId)
        listOf("boxart", "videos", "screenshots", "wheels").forEach { dir ->
            File(mediaDir, "$dir/$gameId.*").parentFile?.listFiles()
                ?.filter { it.nameWithoutExtension == gameId.toString() }
                ?.forEach { it.delete() }
        }
    }

    private suspend fun downloadFile(url: String, dest: File): String? = withContext(Dispatchers.IO) {
        runCatching {
            dest.parentFile?.mkdirs()
            if (dest.exists() && dest.length() > 0) return@withContext dest.absolutePath

            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                response.body?.byteStream()?.use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
            }
            dest.absolutePath
        }.getOrNull()
    }
}

private fun GameMediaEntity.toDomain() = GameMedia(
    id = id,
    gameId = gameId,
    boxArtLocalPath = boxArtLocalPath,
    boxArtRemoteUrl = boxArtRemoteUrl,
    screenshotLocalPath = screenshotLocalPath,
    screenshotRemoteUrl = screenshotRemoteUrl,
    wheelLogoLocalPath = wheelLogoLocalPath,
    wheelLogoRemoteUrl = wheelLogoRemoteUrl,
    videoLocalPath = videoLocalPath,
    videoRemoteUrl = videoRemoteUrl,
    backgroundLocalPath = backgroundLocalPath,
    scraperTimestampMs = scraperTimestampMs
)

private fun GameMedia.toEntity() = GameMediaEntity(
    id = id,
    gameId = gameId,
    boxArtLocalPath = boxArtLocalPath,
    boxArtRemoteUrl = boxArtRemoteUrl,
    screenshotLocalPath = screenshotLocalPath,
    screenshotRemoteUrl = screenshotRemoteUrl,
    wheelLogoLocalPath = wheelLogoLocalPath,
    wheelLogoRemoteUrl = wheelLogoRemoteUrl,
    videoLocalPath = videoLocalPath,
    videoRemoteUrl = videoRemoteUrl,
    backgroundLocalPath = backgroundLocalPath,
    scraperTimestampMs = scraperTimestampMs
)
