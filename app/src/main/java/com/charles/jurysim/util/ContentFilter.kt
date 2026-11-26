package com.charles.jurysim.util

/**
 * Output-side safety filter for AI-generated text.
 *
 * Every string the on-device model produces passes through [sanitize]
 * (via `SimulationViewModel.generateChecked`) before it is shown to the
 * user, stored in case history, or spoken by TTS. This is the backstop
 * behind the prompt-level constraints in [PromptTemplates] and the
 * system instruction in the LiteRT-LM engine: even if the model ignores
 * its instructions, slurs never reach the screen and strong profanity
 * is masked.
 *
 * Policy (matches the Teen content-rating filing):
 * - Slurs / hate terms: the whole response is replaced with an
 *   in-character fallback line. A single slur poisons the response.
 * - Strong profanity: masked in place with "[expletive]".
 * - Mild language ("damn", "hell") is left alone — it is consistent
 *   with realistic courtroom dialogue at a Teen rating.
 */
object ContentFilter {

    /** Shown instead of a response that contained a slur or hate term. */
    const val FALLBACK_LINE: String =
        "The court will keep these proceedings respectful and focused on the facts " +
            "of the case. Let the record reflect that we are moving on."

    /** Slurs and hate terms. Any match discards the entire response. */
    private val severeTerms = listOf(
        "nigger", "nigga", "niggah", "faggot", "kike", "spic", "chink",
        "wetback", "gook", "raghead", "tranny", "retard", "retarded",
        "beaner", "coon", "darkie", "jap", "paki", "fag"
    )

    /** Strong profanity. Masked in place; the rest of the text survives. */
    private val strongProfanity = listOf(
        "motherfucker", "motherfucking", "fuck", "fucking", "fucked",
        "shit", "shitty", "bullshit", "bitch", "bastard", "asshole",
        "whore", "slut", "cunt", "pussy", "dickhead", "jackass",
        "dumbass", "goddamn", "goddammit"
    )

    private val severeRegexes = severeTerms.map { term ->
        Regex("(?i)\\b" + Regex.escape(term) + "\\b")
    }

    private val profanityRegexes = strongProfanity.map { term ->
        Regex("(?i)\\b" + Regex.escape(term) + "\\w*")
    }

    /**
     * Returns [raw] made safe to display: unchanged if clean, masked if
     * it contains strong profanity, or [FALLBACK_LINE] if it contains a
     * slur / hate term.
     */
    fun sanitize(raw: String): String {
        if (raw.isBlank()) return raw
        for (regex in severeRegexes) {
            if (regex.containsMatchIn(raw)) return FALLBACK_LINE
        }
        var out = raw
        for (regex in profanityRegexes) {
            out = regex.replace(out, "[expletive]")
        }
        return out
    }

    /** True if [sanitize] would change [raw] (used by tests/telemetry). */
    fun wouldModify(raw: String): Boolean = sanitize(raw) != raw
}
