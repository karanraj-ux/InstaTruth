package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FactCheckDao {
    @Query("SELECT * FROM fact_checked_videos ORDER BY timestamp DESC")
    fun getAllVideos(): Flow<List<FactCheckedVideo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: FactCheckedVideo): Long
}

@Dao
interface RoomSessionDao {
    @Query("SELECT * FROM room_sessions ORDER BY createdAt DESC")
    fun getAllRooms(): Flow<List<RoomSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomSession): Long
    
    @Query("SELECT * FROM room_sessions WHERE roomCode = :code LIMIT 1")
    suspend fun getRoomByCode(code: String): RoomSession?
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getMessagesForRoom(roomId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long
}
