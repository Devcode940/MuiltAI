package com.multaihub.app.utils

object UserAgent {
    const val MOBILE = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
    const val DESKTOP = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    fun get(isDesktop: Boolean): String = if (isDesktop) DESKTOP else MOBILE
}
