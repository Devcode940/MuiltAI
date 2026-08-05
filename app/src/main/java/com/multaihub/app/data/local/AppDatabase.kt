package com.multaihub.app.data.local

import android.content.Context
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                database.execSQL("""
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
                """)
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "multaihub_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                instance.aiProviderDao().insertAll(DefaultAiProviders.list)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
