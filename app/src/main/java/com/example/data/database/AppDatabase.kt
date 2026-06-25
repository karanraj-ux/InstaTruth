package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FactCheckedVideo::class, RoomSession::class, ChatMessage::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun factCheckDao(): FactCheckDao
    abstract fun roomSessionDao(): RoomSessionDao
    abstract fun chatDao(): ChatDao
}
