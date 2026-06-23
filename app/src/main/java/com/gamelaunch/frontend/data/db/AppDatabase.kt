package com.gamelaunch.frontend.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gamelaunch.frontend.data.db.dao.EmulatorMappingDao
import com.gamelaunch.frontend.data.db.dao.GameDao
import com.gamelaunch.frontend.data.db.dao.GameMediaDao
import com.gamelaunch.frontend.data.db.entity.EmulatorMappingEntity
import com.gamelaunch.frontend.data.db.entity.GameEntity
import com.gamelaunch.frontend.data.db.entity.GameMediaEntity

@Database(
    entities = [
        GameEntity::class,
        GameMediaEntity::class,
        EmulatorMappingEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun gameMediaDao(): GameMediaDao
    abstract fun emulatorMappingDao(): EmulatorMappingDao

    companion object {
        const val DATABASE_NAME = "gamelauncher.db"
    }
}
