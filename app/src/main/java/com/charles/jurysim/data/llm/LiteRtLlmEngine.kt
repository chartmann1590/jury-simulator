package com.charles.jurysim.data.llm

import com.charles.jurysim.util.PromptTemplates
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * LiteRT-LM-backed implementation of [LlmEngine] for on-device Gemma 4
 * inference. Prefers the GPU backend (Adreno / Mali via ML Drift) and falls
 * back to CPU (XNNPACK) if GPU init fails.
 *
 * The underlying [Engine] is not safe for concurrent generation; this class
 * serializes [generate] calls behind a [Mutex]. Each `generate` call spins
 * up a fresh `Conversation` so prompts remain independent (we don't want
 * chat history bleeding between, say, witness generation and verdict
 * generation).
 */
class LiteRtLlmEngine(
    private val modelPath: String,
    private val cacheDir: String? = null
) : LlmEngine {

    @Volatile private var engine: Engine? = null
    private val initMutex = Mutex()
    private val genMutex = Mutex()

    override suspend fun ensureLoaded() {
        if (engine != null) return
        initMutex.withLock {
            if (engine != null) return
            engine = withContext(Dispatchers.IO) {
                runCatching { newEngine(Backend.GPU()) }
                    .getOrElse { newEngine(Backend.CPU()) }
            }
        }
    }

    private fun newEngine(backend: Backend): Engine {
        val cfg = EngineConfig(
            modelPath = modelPath,
            backend = backend,
            cacheDir = cacheDir
        )
        return Engine(cfg).also { it.initialize() }
    }

    override suspend fun generate(prompt: String): Result<String> = runCatching {
        ensureLoaded()
        val live = engine ?: error("LiteRT engine failed to initialize")
        genMutex.withLock {
            withContext(Dispatchers.Default) {
                live.createConversation(CONVERSATION_CONFIG).use { convo ->
                    // The Message API doesn't expose a stable `.text` field
                    // across versions; toString() is documented to return
                    // the text content for text-only conversations.
                    convo.sendMessage(prompt).toString()
                        // LiteRT-LM 0.11 exposes no per-conversation output
                        // token cap, so enforce a hard output length here.
                        .take(MAX_OUTPUT_CHARS)
                }
            }
        }
    }

    companion object {
        /**
         * Hard cap on generated output length (~2,500 tokens at typical
         * density — far longer than any single simulation response needs).
         */
        const val MAX_OUTPUT_CHARS: Int = 10_000

        /**
         * Every conversation runs with the standing content rules as its
         * system instruction and with explicit sampler limits (temperature
         * 0.7 / topK 40 / topP 0.9) instead of runtime defaults, keeping
         * generation focused and courtroom-appropriate.
         */
        val CONVERSATION_CONFIG: ConversationConfig = ConversationConfig(
            systemInstruction = Contents.of(PromptTemplates.CONSTRAINT_BLOCK),
            samplerConfig = SamplerConfig(
                topK = 40,
                topP = 0.9,
                temperature = 0.7
            )
        )
    }

    override fun close() {
        engine?.close()
        engine = null
    }
}
