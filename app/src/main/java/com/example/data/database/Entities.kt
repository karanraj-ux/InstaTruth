package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fact_checked_videos")
data class FactCheckedVideo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val extractedVideoUrl: String,
    val analysisReport: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "room_sessions")
data class RoomSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val roomCode: String,
    val videoId: Int?, // optional linked video
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val roomId: String,
    val sender: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
