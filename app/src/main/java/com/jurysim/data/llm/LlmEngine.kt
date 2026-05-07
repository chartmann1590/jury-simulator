package com.jurysim.data.llm

/**
 * Abstraction over an on-device LLM. The current production implementation is
 * [LiteRtLlmEngine] backed by `com.google.ai.edge.litertlm.Engine`.
 *
 * Implementations are expected to be safe for concurrent callers — they may
 * serialize [generate] internally if the underlying engine is single-threaded.
 */
interface LlmEngine {

    /**
     * Eagerly load weights into memory if not already loaded. Idempotent.
     * Safe to call from any dispatcher; expensive work is moved to IO inside.
     */
    suspend fun ensureLoaded()

    /**
     * Single-shot generation. Returns the full completion as a string, or a
     * failure if the model is unavailable / generation throws.
     */
    suspend fun generate(prompt: String): Result<String>

    /**
     * Release native resources. After calling this, [ensureLoaded] must be
     * invoked again before the next [generate].
     */
    fun close()
}
