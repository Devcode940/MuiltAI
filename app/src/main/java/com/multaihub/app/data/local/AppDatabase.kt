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
 *
 * The database owns durable provider, prompt, note, and tab state. UI code never writes
 * SQLite directly — all access goes through the DAOs exposed by this class.
 *
 * Schema version history:
 * - Version 1: Initial schema with providers, prompts, notes.
 * - Version 2: Added `tabs` table.
 * - Version 3: Added performance indexes on frequently queried columns.
 */
@Database(
    entities = [AiProvider::class, Prompt::class, Note::class, Tab::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    /** Data access object for AI provider operations. */
    abstract fun aiProviderDao(): AiProviderDao

    /** Data access object for prompt library operations. */
    abstract fun promptDao(): PromptDao

    /** Data access object for note operations. */
    abstract fun noteDao(): NoteDao

    /** Data access object for browser tab operations. */
    abstract fun tabDao(): TabDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Migration from schema version 1 to 2: creates the `tabs` table. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
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

        /**
         * Migration from schema version 2 to 3: adds performance indexes.
         *
         * Indexes are added to columns frequently used in WHERE and ORDER BY clauses
         * to keep catalog queries responsive as the database grows.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // ai_providers indexes
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ai_providers_category ON ai_providers(category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ai_providers_isFavorite ON ai_providers(isFavorite)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ai_providers_isHidden ON ai_providers(isHidden)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ai_providers_lastUsed ON ai_providers(lastUsed)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ai_providers_url ON ai_providers(url)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ai_providers_isHidden_category ON ai_providers(isHidden, category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ai_providers_isHidden_isFavorite ON ai_providers(isHidden, isFavorite)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_ai_providers_isHidden_lastUsed ON ai_providers(isHidden, lastUsed)")

                // tabs indexes
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tabs_providerId ON tabs(providerId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tabs_lastAccessed ON tabs(lastAccessed)")
            }
        }

        /**
         * Returns the process-wide Room singleton instance.
         *
         * @param context Any Android context; the application context is used internally.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "multaihub_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
         *
         * Synchronous insertion inside Room's `onCreate` callback avoids a coroutine that
         * would capture an uninitialized database instance and avoids a process-wide leaked scope.
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

        /** Converts an [AiProvider] to [ContentValues] for direct SQLite insertion. */
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
