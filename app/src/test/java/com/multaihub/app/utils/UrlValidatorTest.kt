package com.multaihub.app.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Regression tests for the WebView URL trust boundary. */
class UrlValidatorTest {
    @Test
    fun acceptsHttpsDomain() {
        assertTrue(UrlValidator.isValidUrl("https://chatgpt.com"))
    }

    @Test
    fun upgradesHttpWhenHttpsIsRequired() {
        assertEquals(
            "https://example.com/path",
            UrlValidator.validateAndEnforceHttps("http://example.com/path")
        )
    }

    @Test
    fun rejectsDangerousSchemes() {
        assertNull(UrlValidator.validateAndEnforceHttps("javascript:alert(1)"))
        assertNull(UrlValidator.validateAndEnforceHttps("file:///etc/passwd"))
        assertNull(UrlValidator.validateAndEnforceHttps("data:text/html,test"))
        assertNull(UrlValidator.validateAndEnforceHttps("intent://example.com"))
    }

    @Test
    fun rejectsMissingHost() {
        assertFalse(UrlValidator.isValidUrl("https:///path"))
    }

    @Test
    fun rejectsOversizedInput() {
        assertFalse(UrlValidator.isValidUrl("https://example.com/" + "a".repeat(2048)))
    }

    @Test
    fun returnsNormalizedDomain() {
        assertEquals("example.com", UrlValidator.getDomain("HTTPS://Example.COM/path"))
    }
}
