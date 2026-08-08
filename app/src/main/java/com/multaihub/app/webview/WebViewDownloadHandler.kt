package com.multaihub.app.webview

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil

/** Handles WebView downloads through Android's managed DownloadManager. */
class WebViewDownloadHandler(private val context: Context) {
    /** Queues a download only for HTTP(S) resources. */
    fun enqueue(url: String, userAgent: String?, contentDisposition: String?, mimeType: String?) {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return
        val scheme = uri.scheme?.lowercase() ?: return
        if (scheme != "https" && scheme != "http") return

        val request = DownloadManager.Request(uri).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                URLUtil.guessFileName(url, contentDisposition, mimeType).take(180)
            )
            userAgent?.let(::setUserAgent)
            CookieManager.getInstance().getCookie(url)?.let(::addRequestHeader)
            mimeType?.let(::setMimeType)
        }
        context.getSystemService(DownloadManager::class.java)?.enqueue(request)
    }
}
