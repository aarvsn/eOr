package com.gamelaunch.frontend.domain.usecase

import com.gamelaunch.frontend.data.db.dao.LaunchBoxDao
import com.gamelaunch.frontend.data.network.LaunchBoxService
import com.gamelaunch.frontend.data.parser.LaunchBoxXmlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.zip.ZipInputStream
import javax.inject.Inject

sealed class LbSyncStatus {
    object Downloading : LbSyncStatus()
    data class Parsing(val gamesIndexed: Int) : LbSyncStatus()
    data class Complete(val totalGames: Int) : LbSyncStatus()
    data class Error(val message: String) : LbSyncStatus()
}

class SyncLaunchBoxUseCase @Inject constructor(
    private val launchBoxService: LaunchBoxService,
    private val launchBoxDao: LaunchBoxDao
) {
    operator fun invoke(): Flow<LbSyncStatus> = flow {
        emit(LbSyncStatus.Downloading)

        val response = runCatching { launchBoxService.downloadMetadata() }.getOrElse {
            emit(LbSyncStatus.Error("Download failed: ${it.message}"))
            return@flow
        }

        if (!response.isSuccessful) {
            emit(LbSyncStatus.Error("Server error ${response.code()}"))
            return@flow
        }

        val body = response.body() ?: run {
            emit(LbSyncStatus.Error("Empty response"))
            return@flow
        }

        launchBoxDao.clearImages()
        launchBoxDao.clearGames()

        var totalGames = 0

        ZipInputStream(body.byteStream().buffered()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "Metadata.xml") {
                    LaunchBoxXmlParser().parse(
                        input = zip,
                        onBatch = { games, images ->
                            if (games.isNotEmpty()) launchBoxDao.insertGames(games)
                            if (images.isNotEmpty()) launchBoxDao.insertImages(images)
                            totalGames += games.size
                            emit(LbSyncStatus.Parsing(totalGames))
                        }
                    )
                    break
                }
                entry = zip.nextEntry
            }
        }

        if (totalGames == 0) {
            emit(LbSyncStatus.Error("No games found in database"))
        } else {
            emit(LbSyncStatus.Complete(totalGames))
        }
    }.flowOn(Dispatchers.IO)
}
