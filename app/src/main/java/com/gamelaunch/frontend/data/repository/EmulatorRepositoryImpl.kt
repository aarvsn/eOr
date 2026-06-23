package com.gamelaunch.frontend.data.repository

import com.gamelaunch.frontend.data.db.dao.EmulatorMappingDao
import com.gamelaunch.frontend.data.db.entity.EmulatorMappingEntity
import com.gamelaunch.frontend.domain.model.EmulatorMapping
import com.gamelaunch.frontend.domain.model.InstalledEmulator
import com.gamelaunch.frontend.domain.repository.EmulatorRepository
import com.gamelaunch.frontend.launcher.PackageManagerHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmulatorRepositoryImpl @Inject constructor(
    private val emulatorMappingDao: EmulatorMappingDao,
    private val packageManagerHelper: PackageManagerHelper
) : EmulatorRepository {

    private val gson = Gson()

    override suspend fun getMappingForPlatform(platformId: String): EmulatorMapping? =
        emulatorMappingDao.getMappingForPlatform(platformId)?.toDomain()

    override fun getAllMappings(): Flow<List<EmulatorMapping>> =
        emulatorMappingDao.getAllMappings().map { it.map(EmulatorMappingEntity::toDomain) }

    override suspend fun upsertMapping(mapping: EmulatorMapping) {
        emulatorMappingDao.upsertMapping(mapping.toEntity())
    }

    override suspend fun deleteMappingForPlatform(platformId: String) {
        emulatorMappingDao.deleteMappingForPlatform(platformId)
    }

    override fun getInstalledEmulators(): List<InstalledEmulator> =
        packageManagerHelper.getInstalledEmulators()

    private fun EmulatorMappingEntity.toDomain(): EmulatorMapping {
        val extrasType = object : TypeToken<Map<String, String>>() {}.type
        val extras: Map<String, String> = runCatching {
            gson.fromJson(intentExtrasJson, extrasType) ?: emptyMap()
        }.getOrDefault(emptyMap())

        return EmulatorMapping(
            id = id,
            platformId = platformId,
            packageName = packageName,
            launchAction = launchAction,
            intentExtras = extras,
            isRetroArch = isRetroArch,
            retroArchCore = retroArchCore
        )
    }

    private fun EmulatorMapping.toEntity() = EmulatorMappingEntity(
        id = id,
        platformId = platformId,
        packageName = packageName,
        launchAction = launchAction,
        intentExtrasJson = gson.toJson(intentExtras),
        isRetroArch = isRetroArch,
        retroArchCore = retroArchCore
    )
}
