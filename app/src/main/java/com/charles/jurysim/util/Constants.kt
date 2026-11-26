package com.charles.jurysim.util

object Constants {
    const val TOTAL_PROSECUTION_WITNESSES = 2
    const val TOTAL_DEFENSE_WITNESSES = 2

    // On-device LLM (Gemma 4 E4B IT in LiteRT-LM format).
    // The default URL is PINNED to a specific Hugging Face commit
    // (f254b5ba…) — never point this at a mutable branch like
    // resolve/main: the file served there can change at any time, which
    // would silently change the model every user downloads. To move to
    // a newer model revision, update the commit, size, and SHA-256
    // together (the SHA-256 is the file's LFS oid, also verifiable by
    // hashing a downloaded copy).
    const val LITERTLM_MODEL_URL =
        "https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/f254b5ba6e36f7d89cc7892e268e9877bc20f0a0/gemma-4-E4B-it.litertlm"
    const val LITERTLM_MODEL_FILENAME = "gemma-4-E4B-it.litertlm"
    const val LITERTLM_MODEL_SIZE_BYTES = 3_659_530_240L
    const val LITERTLM_MODEL_DISPLAY_NAME = "Gemma 4 E4B IT"

    // SHA-256 of the pinned model file. Verification is MANDATORY and
    // fails closed: ModelDownloader refuses to accept a download whose
    // hash does not match. There is deliberately no "blank to skip"
    // option anymore.
    const val LITERTLM_MODEL_SHA256 =
        "0b2a8980ce155fd97673d8e820b4d29d9c7d99b8fa6806f425d969b145bd52e0"

    // Smallest plausible size for a user-supplied custom model file.
    // Custom URLs cannot be hash-checked against a known value, so this
    // sanity floor plus a successful engine load is the integrity gate.
    const val CUSTOM_MODEL_MIN_SIZE_BYTES = 1_000_000_000L

    // Disk-space pre-check headroom (model size + ~1 GB safety margin).
    const val LITERTLM_MIN_FREE_SPACE_BYTES = 4_500_000_000L

    // Subdirectory under filesDir/ where downloaded models live.
    const val MODELS_DIR = "models"

    // Shown in onboarding, Settings > About, and the Play listing.
    const val LEGAL_DISCLAIMER =
        "Jury Simulator is a work of fiction for entertainment and education. " +
            "All cases, people and verdicts are AI-generated and fictional. " +
            "It is not legal advice and does not simulate any real case or court."
}
