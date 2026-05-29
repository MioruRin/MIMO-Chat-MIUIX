package com.mroldl001.mimochat.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mroldl001.mimochat.data.local.AppDatabase
import com.mroldl001.mimochat.data.local.ChatDao
import com.mroldl001.mimochat.data.local.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.addColumnIfMissing("messages", "isAborted", "INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.addColumnIfMissing("messages", "isFailed", "INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.addColumnIfMissing("messages", "searchResultsJson", "TEXT")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.addColumnIfMissing("messages", "reasoningContent", "TEXT")
        db.addColumnIfMissing("messages", "isStreaming", "INTEGER NOT NULL DEFAULT 0")
    }
}

private fun SupportSQLiteDatabase.addColumnIfMissing(
    tableName: String,
    columnName: String,
    columnDefinition: String
) {
    query("PRAGMA table_info($tableName)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (cursor.getString(nameIndex) == columnName) return
        }
    }
    execSQL("ALTER TABLE $tableName ADD COLUMN $columnName $columnDefinition")
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mimo_chat_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    fun provideChatDao(database: AppDatabase): ChatDao {
        return database.chatDao()
    }
}
