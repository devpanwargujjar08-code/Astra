package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AstraDatabase : RoomDatabase() {
    abstract fun astraDao(): AstraDao

    companion object {
        @Volatile
        private var INSTANCE: AstraDatabase? = null

        fun getDatabase(context: Context): AstraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstraDatabase::class.java,
                    "astra_assistant.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
