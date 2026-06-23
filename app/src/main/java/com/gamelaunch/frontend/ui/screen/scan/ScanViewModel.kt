package com.gamelaunch.frontend.ui.screen.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.frontend.domain.repository.SettingsRepository
import com.gamelaunch.frontend.domain.usecase.ScanProgress
import com.gamelaunch.frontend.domain.usecase.ScanRomsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanUiState(
    val isScanning: Boolean = false,
    val progress: ScanProgress? = null,
    val isComplete: Boolean = false,
    val errorMessage: String? = null,
    val romRootPath: String = ""
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanRomsUseCase: ScanRomsUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState

    init {
        viewModelScope.launch {
            val path = settingsRepository.romRootPath.firstOrNull() ?: ""
            _uiState.update { it.copy(romRootPath = path) }
        }
    }

    fun startScan(path: String? = null) {
        viewModelScope.launch {
            val romPath = path ?: settingsRepository.romRootPath.firstOrNull() ?: ""
            if (romPath.isBlank()) {
                _uiState.update { it.copy(errorMessage = "ROM folder not configured. Go to Settings first.") }
                return@launch
            }

            _uiState.update { it.copy(isScanning = true, isComplete = false, errorMessage = null) }

            scanRomsUseCase(romPath).collect { progress ->
                _uiState.update { it.copy(progress = progress) }
            }

            _uiState.update { it.copy(isScanning = false, isComplete = true) }
        }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }
}
