package com.multaihub.app.utils

/**
 * User-agent strings used for WebView mobile/desktop mode switching.
 *
 * These strings identify the browser environment to AI websites. The desktop UA
 * triggers desktop layouts on sites that would otherwise serve a mobile-optimized
 * or restricted experience.
 */
object UserAgent {

    /** User-agent string representing a Chrome browser on Android mobile. */
    const val MOBILE =
        "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"

    /** User-agent string representing a Chrome browser on Windows desktop. */
    const val DESKTOP =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"

    /**
     * Returns the appropriate user-agent string.
     *
     * @param isDesktop When `true`, returns the desktop UA; otherwise returns the mobile UA.
     */
    fun get(isDesktop: Boolean): String = if (isDesktop) DESKTOP else MOBILE
}
