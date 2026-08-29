package com.multaihub.app.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Unit tests for [UrlValidator].
 *
 * These tests run on the JVM (no Android device/emulator required) because
 * [UrlValidator] uses only `android.net.Uri` which is available in the
 * Android unit-test classpath via `android.jar` mocks.
 */
class UrlValidatorTest {

    // region validateAndEnforceHttps

    @Test
    fun `validateAndEnforceHttps accepts valid HTTPS URL`() {
        val result = UrlValidator.validateAndEnforceHttps("https://chatgpt.com")
        assertNotNull(result)
        assertEquals("https://chatgpt.com", result)
    }

    @Test
    fun `validateAndEnforceHttps upgrades HTTP to HTTPS when enforced`() {
        val result = UrlValidator.validateAndEnforceHttps("http://chatgpt.com", enforceHttps = true)
        assertNotNull(result)
        assertEquals("https://chatgpt.com", result)
    }

    @Test
    fun `validateAndEnforceHttps keeps HTTP when not enforced`() {
        val result = UrlValidator.validateAndEnforceHttps("http://chatgpt.com", enforceHttps = false)
        assertNotNull(result)
        assertEquals("http://chatgpt.com", result)
    }

    @Test
    fun `validateAndEnforceHttps prepends https when scheme is missing`() {
        val result = UrlValidator.validateAndEnforceHttps("chatgpt.com")
        assertNotNull(result)
        assertEquals("https://chatgpt.com", result)
    }

    @Test
    fun `validateAndEnforceHttps rejects blank input`() {
        assertNull(UrlValidator.validateAndEnforceHttps(""))
        assertNull(UrlValidator.validateAndEnforceHttps("   "))
    }

    @Test
    fun `validateAndEnforceHttps rejects overly long URLs`() {
        val longUrl = "https://example.com/" + "a".repeat(2100)
        assertNull(UrlValidator.validateAndEnforceHttps(longUrl))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "javascript:alert('xss')",
            "file:///etc/passwd",
            "content://contacts/people",
            "data:text/html,<h1>hello</h1>",
            "intent://scan/#Intent;scheme=zxing;package=com.google.zxing.client.android;end",
            "ftp://example.com/file",
            "custom://myapp/data"
        ]
    )
    fun `validateAndEnforceHttps rejects dangerous schemes`(url: String) {
        assertNull(UrlValidator.validateAndEnforceHttps(url, enforceHttps = false))
    }

    @Test
    fun `validateAndEnforceHttps normalizes host to lowercase`() {
        val result = UrlValidator.validateAndEnforceHttps("https://ChatGPT.COM")
        assertNotNull(result)
        assertEquals("https://chatgpt.com", result)
    }

    @Test
    fun `validateAndEnforceHttps preserves path and query`() {
        val result = UrlValidator.validateAndEnforceHttps("https://example.com/path?query=value#fragment")
        assertNotNull(result)
        assertEquals("https://example.com/path?query=value#fragment", result)
    }

    // endregion

    // region isHttps

    @Test
    fun `isHttps returns true for HTTPS URLs`() {
        assertTrue(UrlValidator.isHttps("https://example.com"))
        assertTrue(UrlValidator.isHttps("HTTPS://example.com"))
    }

    @Test
    fun `isHttps returns false for non-HTTPS URLs`() {
        assertFalse(UrlValidator.isHttps("http://example.com"))
        assertFalse(UrlValidator.isHttps("ftp://example.com"))
        assertFalse(UrlValidator.isHttps("not-a-url"))
    }

    // endregion

    // region toHttps

    @Test
    fun `toHttps upgrades HTTP to HTTPS`() {
        assertEquals("https://example.com", UrlValidator.toHttps("http://example.com"))
    }

    @Test
    fun `toHttps leaves HTTPS unchanged`() {
        assertEquals("https://example.com", UrlValidator.toHttps("https://example.com"))
    }

    @Test
    fun `toHttps preserves path during upgrade`() {
        assertEquals(
            "https://example.com/path?q=1",
            UrlValidator.toHttps("http://example.com/path?q=1")
        )
    }

    // endregion

    // region isValidUrl

    @Test
    fun `isValidUrl accepts valid HTTP and HTTPS`() {
        assertTrue(UrlValidator.isValidUrl("https://example.com"))
        assertTrue(UrlValidator.isValidUrl("http://example.com"))
    }

    @Test
    fun `isValidUrl rejects invalid URLs`() {
        assertFalse(UrlValidator.isValidUrl("not a url"))
        assertFalse(UrlValidator.isValidUrl("ftp://example.com"))
        assertFalse(UrlValidator.isValidUrl(""))
    }

    // endregion

    // region getDomain

    @Test
    fun `getDomain extracts host from valid URL`() {
        assertEquals("chatgpt.com", UrlValidator.getDomain("https://chatgpt.com/path"))
    }

    @Test
    fun `getDomain returns null for invalid URL`() {
        assertNull(UrlValidator.getDomain("not a url"))
    }

    // endregion

    // region isSafeWebNavigation

    @Test
    fun `isSafeWebNavigation accepts HTTP and HTTPS`() {
        assertTrue(UrlValidator.isSafeWebNavigation("https://example.com"))
        assertTrue(UrlValidator.isSafeWebNavigation("http://example.com"))
    }

    @Test
    fun `isSafeWebNavigation rejects dangerous schemes`() {
        assertFalse(UrlValidator.isSafeWebNavigation("javascript:alert(1)"))
        assertFalse(UrlValidator.isSafeWebNavigation("file:///sdcard"))
    }

    // endregion
}
