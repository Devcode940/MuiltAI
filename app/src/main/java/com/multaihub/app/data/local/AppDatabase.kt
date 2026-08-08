package com.multaihub.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.data.model.Note
import com.multaihub.app.data.model.Prompt
import com.multaihub.app.data.model.Tab
import com.multaihub.app.utils.DefaultAiProviders

/**
 * Room database for local application state.
 * // WHY: The database owns durable provider, prompt, note, and tab state; UI code never writes SQLite directly.
 */
@Database(
    entities = [AiProvider::class, Prompt::class, Note::class, Tab::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun aiProviderDao(): AiProviderDao
    abstract fun promptDao(): PromptDao
    abstract fun noteDao(): NoteDao
    abstract fun tabDao(): TabDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // WHY: This migration creates the tab table without destroying existing provider data.
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tabs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        providerId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        url TEXT NOT NULL,
                        isDesktopMode INTEGER NOT NULL DEFAULT 0,
                        canGoBack INTEGER NOT NULL DEFAULT 0,
                        canGoForward INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        lastAccessed INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        /** Returns the process-wide Room instance. */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "multaihub_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seedDefaultProviders(db)
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /**
         * Seeds built-in providers only when the database is first created.
         * // WHY: Synchronous insertion inside Room's onCreate callback avoids a coroutine that
         * // captures an uninitialized database instance and avoids a process-wide leaked scope.
         */
        private fun seedDefaultProviders(db: SupportSQLiteDatabase) {
            DefaultAiProviders.list.forEach { provider ->
                db.insert(
                    "ai_providers",
                    SQLiteDatabase.CONFLICT_IGNORE,
                    provider.toContentValues()
                )
            }
        }

        private fun AiProvider.toContentValues(): ContentValues = ContentValues().apply {
            put("id", id)
            put("name", name)
            put("url", url)
            put("iconUrl", iconUrl)
            put("category", category)
            put("isCustom", if (isCustom) 1 else 0)
            put("isDesktopMode", if (isDesktopMode) 1 else 0)
            put("isFavorite", if (isFavorite) 1 else 0)
            put("isHidden", if (isHidden) 1 else 0)
            put("sortOrder", sortOrder)
            put("lastUsed", lastUsed)
        }
    }
}
