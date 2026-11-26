package com.charles.jurysim.data.llm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.charles.jurysim.JurySimApp
import com.charles.jurysim.util.Constants

/**
 * Downloads the 3.65 GB LiteRT-LM model as a WorkManager foreground
 * service (type: dataSync).
 *
 * This is what makes the manifest's FOREGROUND_SERVICE_DATA_SYNC
 * permission real: the download keeps running with a visible progress
 * notification even if the user leaves the app or the process is
 * reclaimed, instead of dying with a viewModelScope coroutine.
 *
 * Progress is published through WorkManager progress data
 * ([KEY_BYTES_DOWNLOADED] / [KEY_TOTAL_BYTES]); the onboarding UI
 * observes the unique work and maps it back onto [DownloadState].
 */
class ModelDownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val app = applicationContext as JurySimApp

    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_URL) ?: Constants.LITERTLM_MODEL_URL

        createNotificationChannel()
        setForeground(foregroundInfo(percent = 0))

        var failureMessage: String? = null
        var lastPercent = 0

        app.modelDownloader.download(url).collect { state ->
            when (state) {
                is DownloadState.Downloading -> {
                    val percent = (state.fraction * 100).toInt().coerceIn(0, 100)
                    setProgress(
                        Data.Builder()
                            .putLong(KEY_BYTES_DOWNLOADED, state.bytesDownloaded)
                            .putLong(KEY_TOTAL_BYTES, state.totalBytes)
                            .build()
                    )
                    if (percent != lastPercent) {
                        lastPercent = percent
                        setForeground(foregroundInfo(percent))
                    }
                }
                is DownloadState.Verifying -> {
                    setForeground(foregroundInfo(percent = 100, verifying = true))
                }
                is DownloadState.Failed -> {
                    failureMessage = state.message
                }
                else -> Unit
            }
        }

        return if (failureMessage == null) {
            Result.success()
        } else {
            Result.failure(
                Data.Builder()
                    .putString(KEY_ERROR, failureMessage)
                    .build()
            )
        }
    }

    private fun foregroundInfo(percent: Int, verifying: Boolean = false): ForegroundInfo {
        val text = if (verifying) {
            "Verifying download…"
        } else {
            "Downloading the on-device AI model — $percent%"
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("${Constants.LITERTLM_MODEL_DISPLAY_NAME} download")
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, percent, percent == 0 && !verifying)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Model download",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Progress of the one-time AI model download"
                }
            )
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "model-download"
        const val KEY_URL = "url"
        const val KEY_BYTES_DOWNLOADED = "bytes_downloaded"
        const val KEY_TOTAL_BYTES = "total_bytes"
        const val KEY_ERROR = "error"

        private const val CHANNEL_ID = "model_download"
        private const val NOTIFICATION_ID = 1001
    }
}
