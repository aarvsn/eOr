package com.gamelaunch.frontend.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gamelaunch.frontend.data.db.entity.LaunchBoxGameEntity
import com.gamelaunch.frontend.data.db.entity.LaunchBoxImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LaunchBoxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<LaunchBoxGameEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<LaunchBoxImageEntity>)

    @Query("SELECT * FROM lb_games WHERE LOWER(name) LIKE LOWER(:query) AND platform = :platform LIMIT 10")
    suspend fun searchGames(query: String, platform: String): List<LaunchBoxGameEntity>

    @Query("SELECT * FROM lb_images WHERE game_id = :gameId")
    suspend fun getImagesForGame(gameId: Int): List<LaunchBoxImageEntity>

    @Query("SELECT COUNT(*) FROM lb_games")
    fun getGameCount(): Flow<Int>

    @Query("DELETE FROM lb_games")
    suspend fun clearGames()

    @Query("DELETE FROM lb_images")
    suspend fun clearImages()
}
