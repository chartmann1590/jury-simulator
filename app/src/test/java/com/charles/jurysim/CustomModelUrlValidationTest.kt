package com.charles.jurysim

import com.charles.jurysim.ui.screens.onboarding.OnboardingViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The custom model URL is a supply-chain surface: it decides what
 * 3+ GB file the app downloads and loads. Validation must stay strict.
 */
class CustomModelUrlValidationTest {

    private fun error(url: String) = OnboardingViewModel.validateCustomModelUrl(url)

    @Test
    fun `valid https litertlm url passes`() {
        assertNull(error("https://huggingface.co/org/model/resolve/main/gemma.litertlm"))
        assertNull(error("https://example.com/MODEL.LITERTLM"))
    }

    @Test
    fun `http is rejected`() {
        assertNotNull(error("http://example.com/model.litertlm"))
    }

    @Test
    fun `non-litertlm paths are rejected`() {
        assertNotNull(error("https://example.com/model.bin"))
        assertNotNull(error("https://example.com/"))
        assertNotNull(error("https://example.com/model.litertlm.exe"))
    }

    @Test
    fun `missing host or garbage is rejected`() {
        assertNotNull(error("https:///model.litertlm"))
        assertNotNull(error("not a url"))
        assertNotNull(error(""))
    }

    @Test
    fun `error messages are specific`() {
        assertEquals(
            "Custom model URLs must use HTTPS.",
            error("http://example.com/model.litertlm")
        )
    }
}
