package com.jurysim.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jurysim.data.llm.DownloadState
import com.jurysim.ui.adaptive.AdaptiveCenteredContent
import com.jurysim.util.Constants
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onReady: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showCustomUrlDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.download) {
        if (state.download is DownloadState.Ready) onReady()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set up Gemma 4") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        AdaptiveCenteredContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                HeaderCard()
                DeviceInfoCard(state)

                when (val s = state.download) {
                    DownloadState.Idle -> WelcomeActions(
                        state = state,
                        onDownload = viewModel::startDownload,
                        onCustomUrl = { showCustomUrlDialog = true }
                    )
                    DownloadState.Connecting -> StatusBlock(
                        title = "Connecting…",
                        body = "Reaching the model host."
                    )
                    is DownloadState.Downloading -> DownloadingBlock(
                        state = s,
                        onCancel = viewModel::cancelDownload
                    )
                    DownloadState.Verifying -> StatusBlock(
                        title = "Verifying download",
                        body = "Checking the model file. This can take a moment for a 3.65 GB file."
                    )
                    DownloadState.LoadingEngine -> StatusBlock(
                        title = "Loading Gemma 4",
                        body = "Initializing on-device inference (about 5 seconds)."
                    )
                    DownloadState.Ready -> StatusBlock(
                        title = "Ready",
                        body = "Model is loaded. Taking you to the home screen…",
                        icon = Icons.Default.CheckCircle
                    )
                    is DownloadState.Failed -> FailedBlock(
                        state = s,
                        onRetry = viewModel::retry,
                        onCustomUrl = { showCustomUrlDialog = true }
                    )
                }
            }
        }
    }

    if (showCustomUrlDialog) {
        CustomUrlDialog(
            initial = state.customUrl ?: "",
            onDismiss = { showCustomUrlDialog = false },
            onConfirm = { url ->
                viewModel.useCustomUrl(url)
                showCustomUrlDialog = false
            }
        )
    }
}

@Composable
private fun HeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = Constants.LITERTLM_MODEL_DISPLAY_NAME,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "On-device LLM via LiteRT-LM. Runs entirely offline once " +
                        "downloaded — no network calls, no server account.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Download size: ~3.65 GB · Apache 2.0 · Hugging Face mirror",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun DeviceInfoCard(state: OnboardingUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Memory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Device",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            InfoRow("Total RAM", formatBytes(state.totalRamBytes))
            InfoRow("Free storage", formatBytes(state.freeSpaceBytes))

            if (state.lowRamWarning) {
                Spacer(Modifier.height(8.dp))
                WarningRow("This device has limited RAM. Gemma 4 may run slowly.")
            }
            if (state.notEnoughSpace) {
                Spacer(Modifier.height(8.dp))
                WarningRow("Not enough free space — free up at least 4.5 GB.")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WarningRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun WelcomeActions(
    state: OnboardingUiState,
    onDownload: () -> Unit,
    onCustomUrl: () -> Unit
) {
    Button(
        onClick = onDownload,
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.notEnoughSpace
    ) {
        Text("Download model")
    }
    TextButton(
        onClick = onCustomUrl,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (state.customUrl == null) "Use custom URL" else "Custom URL set — change")
    }
}

@Composable
private fun StatusBlock(
    title: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(8.dp))
            } else {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
            }
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DownloadingBlock(state: DownloadState.Downloading, onCancel: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Downloading Gemma 4",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { state.fraction.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${formatBytes(state.bytesDownloaded)} / ${formatBytes(state.totalBytes)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "${formatBytes(state.bytesPerSec)}/s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pause")
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Pausing keeps your progress — resuming will continue from here.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FailedBlock(
    state: DownloadState.Failed,
    onRetry: () -> Unit,
    onCustomUrl: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Setup failed",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                state.message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(16.dp))
            if (state.retryable) {
                Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                    Text("Retry")
                }
                Spacer(Modifier.height(8.dp))
            }
            TextButton(onClick = onCustomUrl, modifier = Modifier.fillMaxWidth()) {
                Text("Use custom URL")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomUrlDialog(
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom model URL") },
        text = {
            Column {
                Text(
                    "Paste a direct HTTPS link to a Gemma 4 .litertlm file. " +
                            "Useful for self-hosted mirrors or air-gapped sideloads.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("https://…/model.litertlm") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) { Text("Use this URL") }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm("") }) { Text("Reset to default") }
        }
    )
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var idx = 0
    while (value >= 1024.0 && idx < units.lastIndex) {
        value /= 1024.0
        idx++
    }
    return String.format(Locale.US, if (idx <= 1) "%.0f %s" else "%.2f %s", value, units[idx])
}
