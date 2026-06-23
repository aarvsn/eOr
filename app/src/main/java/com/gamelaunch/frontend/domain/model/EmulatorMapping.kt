package com.gamelaunch.frontend.domain.model

import android.content.Intent

data class EmulatorMapping(
    val id: Long = 0,
    val platformId: String,
    val packageName: String,
    val launchAction: String = Intent.ACTION_VIEW,
    val intentExtras: Map<String, String> = emptyMap(),
    val isRetroArch: Boolean = false,
    val retroArchCore: String? = null
)
