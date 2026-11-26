package com.charles.jurysim.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptTemplatesTest {

    @Test
    fun `case generator is restricted to the charge allowlist`() {
        val prompt = PromptTemplates.generateCase()
        for (charge in PromptTemplates.ALLOWED_CHARGES) {
            assertTrue("allowlist charge missing from prompt: $charge", prompt.contains(charge))
        }
        assertTrue(prompt.contains("choose ONLY from this list", ignoreCase = true))
    }

    @Test
    fun `case generator explicitly bans high-risk offence categories`() {
        val prompt = PromptTemplates.generateCase()
        assertTrue(prompt.contains("sexual offences", ignoreCase = true))
        assertTrue(prompt.contains("offences against children", ignoreCase = true))
        assertTrue(prompt.contains("graphic violence", ignoreCase = true))
        // The old open-ended phrasing must be gone.
        assertFalse(prompt.contains("etc.)"))
    }

    @Test
    fun `evidence template no longer invites weapons or graphic detail`() {
        val prompt = PromptTemplates.presentEvidence("A burglary case.", "John Smith")
        assertFalse(prompt.contains("weapons", ignoreCase = true))
        assertTrue(prompt.contains("without graphic detail", ignoreCase = true))
    }

    @Test
    fun `constraint block covers the standing content rules`() {
        val block = PromptTemplates.CONSTRAINT_BLOCK
        assertTrue(block.contains("no profanity", ignoreCase = true))
        assertTrue(block.contains("No sexual content", ignoreCase = true))
        assertTrue(block.contains("graphic", ignoreCase = true))
        assertTrue(block.contains("legal advice", ignoreCase = true))
        assertTrue(block.contains("redirect", ignoreCase = true))
    }
}
