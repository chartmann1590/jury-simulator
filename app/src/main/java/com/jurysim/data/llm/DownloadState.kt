package com.jurysim.data.llm

/**
 * Discrete states that drive the onboarding UI. The contract is one-way: once
 * we reach [Ready] the screen finishes; failures land in [Failed] and require
 * an explicit user retry.
 */
sealed class DownloadState {

    /** No download in progress. Initial state when the app launches without a model. */
    data object Idle : DownloadState()

    /** Reading manifest / opening connection / resuming partial. */
    data object Connecting : DownloadState()

    /**
     * Bytes streaming. [bytesDownloaded] / [totalBytes] drives the progress
     * bar. [bytesPerSec] is a smoothed throughput estimate for the UI.
     */
    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val bytesPerSec: Long
    ) : DownloadState() {
        val fraction: Float
            get() = if (totalBytes <= 0L) 0f else bytesDownloaded.toFloat() / totalBytes.toFloat()
    }

    /** SHA-256 verifying the .part file. May take several seconds for 3.6 GB. */
    data object Verifying : DownloadState()

    /** Loading weights into LiteRT-LM. Takes a few seconds on first launch. */
    data object LoadingEngine : DownloadState()

    /** Model is on disk and the engine is initialized. */
    data object Ready : DownloadState()

    /** Terminal error state. [message] is shown to the user; [retryable] gates the Retry button. */
    data class Failed(val message: String, val retryable: Boolean = true) : DownloadState()
}
