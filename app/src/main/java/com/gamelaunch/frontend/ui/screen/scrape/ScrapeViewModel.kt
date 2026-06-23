package com.gamelaunch.frontend.ui.screen.scrape

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.frontend.domain.usecase.BatchScrapeState
import com.gamelaunch.frontend.domain.usecase.BatchScrapeUseCase
import com.gamelaunch.frontend.domain.usecase.ScrapeResult
import com.gamelaunch.frontend.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScrapeUiState(
    val batchState: BatchScrapeState? = null,
    val isRunning: Boolean = false,
    val isConfigured: Boolean = false
)

@HiltViewModel
class ScrapeViewModel @Inject constructor(
    private val batchScrapeUseCase: BatchScrapeUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScrapeUiState())
    val uiState: StateFlow<ScrapeUiState> = _uiState

    private var scrapeJob: Job? = null

    init {
        viewModelScope.launch {
            val config = settingsRepository.scraperConfig.firstOrNull()
            _uiState.update { it.copy(isConfigured = config?.isConfigured == true) }
        }
    }

    fun startScrape() {
        viewModelScope.launch {
            val config = settingsRepository.scraperConfig.firstOrNull() ?: return@launch
            if (!config.isConfigured) return@launch

            _uiState.update { it.copy(isRunning = true) }
            scrapeJob = launch {
                batchScrapeUseCase(config).collect { state ->
                    _uiState.update { it.copy(batchState = state) }
                }
            }
            scrapeJob?.join()
            _uiState.update { it.copy(isRunning = false) }
        }
    }

    fun cancelScrape() {
        scrapeJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    override fun onCleared() {
        super.onCleared()
        scrapeJob?.cancel()
    }
}
