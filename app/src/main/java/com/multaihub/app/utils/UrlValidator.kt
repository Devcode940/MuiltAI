package com.multaihub.app.utils
import android.net.Uri
import android.util.Patterns
object UrlValidator {
    fun validateAndEnforceHttps(url: String, enforceHttps: Boolean = true): String? = try {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank()) return null
        var parsedUri = Uri.parse(trimmedUrl)
        if (parsedUri.scheme.isNullOrEmpty()) parsedUri = Uri.parse("https://$trimmedUrl")
        val scheme = parsedUri.scheme?.lowercase()
        if (scheme !in listOf("http", "https")) return null
        if (parsedUri.host.isNullOrBlank()) return null
        val host = parsedUri.host!!
        if (!Patterns.DOMAIN_NAME.matcher(host).matches()) return null
        if (enforceHttps && scheme == "http") return trimmedUrl.replaceFirst("http://", "https://")
        trimmedUrl
    } catch (e: Exception) { null }
    fun isHttps(url: String): Boolean = try { Uri.parse(url).scheme?.lowercase() == "https" } catch (e: Exception) { false }
    fun toHttps(url: String): String = url.replaceFirst("http://".toRegex(), "https://")
    fun isValidUrl(url: String): Boolean = validateAndEnforceHttps(url, enforceHttps = false) != null
    fun getDomain(url: String): String? = try { val uri = if (url.startsWith("http")) Uri.parse(url) else Uri.parse("https://$url"); uri.host } catch (e: Exception) { null }
}
