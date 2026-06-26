package com.gamelaunch.frontend.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lb_games",
    indices = [Index("platform"), Index("name")]
)
data class LaunchBoxGameEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val platform: String,
    @ColumnInfo(name = "release_year") val releaseYear: Int?,
    val overview: String?,
    val developer: String?,
    val publisher: String?,
    val rating: Float?,
    @ColumnInfo(name = "video_url") val videoUrl: String?
)
