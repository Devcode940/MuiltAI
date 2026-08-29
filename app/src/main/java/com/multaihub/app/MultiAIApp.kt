package com.multaihub.app

import android.app.Application
import com.multaihub.app.data.local.AppDatabase
import com.multaihub.app.data.repository.AiRepository
import com.multaihub.app.utils.NetworkMonitor
import com.multaihub.app.webview.WebViewPolicy

/**
 * Application root and dependency container.
 *
 * Creates the process-wide singleton instances of the database, repository, and network
 * monitor. These are exposed as properties so Activities and ViewModel factories can
 * retrieve them without a DI framework.
 */
class MultiAIApp : Application() {

    /** Process-wide data repository for all persistence operations. */
    lateinit var repository: AiRepository
        private set

    /** Network connectivity monitor exposing a lifecycle-aware Flow. */
    lateinit var networkMonitor: NetworkMonitor
        private set

    override fun onCreate() {
        super.onCreate()

        val database = AppDatabase.getInstance(this)
        repository = AiRepository(
            aiProviderDao = database.aiProviderDao(),
            promptDao = database.promptDao(),
            noteDao = database.noteDao(),
            tabDao = database.tabDao()
        )

        networkMonitor = NetworkMonitor(this)
    }

    /**
     * Configures WebView data isolation for a specific provider.
     *
     * **Must be called before any WebView is created** for the given provider. This
     * ensures cookies, localStorage, and other WebView data are siloed per provider,
     * preventing cross-site tracking between AI platforms.
     *
     * @param providerId Unique identifier for the provider (used as the isolation suffix).
     * @return `true` if isolation was applied successfully.
     */
    fun enableWebViewIsolation(providerId: String): Boolean {
        // Sanitize the provider ID to only safe characters for the directory suffix
        val safeSuffix = providerId.replace(Regex("[^A-Za-z0-9_-]"), "_")
        return WebViewPolicy.setDataDirectorySuffix(safeSuffix)
    }
}
