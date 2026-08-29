package com.multaihub.app.webview

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import com.multaihub.app.utils.AppConstants.MAX_DOWNLOAD_FILENAME_LENGTH
import com.multaihub.app.utils.AppConstants.SCHEME_HTTP
import com.multaihub.app.utils.AppConstants.SCHEME_HTTPS

/**
 * Handles WebView downloads through Android's managed [DownloadManager].
 *
 * Downloads are delegated to the system so the app does not need storage permissions
 * or to manage network streams directly. Only HTTP(S) URLs are accepted.
 */
class WebViewDownloadHandler(private val context: Context) {

    /**
     * Queues a download request. Only HTTP(S) resources are accepted.
     *
     * @param url The URL of the file to download.
     * @param userAgent Optional user-agent string for the request.
     * @param contentDisposition Optional Content-Disposition header for filename detection.
     * @param mimeType Optional MIME type of the file.
     */
    fun enqueue(url: String, userAgent: String?, contentDisposition: String?, mimeType: String?) {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return
        val scheme = uri.scheme?.lowercase() ?: return
        if (scheme != SCHEME_HTTPS && scheme != SCHEME_HTTP) return

        val request = DownloadManager.Request(uri).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                URLUtil.guessFileName(url, contentDisposition, mimeType).take(MAX_DOWNLOAD_FILENAME_LENGTH)
            )
            userAgent?.let(::setUserAgent)
            CookieManager.getInstance().getCookie(url)?.let(::addRequestHeader)
            mimeType?.let(::setMimeType)
        }

        context.getSystemService(DownloadManager::class.java)?.enqueue(request)
    }
}
