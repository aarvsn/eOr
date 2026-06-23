package com.gamelaunch.frontend.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "emulator_mappings",
    indices = [Index(value = ["platform_id"], unique = true)]
)
data class EmulatorMappingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "platform_id") val platformId: String,
    @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "launch_action") val launchAction: String = "android.intent.action.VIEW",
    @ColumnInfo(name = "intent_extras_json") val intentExtrasJson: String = "{}",
    @ColumnInfo(name = "is_retroarch") val isRetroArch: Boolean = false,
    @ColumnInfo(name = "retroarch_core") val retroArchCore: String? = null
)
