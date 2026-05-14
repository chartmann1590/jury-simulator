package com.charles.jurysim.util

object Constants {
    const val TOTAL_PROSECUTION_WITNESSES = 2
    const val TOTAL_DEFENSE_WITNESSES = 2

    // On-device LLM (Gemma 4 E4B IT in LiteRT-LM format).
    // Default URL points at the public Hugging Face mirror — Apache 2.0, non-gated, no token required.
    // To switch to a private CDN, change LITERTLM_MODEL_URL only.
    const val LITERTLM_MODEL_URL =
        "https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm"
    const val LITERTLM_MODEL_FILENAME = "gemma-4-E4B-it.litertlm"
    const val LITERTLM_MODEL_SIZE_BYTES = 3_659_530_240L
    const val LITERTLM_MODEL_DISPLAY_NAME = "Gemma 4 E4B IT"

    // Optional SHA-256 checksum for integrity verification. Leave blank to skip.
    const val LITERTLM_MODEL_SHA256 = ""

    // Disk-space pre-check headroom (model size + ~1 GB safety margin).
    const val LITERTLM_MIN_FREE_SPACE_BYTES = 4_500_000_000L

    // Subdirectory under filesDir/ where downloaded models live.
    const val MODELS_DIR = "models"
}
