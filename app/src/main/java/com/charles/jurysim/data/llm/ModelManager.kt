package com.charles.jurysim.data.llm

import android.content.Context
import com.charles.jurysim.util.Constants
import java.io.File

/**
 * Owns the on-disk location of the downloaded LiteRT-LM model and the
 * associated free-space / existence checks. Pure file-system bookkeeping;
 * actual download streaming lives in [ModelDownloader].
 */
class ModelManager(context: Context) {

    private val appContext = context.applicationContext

    val modelsDir: File = File(appContext.filesDir, Constants.MODELS_DIR).apply {
        if (!exists()) mkdirs()
    }

    val modelFile: File = File(modelsDir, Constants.LITERTLM_MODEL_FILENAME)

    /** Partial download lives here until verification succeeds. */
    val partFile: File = File(modelsDir, "${Constants.LITERTLM_MODEL_FILENAME}.part")

    /** Records which URL the on-disk model came from (default vs custom). */
    private val sourceMarkerFile: File = File(modelsDir, "model-source.txt")

    /** The URL the current on-disk model was downloaded from, if known. */
    fun sourceUrl(): String? =
        if (sourceMarkerFile.exists()) {
            sourceMarkerFile.readText().trim().ifEmpty { null }
        } else {
            null
        }

    /** Persists [url] as the source of the on-disk model. Called only
     *  after a download has passed verification. */
    fun recordSource(url: String) {
        sourceMarkerFile.writeText(url)
    }

    /**
     * Returns true if the fully-downloaded model file exists and is the
     * file we expect:
     * - Default model: EXACT byte-for-byte size match against the pinned
     *   revision (the SHA-256 was already verified at download time).
     * - Custom model (user-supplied URL): at least the sanity floor —
     *   there is no known-good size/hash for arbitrary user models.
     * - Legacy installs with no source marker: exact size match, since
     *   only the default model was ever shipped to them.
     */
    fun isModelDownloaded(): Boolean {
        if (!modelFile.exists()) return false
        val size = modelFile.length()
        val source = sourceUrl()
        return if (source == null || source == Constants.LITERTLM_MODEL_URL) {
            size == Constants.LITERTLM_MODEL_SIZE_BYTES
        } else {
            size >= Constants.CUSTOM_MODEL_MIN_SIZE_BYTES
        }
    }

    fun modelSizeOnDiskBytes(): Long = if (modelFile.exists()) modelFile.length() else 0L

    /**
     * True iff there is enough usable space in `filesDir` to hold the full
     * download plus a safety headroom.
     */
    fun hasFreeSpace(): Boolean =
        appContext.filesDir.usableSpace >= Constants.LITERTLM_MIN_FREE_SPACE_BYTES

    /** Bytes needed beyond what's already on disk (download still to do). */
    fun bytesStillNeeded(): Long {
        val have = if (partFile.exists()) partFile.length() else modelSizeOnDiskBytes()
        return (Constants.LITERTLM_MODEL_SIZE_BYTES - have).coerceAtLeast(0L)
    }

    /** Removes the finished file, the partial file, and the source marker. */
    fun deleteAll() {
        if (modelFile.exists()) modelFile.delete()
        if (partFile.exists()) partFile.delete()
        if (sourceMarkerFile.exists()) sourceMarkerFile.delete()
    }

    fun modelPath(): String = modelFile.absolutePath
}
