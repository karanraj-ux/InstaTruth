package com.example.data.database

import kotlinx.coroutines.flow.Flow

class FactCheckRepository(
    private val factCheckDao: FactCheckDao,
    private val roomSessionDao: RoomSessionDao,
    private val chatDao: ChatDao
) {
    val allFactCheckedVideos: Flow<List<FactCheckedVideo>> = factCheckDao.getAllVideos()
    val allRoomSessions: Flow<List<RoomSession>> = roomSessionDao.getAllRooms()

    suspend fun insertVideo(video: FactCheckedVideo) = factCheckDao.insertVideo(video)

    suspend fun createRoom(roomCode: String, videoId: Int? = null) {
        roomSessionDao.insertRoom(RoomSession(roomCode = roomCode, videoId = videoId))
    }
    
    suspend fun getRoomByCode(code: String): RoomSession? {
        return roomSessionDao.getRoomByCode(code)
    }

    fun getMessagesForRoom(roomId: String): Flow<List<ChatMessage>> = chatDao.getMessagesForRoom(roomId)

    suspend fun sendMessage(roomId: String, sender: String, text: String) {
        chatDao.insertMessage(ChatMessage(roomId = roomId, sender = sender, message = text))
    }
}
