package com.amarildoaliaj.spotifyfreshorcry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amarildoaliaj.spotifyfreshorcry.data.repository.FileStorageRepository
import com.amarildoaliaj.spotifyfreshorcry.service.FileLocator
import com.amarildoaliaj.spotifyfreshorcry.service.PlaylistService
import com.amarildoaliaj.spotifyfreshorcry.service.PropertiesLoader
import com.amarildoaliaj.spotifyfreshorcry.service.SpotifyHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.michaelthelin.spotify.SpotifyApi

class MyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private lateinit var properties: Map<String, String>
    private lateinit var fileStorageRepository: FileStorageRepository
    private var spotifyHandler: SpotifyHandler? = null

    suspend fun selectFile(): String {
        val result: Result<String> = FileLocator().selectFile()
        return try {
            result.getOrThrow()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Unexpected error: ${e.message}",
            )
            ""
        }
    }

    fun updateConfigurationPath(path: String) {
        _uiState.value = _uiState.value.copy(
            configurationFilePath = path,
            currentStep = if (path.isNotBlank()) {
                ProcessStep.Initial
            } else {
                ProcessStep.Initial
            },
        )
    }

    fun updateDatabasePath(path: String) {
        _uiState.value = _uiState.value.copy(databaseFilePath = path)
    }

    fun updateBrowserUrl(url: String) {
        _uiState.value = _uiState.value.copy(browserUrl = url)
    }

    fun processNextStep() {
        val currentState = _uiState.value

        when (currentState.currentStep) {
            ProcessStep.Initial -> loadConfiguration()

            ProcessStep.ConfigurationLoaded -> getBrowserAuthorization()

            ProcessStep.TokenReceived -> finalizePlaylist()

            else -> {
                // no action needed for Processing/Completed
            }
        }
    }

    private fun loadConfiguration() {
        val configPath = _uiState.value.configurationFilePath
        if (configPath.isBlank()) return

        _uiState.value = _uiState.value.copy(isLoading = true)

        try {
            properties = PropertiesLoader(configPath).load()
            spotifyHandler = SpotifyHandler(
                properties[PropertiesLoader.SPOTIFY_CLIENT_ID],
                properties[PropertiesLoader.SPOTIFY_CLIENT_SECRET],
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentStep = ProcessStep.ConfigurationLoaded,
                successMessage = "Configuration loaded successfully",
            )
        } catch (e: IllegalStateException) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Configuration error: ${e.message}",
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Unexpected error: ${e.message}",
            )
        }
    }

    private fun getBrowserAuthorization() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        try {
            spotifyHandler?.openLinkOnBrowser()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentStep = ProcessStep.TokenReceived,
                successMessage = "Authorization URL opened. Please complete authentication and paste the URL.",
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Authorization error: ${e.message}",
            )
        }
    }

    private fun finalizePlaylist() {
        val currentState = _uiState.value
        if (currentState.databaseFilePath.isBlank() || currentState.browserUrl.isBlank()) {
            _uiState.value = currentState.copy(
                error = "Please provide both database file path and browser URL",
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            currentStep = ProcessStep.Processing,
        )

        // esegui l'operazione in una coroutine per non bloccare la UI
        viewModelScope.launch {
            try {
                // Operazioni che potrebbero essere pesanti, eseguite in background
                val result = withContext(Dispatchers.IO) {
                    fileStorageRepository = FileStorageRepository(currentState.databaseFilePath)
                    val tokenApi: SpotifyApi = spotifyHandler?.getTokenApi(currentState.browserUrl)
                        ?: throw IllegalStateException("Spotify handler not initialized")

                    val blockPlaylistId: String = properties[PropertiesLoader.SPOTIFY_PLAYLIST_BLOCK]
                        ?: throw IllegalStateException("Block playlist ID not found")
                    val listenPlaylistId: String = properties[PropertiesLoader.SPOTIFY_PLAYLIST_LISTEN]
                        ?: throw IllegalStateException("Listen playlist ID not found")

                    PlaylistService(tokenApi, fileStorageRepository)
                        .run(blockPlaylistId, listenPlaylistId)
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentStep = ProcessStep.Completed,
                    successMessage = result,
                )
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentStep = ProcessStep.TokenReceived, // Torna indietro per permettere di correggere
                    error = "Configuration error: ${e.message}",
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentStep = ProcessStep.TokenReceived, // Torna indietro per permettere di riprovare
                    error = "Processing error: ${e.message}",
                )
            }
        }
    }

    fun resetProcess() {
        _uiState.value = UiState()
        spotifyHandler = null
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            error = null,
        )
    }
}
