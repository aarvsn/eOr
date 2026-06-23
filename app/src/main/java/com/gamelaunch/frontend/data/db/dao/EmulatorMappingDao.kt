package com.gamelaunch.frontend.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gamelaunch.frontend.data.db.entity.EmulatorMappingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmulatorMappingDao {

    @Query("SELECT * FROM emulator_mappings WHERE platform_id = :platformId LIMIT 1")
    suspend fun getMappingForPlatform(platformId: String): EmulatorMappingEntity?

    @Query("SELECT * FROM emulator_mappings ORDER BY platform_id ASC")
    fun getAllMappings(): Flow<List<EmulatorMappingEntity>>

    @Upsert
    suspend fun upsertMapping(entity: EmulatorMappingEntity)

    @Query("DELETE FROM emulator_mappings WHERE platform_id = :platformId")
    suspend fun deleteMappingForPlatform(platformId: String)
}
