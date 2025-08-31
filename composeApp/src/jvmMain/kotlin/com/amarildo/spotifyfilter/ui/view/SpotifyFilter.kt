package com.amarildo.spotifyfilter.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amarildo.spotifyfilter.ui.theme.SpotifyFilterTheme
import com.amarildo.spotifyfilter.viewmodel.MyViewModel
import com.amarildo.spotifyfilter.viewmodel.ProcessStep
import com.amarildo.spotifyfilter.viewmodel.UiState
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Database
import com.composables.icons.lucide.Delete
import com.composables.icons.lucide.ExternalLink
import com.composables.icons.lucide.FolderInput
import com.composables.icons.lucide.Key
import com.composables.icons.lucide.Link
import com.composables.icons.lucide.Loader
import com.composables.icons.lucide.LoaderCircle
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.RotateCcw
import com.composables.icons.lucide.Settings
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun SpotifyFilter(vm: MyViewModel = viewModel()) {
    SpotifyFilterTheme {
        val scope = rememberCoroutineScope()
        val uiState by vm.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        var isPathLoading by remember { mutableStateOf(false) }

        LaunchedEffect(uiState.successMessage, uiState.error) {
            uiState.successMessage?.let { msg ->
                snackbarHostState.showSnackbar(
                    message = msg,
                    duration = SnackbarDuration.Short,
                )
                vm.clearMessage()
            }
            uiState.error?.let { error ->
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long,
                )
                vm.clearMessage()
            }
        }

        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = if (data.visuals.message.contains("Error", ignoreCase = true)) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        contentColor = if (data.visuals.message.contains("Error", ignoreCase = true)) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                    )
                }
            },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item { HeaderCard(uiState.currentStep) }

                item { ConfigurationFileInput(uiState, vm, isPathLoading) { isPathLoading = it } }

                if (uiState.currentStep == ProcessStep.TokenReceived) {
                    item {
                        AdditionalInputs(uiState, vm, isPathLoading) { isPathLoading = it }
                    }
                }

                item { ActionButton(uiState, vm) }

                if (uiState.currentStep == ProcessStep.Completed) {
                    item { CompletionCard(vm) }
                }
            }
        }
    }
}

@Composable
private fun HeaderCard(currentStep: ProcessStep) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(verticalAlignment = CenterVertically) {
                Icon(
                    imageVector = when (currentStep) {
                        ProcessStep.Initial -> Lucide.Settings
                        ProcessStep.ConfigurationLoaded -> Lucide.Settings
                        ProcessStep.TokenReceived -> Lucide.Key
                        ProcessStep.Processing -> Lucide.Loader
                        ProcessStep.Completed -> Lucide.Check
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentStep) {
                        ProcessStep.Initial -> "Configuration Setup"
                        ProcessStep.ConfigurationLoaded -> "Authorization Required"
                        ProcessStep.TokenReceived -> "Final Configuration"
                        ProcessStep.Processing -> "Processing..."
                        ProcessStep.Completed -> "Process Complete"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (currentStep) {
                    ProcessStep.Initial -> "Select your configuration file to begin"
                    ProcessStep.ConfigurationLoaded -> "Configuration loaded. Click to get authorization URL"
                    ProcessStep.TokenReceived -> "Complete the setup with database file and authorization URL"
                    ProcessStep.Processing -> "Processing your playlist filters..."
                    ProcessStep.Completed -> "All done! Your playlists have been processed."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun ConfigurationFileInput(
    uiState: UiState,
    vm: MyViewModel,
    isPathLoading: Boolean,
    setPathLoading: (Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()

    EnhancedInputRow(
        label = "Configuration File Path",
        value = uiState.configurationFilePath,
        onValueChange = vm::updateConfigurationPath,
        placeholder = "Select the configuration file...",
        isLoading = isPathLoading,
        onFocusAction = {
            scope.launch {
                setPathLoading(true)
                try {
                    val selectedPath = vm.selectFile()
                    if (selectedPath.isNotEmpty()) {
                        vm.updateConfigurationPath(selectedPath)
                    }
                } finally {
                    setPathLoading(false)
                }
            }
        },
        leadingIcon = {
            Icon(
                imageVector = Lucide.FolderInput,
                contentDescription = "Configuration File",
                tint = MaterialTheme.colorScheme.primary,
            )
        },
    )
}

@Composable
private fun AdditionalInputs(
    uiState: UiState,
    vm: MyViewModel,
    isPathLoading: Boolean,
    setPathLoading: (Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        EnhancedInputRow(
            label = "Database File Path",
            value = uiState.databaseFilePath,
            onValueChange = vm::updateDatabasePath,
            placeholder = "Select the database file...",
            isLoading = isPathLoading,
            onFocusAction = {
                scope.launch {
                    setPathLoading(true)
                    try {
                        val selectedPath = vm.selectFile()
                        if (selectedPath.isNotEmpty()) {
                            vm.updateDatabasePath(selectedPath)
                        }
                    } finally {
                        setPathLoading(false)
                    }
                }
            },
            leadingIcon = {
                Icon(
                    imageVector = Lucide.Database,
                    contentDescription = "Database File",
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
        )

        EnhancedInputRow(
            label = "Authorization URL",
            value = uiState.browserUrl,
            onValueChange = vm::updateBrowserUrl,
            placeholder = "Paste the authorization URL from your browser...",
            onFocusAction = {},
            leadingIcon = {
                Icon(
                    imageVector = Lucide.Link,
                    contentDescription = "Authorization URL",
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
        )
    }
}

@Composable
private fun ActionButton(uiState: UiState, vm: MyViewModel) {
    Column {
        Button(
            onClick = { vm.processNextStep() },
            enabled = uiState.canProceedToNext && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp,
            ),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(
                    imageVector = when (uiState.currentStep) {
                        ProcessStep.Initial -> Lucide.Settings
                        ProcessStep.ConfigurationLoaded -> Lucide.ExternalLink
                        ProcessStep.TokenReceived -> Lucide.Play
                        ProcessStep.Processing -> Lucide.LoaderCircle
                        ProcessStep.Completed -> Lucide.Check
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                uiState.nextButtonText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }

        // Validation messages
        if (!uiState.canProceedToNext && !uiState.isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            val missingFields = uiState.requiredFields
            val message = if (missingFields.isNotEmpty()) {
                "Required: ${missingFields.joinToString(", ")}"
            } else {
                "Please complete all required fields"
            }

            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CompletionCard(vm: MyViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = CenterHorizontally,
        ) {
            Icon(
                imageVector = Lucide.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Processing Complete!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Your Spotify playlists have been successfully processed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { vm.resetProcess() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                ),
            ) {
                Icon(
                    imageVector = Lucide.RotateCcw,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start New Process")
            }
        }
    }
}

@Composable
fun EnhancedInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onFocusAction: () -> Unit,
    placeholder: String = "",
    isLoading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon,
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else if (value.isNotEmpty()) {
                    IconButton(
                        onClick = { onValueChange("") },
                    ) {
                        Icon(
                            imageVector = Lucide.Delete,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && !isLoading) {
                        onFocusAction()
                    }
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
            ),
            shape = RoundedCornerShape(12.dp),
        )
    }
}
