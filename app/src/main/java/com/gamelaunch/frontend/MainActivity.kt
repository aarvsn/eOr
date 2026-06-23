package com.gamelaunch.frontend

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.gamelaunch.frontend.domain.repository.SettingsRepository
import com.gamelaunch.frontend.ui.navigation.AppNavGraph
import com.gamelaunch.frontend.ui.navigation.Screen
import com.gamelaunch.frontend.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* storage permissions handled — ROM folder picker and all-files access are the fallback */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestStoragePermissions()
        requestAllFilesAccessIfNeeded()

        setContent {
            AppTheme {
                val navController = rememberNavController()
                val isFirstLaunch by settingsRepository.isFirstLaunch.collectAsState(initial = true)
                val startDestination = if (isFirstLaunch) Screen.Settings.route else Screen.Home.route

                AppNavGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Swallow Back at the launcher root — sub-screens handle it via the nav stack
    }

    private fun requestStoragePermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions)
    }

    /**
     * On Android 11+ (API 30), direct file access to SD cards requires
     * MANAGE_EXTERNAL_STORAGE ("All files access"). We send the user to the
     * system settings page once if it hasn't been granted yet.
     */
    private fun requestAllFilesAccessIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            !Environment.isExternalStorageManager()
        ) {
            runCatching {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                )
            }.onFailure {
                // Fallback for devices that don't support the per-app page
                runCatching {
                    startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                }
            }
        }
    }
}
