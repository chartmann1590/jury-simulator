package com.charles.jurysim.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentFilterTest {

    @Test
    fun `clean courtroom text passes through unchanged`() {
        val text = "The witness stated the defendant was seen near the warehouse at 9 PM."
        assertEquals(text, ContentFilter.sanitize(text))
    }

    @Test
    fun `mild language is allowed at a Teen rating`() {
        val text = "That verdict was a damn shame, but the evidence was clear."
        assertEquals(text, ContentFilter.sanitize(text))
    }

    @Test
    fun `strong profanity is masked in place`() {
        val out = ContentFilter.sanitize("This whole fucking case is bullshit.")
        assertFalse(out.contains("fucking", ignoreCase = true))
        assertFalse(out.contains("bullshit", ignoreCase = true))
        assertTrue(out.contains("[expletive]"))
        assertTrue(out.startsWith("This whole"))
    }

    @Test
    fun `a slur discards the whole response for the fallback line`() {
        val out = ContentFilter.sanitize("The juror muttered a racial slur: nigger.")
        assertEquals(ContentFilter.FALLBACK_LINE, out)
    }

    @Test
    fun `slur detection is case insensitive`() {
        val out = ContentFilter.sanitize("He shouted FAGGOT across the room.")
        assertEquals(ContentFilter.FALLBACK_LINE, out)
    }

    @Test
    fun `blank input is returned as-is`() {
        assertEquals("", ContentFilter.sanitize(""))
        assertEquals("   ", ContentFilter.sanitize("   "))
    }

    @Test
    fun `structured verdict output is not disturbed`() {
        val text = "VERDICT: GUILTY\nREASONING: The fingerprint evidence was decisive."
        assertEquals(text, ContentFilter.sanitize(text))
    }
}
