package com.amarildo.spotifyfilter.viewmodel

import androidx.lifecycle.ViewModel
import com.amarildo.spotifyfilter.data.repository.FileStorageRepository
import com.amarildo.spotifyfilter.service.FileLocator
import com.amarildo.spotifyfilter.service.PlaylistService
import com.amarildo.spotifyfilter.service.PropertiesLoader
import com.amarildo.spotifyfilter.service.SpotifyHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.io.IOException
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

        try {
            fileStorageRepository = FileStorageRepository(currentState.databaseFilePath)
            val tokenApi: SpotifyApi = spotifyHandler?.getTokenApi(currentState.browserUrl)
                ?: throw IllegalStateException("Spotify handler not initialized")

            val blockPlaylistId: String = properties[PropertiesLoader.SPOTIFY_PLAYLIST_BLOCK]
                ?: throw IllegalStateException("Block playlist ID not found")
            val listenPlaylistId: String = properties[PropertiesLoader.SPOTIFY_PLAYLIST_LISTEN]
                ?: throw IllegalStateException("Listen playlist ID not found")

            val result: String = PlaylistService(tokenApi, fileStorageRepository)
                .run(blockPlaylistId, listenPlaylistId)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentStep = ProcessStep.Completed,
                successMessage = result,
            )
        } catch (e: IllegalStateException) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentStep = ProcessStep.TokenReceived, // Torna indietro per permettere di correggere
                error = "Configuration error: ${e.message}",
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentStep = ProcessStep.TokenReceived, // Torna indietro per permettere di riprovare
                error = "Processing error: ${e.message}",
            )
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
