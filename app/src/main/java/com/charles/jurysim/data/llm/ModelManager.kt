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

    /**
     * Returns true if the fully-downloaded model file exists and has a
     * plausible size. We don't enforce an exact byte count because the HF
     * canonical filename / size could shift across model card revisions.
     */
    fun isModelDownloaded(): Boolean {
        if (!modelFile.exists()) return false
        // Sanity check: at least 1 GB. Anything smaller is corrupt / aborted.
        return modelFile.length() >= 1_000_000_000L
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

    /** Removes both the finished and partial files. */
    fun deleteAll() {
        if (modelFile.exists()) modelFile.delete()
        if (partFile.exists()) partFile.delete()
    }

    fun modelPath(): String = modelFile.absolutePath
}
