package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val category: String = "General" // General, Problem Solving, Tech & Coding, Academic, Brainstorming
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val sender: String, // "USER" or "ASTRA"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false,
    val status: String = "SUCCESS" // SUCCESS, ERROR, SENDING
)
