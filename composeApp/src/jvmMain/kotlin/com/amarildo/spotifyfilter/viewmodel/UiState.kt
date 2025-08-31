package com.amarildo.spotifyfilter.viewmodel

sealed class ProcessStep {
    object Initial : ProcessStep()
    object ConfigurationLoaded : ProcessStep()
    object TokenReceived : ProcessStep()
    object Processing : ProcessStep()
    object Completed : ProcessStep()
}

data class UiState(
    val currentStep: ProcessStep = ProcessStep.Initial,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null,
    val configurationFilePath: String = "",
    val databaseFilePath: String = "",
    val browserUrl: String = "",
) {
    val canProceedToNext: Boolean
        get() = when (currentStep) {
            ProcessStep.Initial -> configurationFilePath.isNotBlank()
            ProcessStep.ConfigurationLoaded -> true // Automatico dopo il caricamento config
            ProcessStep.TokenReceived -> databaseFilePath.isNotBlank() && browserUrl.isNotBlank()
            ProcessStep.Processing -> false
            ProcessStep.Completed -> false
        }

    val nextButtonText: String
        get() = when (currentStep) {
            ProcessStep.Initial -> "Load Configuration"
            ProcessStep.ConfigurationLoaded -> "Get Authorization"
            ProcessStep.TokenReceived -> "Process Playlist"
            ProcessStep.Processing -> "Processing..."
            ProcessStep.Completed -> "Completed"
        }

    val requiredFields: List<String>
        get() = when (currentStep) {
            ProcessStep.Initial -> listOf("Configuration File")
            ProcessStep.ConfigurationLoaded -> listOf("Database File", "Browser URL")
            else -> emptyList()
        }
}
