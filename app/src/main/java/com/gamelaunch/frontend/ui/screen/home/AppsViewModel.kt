package com.gamelaunch.frontend.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.frontend.domain.model.InstalledApp
import com.gamelaunch.frontend.launcher.PackageManagerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AppsUiState(
    val apps: List<InstalledApp> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AppsViewModel @Inject constructor(
    val packageManagerHelper: PackageManagerHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) { packageManagerHelper.getInstalledApps() }
            _uiState.value = AppsUiState(apps = apps, isLoading = false)
        }
    }

    fun launchApp(packageName: String) {
        packageManagerHelper.launchApp(packageName)
    }
}
