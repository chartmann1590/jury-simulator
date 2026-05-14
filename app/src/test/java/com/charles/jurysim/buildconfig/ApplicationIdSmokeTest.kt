package com.charles.jurysim.buildconfig

import com.charles.jurysim.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplicationIdSmokeTest {

    @Test
    fun `applicationId is the renamed package`() {
        assertEquals("com.charles.jurysim", BuildConfig.APPLICATION_ID)
    }
}
