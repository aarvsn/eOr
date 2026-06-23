package com.gamelaunch.frontend.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.frontend.domain.model.EmulatorMapping
import com.gamelaunch.frontend.domain.model.InstalledEmulator
import com.gamelaunch.frontend.domain.repository.EmulatorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmulatorConfigUiState(
    val mappings: Map<String, EmulatorMapping> = emptyMap(),
    val installedEmulators: List<InstalledEmulator> = emptyList()
)

@HiltViewModel
class EmulatorConfigViewModel @Inject constructor(
    private val emulatorRepository: EmulatorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmulatorConfigUiState())
    val uiState: StateFlow<EmulatorConfigUiState> = _uiState

    init {
        val installed = emulatorRepository.getInstalledEmulators()
        _uiState.update { it.copy(installedEmulators = installed) }

        viewModelScope.launch {
            emulatorRepository.getAllMappings().collect { mappings ->
                _uiState.update { state ->
                    state.copy(mappings = mappings.associateBy { it.platformId })
                }
            }
        }
    }

    fun upsertMapping(mapping: EmulatorMapping) {
        viewModelScope.launch {
            emulatorRepository.upsertMapping(mapping)
        }
    }
}
