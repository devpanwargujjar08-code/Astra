package com.example.data.repository

import com.example.data.local.AstraDao
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository adhering to the Repository Pattern to abstract local Room database operations
 * from the ViewModel and UI layer.
 */
class AstraRepository(private val dao: AstraDao) {

    val allConversations: Flow<List<ConversationEntity>> = dao.getAllConversations()

    val bookmarkedMessages: Flow<List<MessageEntity>> = dao.getBookmarkedMessages()

    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>> {
        return dao.getMessagesForConversation(conversationId)
    }

    suspend fun getMessagesListForConversation(conversationId: Long): List<MessageEntity> {
        return dao.getMessagesListForConversation(conversationId)
    }

    suspend fun getConversationById(id: Long): ConversationEntity? {
        return dao.getConversationById(id)
    }

    suspend fun insertConversation(conversation: ConversationEntity): Long {
        return dao.insertConversation(conversation)
    }

    suspend fun updateConversation(conversation: ConversationEntity) {
        dao.updateConversation(conversation)
    }

    suspend fun deleteConversation(id: Long) {
        dao.deleteMessagesForConversation(id)
        dao.deleteConversation(id)
    }

    suspend fun insertMessage(message: MessageEntity): Long {
        return dao.insertMessage(message)
    }

    suspend fun updateMessage(message: MessageEntity) {
        dao.updateMessage(message)
    }

    suspend fun updateBookmark(messageId: Long, isBookmarked: Boolean) {
        dao.updateBookmark(messageId, isBookmarked)
    }

    suspend fun getConversationCount(): Int {
        return dao.getConversationCount()
    }
}
