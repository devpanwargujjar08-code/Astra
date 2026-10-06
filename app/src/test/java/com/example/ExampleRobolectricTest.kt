package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AstraDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.repository.AstraRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AstraDatabase
    private lateinit var repository: AstraRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AstraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AstraRepository(database.astraDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Astra", appName)
    }

    @Test
    fun `conversation and messages persist across retrieval in Room database`() = runBlocking {
        // 1. Insert Conversation
        val convId = repository.insertConversation(
            ConversationEntity(title = "Room Persistence Test", category = "Tech")
        )
        assertTrue(convId > 0)

        // 2. Insert User Message
        val userMsgId = repository.insertMessage(
            MessageEntity(
                conversationId = convId,
                sender = "USER",
                content = "Hello Astra, please persist this message."
            )
        )
        assertTrue(userMsgId > 0)

        // 3. Insert Astra Response
        val astraMsgId = repository.insertMessage(
            MessageEntity(
                conversationId = convId,
                sender = "ASTRA",
                content = "I have persisted your message safely in local SQLite storage!"
            )
        )
        assertTrue(astraMsgId > 0)

        // 4. Verify messages are retrieved in chronological order
        val storedMessages = repository.getMessagesListForConversation(convId)
        assertEquals(2, storedMessages.size)
        assertEquals("USER", storedMessages[0].sender)
        assertEquals("Hello Astra, please persist this message.", storedMessages[0].content)
        assertEquals("ASTRA", storedMessages[1].sender)
        assertEquals("I have persisted your message safely in local SQLite storage!", storedMessages[1].content)

        // 5. Verify conversation flow
        val allConvs = repository.allConversations.first()
        assertEquals(1, allConvs.size)
        assertEquals("Room Persistence Test", allConvs[0].title)
    }
}
