package com.charles.jurysim.data.llm

import com.charles.jurysim.util.Constants
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Streams the LiteRT-LM model file to disk with HTTP `Range` resume support
 * and progress reporting. Verifies SHA-256 if a non-empty hash is configured.
 *
 * The download writes to `<model>.litertlm.part`; only on a successful verify
 * is it atomically renamed to its final filename. This way an interrupted
 * download never leaves a corrupt-but-final-named model on disk.
 */
class ModelDownloader(private val modelManager: ModelManager) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        // No call timeout — a 3.65 GB download on slow connections can take >30 min.
        .callTimeout(0, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Downloads [url] to `modelManager.partFile`, emitting [DownloadState]
     * progress updates. The flow terminates with [DownloadState.Ready] or
     * [DownloadState.Failed].
     *
     * Cancelling the collecting coroutine pauses the download — the .part
     * file is left in place so the next call resumes from where we stopped.
     */
    fun download(url: String = Constants.LITERTLM_MODEL_URL): Flow<DownloadState> = flow {
        if (!modelManager.hasFreeSpace()) {
            emit(
                DownloadState.Failed(
                    "Not enough free storage. Free up at least 4.5 GB and try again.",
                    retryable = false
                )
            )
            return@flow
        }

        emit(DownloadState.Connecting)

        val partFile = modelManager.partFile
        val existingBytes = if (partFile.exists()) partFile.length() else 0L

        val requestBuilder = Request.Builder().url(url)
        if (existingBytes > 0L) {
            requestBuilder.addHeader("Range", "bytes=$existingBytes-")
        }

        val response = try {
            client.newCall(requestBuilder.build()).execute()
        } catch (e: IOException) {
            emit(DownloadState.Failed(e.message ?: "Network error"))
            return@flow
        }

        response.use { resp ->
            if (!resp.isSuccessful) {
                emit(DownloadState.Failed("HTTP ${resp.code}: ${resp.message}"))
                return@flow
            }

            // Total size = existing partial + remaining content length.
            val contentLength = resp.body?.contentLength() ?: -1L
            val totalBytes = if (contentLength > 0L) existingBytes + contentLength
                             else Constants.LITERTLM_MODEL_SIZE_BYTES
            val source = resp.body?.source() ?: run {
                emit(DownloadState.Failed("Empty response body"))
                return@flow
            }

            val raf = RandomAccessFile(partFile, "rw")
            try {
                raf.seek(existingBytes)
                val buffer = ByteArray(64 * 1024)
                var written = existingBytes
                var lastEmitNanos = System.nanoTime()
                var lastEmitBytes = existingBytes

                emit(
                    DownloadState.Downloading(
                        bytesDownloaded = written,
                        totalBytes = totalBytes,
                        bytesPerSec = 0L
                    )
                )

                while (true) {
                    val read = source.read(buffer)
                    if (read == -1) break
                    raf.write(buffer, 0, read)
                    written += read

                    val now = System.nanoTime()
                    val elapsedNanos = now - lastEmitNanos
                    if (elapsedNanos >= 250_000_000L) { // 250 ms
                        val deltaBytes = written - lastEmitBytes
                        val bps = if (elapsedNanos > 0) {
                            (deltaBytes * 1_000_000_000L) / elapsedNanos
                        } else 0L
                        emit(
                            DownloadState.Downloading(
                                bytesDownloaded = written,
                                totalBytes = totalBytes,
                                bytesPerSec = bps
                            )
                        )
                        lastEmitNanos = now
                        lastEmitBytes = written
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: IOException) {
                emit(DownloadState.Failed(e.message ?: "Download failed"))
                return@flow
            } finally {
                runCatching { raf.close() }
            }
        }

        emit(DownloadState.Verifying)

        val expectedSha = Constants.LITERTLM_MODEL_SHA256.trim()
        if (expectedSha.isNotEmpty()) {
            val actual = sha256(modelManager.partFile)
            if (!actual.equals(expectedSha, ignoreCase = true)) {
                modelManager.partFile.delete()
                emit(DownloadState.Failed("Model integrity check failed (SHA-256 mismatch)."))
                return@flow
            }
        }

        // Atomic rename: only commit to the canonical filename once verified.
        val renamed = modelManager.partFile.renameTo(modelManager.modelFile)
        if (!renamed) {
            emit(DownloadState.Failed("Failed to finalize model file."))
            return@flow
        }

        emit(DownloadState.Ready)
    }.flowOn(Dispatchers.IO)

    private fun sha256(file: java.io.File): String {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val n = input.read(buffer)
                if (n == -1) break
                md.update(buffer, 0, n)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
