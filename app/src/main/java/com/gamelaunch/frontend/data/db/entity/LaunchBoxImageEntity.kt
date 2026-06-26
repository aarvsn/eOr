package com.gamelaunch.frontend.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lb_images",
    indices = [Index("game_id")]
)
data class LaunchBoxImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "game_id") val gameId: Int,
    @ColumnInfo(name = "file_name") val fileName: String,
    val type: String,
    val region: String?
)
