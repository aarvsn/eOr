package com.gamelaunch.frontend.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelaunch.frontend.domain.model.Game
import com.gamelaunch.frontend.domain.model.GameMedia
import com.gamelaunch.frontend.domain.platform.PlatformDefinitions
import com.gamelaunch.frontend.domain.repository.GameRepository
import com.gamelaunch.frontend.domain.repository.MediaRepository
import com.gamelaunch.frontend.domain.repository.SettingsRepository
import com.gamelaunch.frontend.ui.theme.LayoutMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val platforms: List<String> = emptyList(),
    val selectedPlatform: String? = null,
    val games: List<Game> = emptyList(),
    val selectedGameIndex: Int = 0,
    val selectedGameMedia: GameMedia? = null,
    val mediaForGames: Map<Long, GameMedia> = emptyMap(),
    val shouldPlayVideo: Boolean = false,
    val layoutMode: LayoutMode = LayoutMode.CAROUSEL,
    val videoMuted: Boolean = true,
    val videoDelayMs: Long = 1500L,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val mediaRepository: MediaRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private var videoDelayJob: Job? = null

    init {
        observePlatforms()
        observeSettings()
        observeAllMedia()
    }

    private fun observePlatforms() {
        viewModelScope.launch {
            gameRepository.getDistinctPlatformIds().collect { platformIds ->
                val displayNames = platformIds.map { id ->
                    PlatformDefinitions.byId[id]?.displayName ?: id.uppercase()
                }
                _uiState.update { state ->
                    state.copy(
                        platforms = platformIds,
                        selectedPlatform = state.selectedPlatform ?: platformIds.firstOrNull(),
                        isLoading = false
                    )
                }
                loadGamesForPlatform(_uiState.value.selectedPlatform)
            }
        }
    }

    private fun observeAllMedia() {
        viewModelScope.launch {
            mediaRepository.observeAllMedia().collect { mediaMap ->
                val selectedGame = _uiState.value.games.getOrNull(_uiState.value.selectedGameIndex)
                _uiState.update {
                    it.copy(
                        mediaForGames     = mediaMap,
                        selectedGameMedia = selectedGame?.let { g -> mediaMap[g.id] }
                    )
                }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.layoutMode,
                settingsRepository.videoMuted,
                settingsRepository.videoAutoplayDelayMs
            ) { layout, muted, delay ->
                Triple(layout, muted, delay)
            }.collect { (layout, muted, delay) ->
                _uiState.update { it.copy(layoutMode = layout, videoMuted = muted, videoDelayMs = delay) }
            }
        }
    }

    private var gamesJob: Job? = null

    private fun loadGamesForPlatform(platformId: String?) {
        gamesJob?.cancel()
        if (platformId == null) return
        gamesJob = viewModelScope.launch {
            gameRepository.getGamesByPlatform(platformId).collect { games ->
                val firstMedia = _uiState.value.mediaForGames[games.firstOrNull()?.id]
                _uiState.update { state ->
                    state.copy(
                        games             = games,
                        selectedGameIndex = 0,
                        shouldPlayVideo   = false,
                        selectedGameMedia = firstMedia
                    )
                }
            }
        }
    }

    fun selectPlatform(platformId: String) {
        videoDelayJob?.cancel()
        _uiState.update { it.copy(selectedPlatform = platformId, shouldPlayVideo = false) }
        loadGamesForPlatform(platformId)
    }

    fun onGameSelected(index: Int) {
        val games = _uiState.value.games
        if (index !in games.indices) return

        videoDelayJob?.cancel()
        val media = _uiState.value.mediaForGames[games[index].id]
        _uiState.update { it.copy(selectedGameIndex = index, shouldPlayVideo = false, selectedGameMedia = media) }

        videoDelayJob = viewModelScope.launch {
            delay(_uiState.value.videoDelayMs)
            _uiState.update { it.copy(shouldPlayVideo = true) }
        }
    }

    fun toggleLayoutMode() {
        val next = if (_uiState.value.layoutMode == LayoutMode.CAROUSEL) LayoutMode.GRID else LayoutMode.CAROUSEL
        viewModelScope.launch { settingsRepository.setLayoutMode(next) }
    }

    fun toggleMute() {
        _uiState.update { it.copy(videoMuted = !it.videoMuted) }
        viewModelScope.launch {
            settingsRepository.setVideoMuted(_uiState.value.videoMuted)
        }
    }
}
