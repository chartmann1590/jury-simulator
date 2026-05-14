package com.charles.jurysim

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.charles.jurysim.data.llm.LlmEngineProvider
import com.charles.jurysim.data.llm.ModelDownloader
import com.charles.jurysim.data.llm.ModelManager

/**
 * `Application` subclass that owns the long-lived singletons used by the
 * on-device LLM stack.
 *
 *  - [modelManager] resolves the on-disk path of the LiteRT-LM weights and
 *    handles free-space / existence checks.
 *  - [llmEngineProvider] lazily constructs the [com.charles.jurysim.data.llm.LlmEngine]
 *    so it survives navigation / config changes and we don't pay the ~5s
 *    weight-load cost more than once per process.
 *  - [modelDownloader] streams the .litertlm file with HTTP Range resume.
 *
 * Wired in `AndroidManifest.xml` via `android:name=".JurySimApp"`.
 */
class JurySimApp : Application() {

    lateinit var modelManager: ModelManager
        private set

    lateinit var llmEngineProvider: LlmEngineProvider
        private set

    lateinit var modelDownloader: ModelDownloader
        private set

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        modelManager = ModelManager(this)
        llmEngineProvider = LlmEngineProvider(modelManager)
        modelDownloader = ModelDownloader(modelManager)
    }
}
