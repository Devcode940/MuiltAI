package com.multaihub.app

import android.app.Application
import com.multaihub.app.data.local.AppDatabase
import com.multaihub.app.data.repository.AiRepository
import com.multaihub.app.utils.NetworkMonitor

class MultiAIApp : Application() {

    lateinit var repository: AiRepository
        private set

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
}
