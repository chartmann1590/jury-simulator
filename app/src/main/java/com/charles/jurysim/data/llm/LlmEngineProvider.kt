package com.charles.jurysim.data.llm

/**
 * Process-singleton holder for the [LlmEngine] instance. Owned by
 * `JurySimApp` (the `Application` subclass) so the 3.65 GB engine survives
 * configuration changes and isn't recreated when the user navigates between
 * screens.
 *
 * The engine is created lazily on first access — onboarding doesn't need it
 * (it's still downloading), but the moment `MainActivity` decides to start
 * at `Home` we want the weights warming up in the background.
 */
class LlmEngineProvider(private val modelManager: ModelManager) {

    @Volatile private var instance: LlmEngine? = null

    /**
     * Returns the singleton [LlmEngine]. Does NOT call [LlmEngine.ensureLoaded]
     * — callers should do that on a background dispatcher (the first
     * `generate` call also implicitly loads).
     */
    fun get(): LlmEngine {
        instance?.let { return it }
        return synchronized(this) {
            instance ?: LiteRtLlmEngine(modelManager.modelPath()).also { instance = it }
        }
    }

    /** Tear down the current engine — call after deleting the model file. */
    fun reset() {
        synchronized(this) {
            instance?.close()
            instance = null
        }
    }
}
