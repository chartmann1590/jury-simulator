package com.charles.jurysim.ui.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.charles.jurysim.data.model.Message
import java.util.Locale

class ChatTtsPlayer(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isReady = true
            }
        }
    }

    fun speak(message: Message, judgeGender: String, judgeVoiceSeed: Int) {
        val engine = tts ?: return
        if (!isReady) return

        val text = message.content.trim()
        if (text.isBlank()) return

        engine.stop()

        if (message.speaker?.contains("judge", ignoreCase = true) == true) {
            applyJudgeVoice(engine, judgeGender, judgeVoiceSeed)
        } else {
            engine.setPitch(1.0f)
            engine.setSpeechRate(1.0f)
        }

        engine.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            Bundle(),
            "chat_tts_${message.timestamp}"
        )
    }

    private fun applyJudgeVoice(engine: TextToSpeech, judgeGender: String, judgeVoiceSeed: Int) {
        val preferredVoices = engine.voices
            ?.filter { voice ->
                !voice.isNetworkConnectionRequired &&
                    voice.locale.language == Locale.US.language
            }
            ?.filter { voice ->
                if (judgeGender.equals("FEMALE", ignoreCase = true)) {
                    matchesFemale(voice)
                } else if (judgeGender.equals("MALE", ignoreCase = true)) {
                    matchesMale(voice)
                } else {
                    true
                }
            }
            ?.sortedBy { it.name }
            .orEmpty()

        val selectedVoice = if (preferredVoices.isNotEmpty()) {
            val idx = kotlin.math.abs(judgeVoiceSeed) % preferredVoices.size
            preferredVoices[idx]
        } else {
            null
        }

        if (selectedVoice != null) {
            engine.voice = selectedVoice
        }

        val basePitch = when {
            judgeGender.equals("FEMALE", ignoreCase = true) -> 1.12f
            judgeGender.equals("MALE", ignoreCase = true) -> 0.92f
            else -> 1.0f
        }
        engine.setPitch(basePitch)
        engine.setSpeechRate(0.95f)
    }

    private fun matchesFemale(voice: Voice): Boolean {
        val text = "${voice.name} ${voice.locale.displayName}".lowercase()
        return text.contains("female") || text.contains("f1") || text.contains("woman")
    }

    private fun matchesMale(voice: Voice): Boolean {
        val text = "${voice.name} ${voice.locale.displayName}".lowercase()
        return text.contains("male") || text.contains("m1") || text.contains("man")
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
