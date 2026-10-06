package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AstraGeminiClient
import com.example.data.api.AstraOfflineSolver
import com.example.data.local.AstraDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.preferences.AstraPreferences
import com.example.data.repository.AstraRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class AstraViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AstraDatabase.getDatabase(application)
    private val repository = AstraRepository(db.astraDao())
    private val geminiClient = AstraGeminiClient()
    val preferences = AstraPreferences(application)

    // Conversations & Messages reactively backed by local Room database
    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentConversationId = MutableStateFlow<Long?>(null)
    val currentConversationId: StateFlow<Long?> = _currentConversationId.asStateFlow()

    private val _currentConversation = MutableStateFlow<ConversationEntity?>(null)
    val currentConversation: StateFlow<ConversationEntity?> = _currentConversation.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<MessageEntity>> = _currentConversationId
        .flatMapLatest { id ->
            if (id != null) repository.getMessagesForConversation(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedMessages: StateFlow<List<MessageEntity>> = repository.bookmarkedMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Generation state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private var activeJob: Job? = null

    // TTS state
    private var textToSpeech: TextToSpeech? = null
    private val _isTtsInitialized = MutableStateFlow(false)
    private val _speakingMessageId = MutableStateFlow<Long?>(null)
    val speakingMessageId: StateFlow<Long?> = _speakingMessageId.asStateFlow()

    // Navigation and UI state
    private val _currentTab = MutableStateFlow(0) // 0: Chat, 1: Hub, 2: Bookmarks, 3: Settings
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _isHistoryDrawerOpen = MutableStateFlow(false)
    val isHistoryDrawerOpen: StateFlow<Boolean> = _isHistoryDrawerOpen.asStateFlow()

    // Suggestion pills
    private val _quickSuggestions = MutableStateFlow(
        listOf(
            "🌅 30-min energizing morning routine",
            "🐍 Python script to automate file backups",
            "⚛️ Explain Quantum Computing simply",
            "💰 Ghar ka budget 50/30/20 rule se kaise manage karein?",
            "💡 5 viral hooks for a tech startup"
        )
    )
    val quickSuggestions: StateFlow<List<String>> = _quickSuggestions.asStateFlow()

    init {
        initTts(application)
        ensureDefaultConversation()
    }

    private fun initTts(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.let { tts ->
                    val result = tts.setLanguage(Locale.getDefault())
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        _isTtsInitialized.value = true
                    }
                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}
                        override fun onDone(utteranceId: String?) {
                            _speakingMessageId.value = null
                        }
                        override fun onError(utteranceId: String?) {
                            _speakingMessageId.value = null
                        }
                    })
                }
            }
        }
    }

    fun setTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun setHistoryDrawerOpen(isOpen: Boolean) {
        _isHistoryDrawerOpen.value = isOpen
    }

    private fun ensureDefaultConversation() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.getConversationCount()
            if (count == 0) {
                val newId = repository.insertConversation(
                    ConversationEntity(
                        title = "Welcome to Astra",
                        category = "General"
                    )
                )
                // Persist friendly initial welcome message from Astra into Room
                repository.insertMessage(
                    MessageEntity(
                        conversationId = newId,
                        sender = "ASTRA",
                        content = """
### ✨ Namaste & Welcome! I am Astra.

I am your all-in-one personal AI assistant. Whether you need:

* **Daily Problem Solving:** Practical steps for daily planning, productivity, cooking, or home fixes.
* **Technical & Coding:** Debugging, Python/Kotlin code, software design, and algorithms.
* **Academic & Science:** Clear, simple breakdowns of physics, math, research, or business concepts.
* **Brainstorming:** Creative startup ideas, content hooks, and strategic frameworks.
* **Hindi & Hinglish Support:** Feel free to chat in English, Hindi, ya bilkul natural Hinglish me!

How can I help you today? Try picking one of the quick suggestions below, tap the mic to speak, or type your query!
                        """.trimIndent(),
                        status = "SUCCESS"
                    )
                )
                _currentConversationId.value = newId
                _currentConversation.value = repository.getConversationById(newId)
            } else {
                // Restore the most recently active conversation from Room
                conversations.value.firstOrNull()?.let {
                    _currentConversationId.value = it.id
                    _currentConversation.value = it
                }
            }
        }
    }

    fun selectConversation(conversationId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val conv = repository.getConversationById(conversationId)
            _currentConversationId.value = conversationId
            _currentConversation.value = conv
            _isHistoryDrawerOpen.value = false
            stopSpeaking()
        }
    }

    fun createNewConversation(initialPrompt: String? = null, category: String = "General") {
        viewModelScope.launch(Dispatchers.IO) {
            stopSpeaking()
            val title = if (!initialPrompt.isNullOrBlank()) {
                if (initialPrompt.length > 28) initialPrompt.take(28) + "..." else initialPrompt
            } else {
                "New Conversation"
            }
            val id = repository.insertConversation(
                ConversationEntity(
                    title = title,
                    category = category
                )
            )
            _currentConversationId.value = id
            _currentConversation.value = repository.getConversationById(id)
            _isHistoryDrawerOpen.value = false

            if (!initialPrompt.isNullOrBlank()) {
                sendMessage(initialPrompt)
            }
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            stopSpeaking()
            repository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                val remaining = conversations.value.filter { it.id != id }
                if (remaining.isNotEmpty()) {
                    selectConversation(remaining.first().id)
                } else {
                    createNewConversation()
                }
            }
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        val convId = _currentConversationId.value ?: return

        activeJob?.cancel()
        _isGenerating.value = true

        viewModelScope.launch(Dispatchers.IO) {
            // Save user message to Room DB
            val userMsgId = repository.insertMessage(
                MessageEntity(
                    conversationId = convId,
                    sender = "USER",
                    content = userText.trim(),
                    status = "SUCCESS"
                )
            )

            // Update conversation title if default
            val currentConv = _currentConversation.value
            if (currentConv != null && (currentConv.title == "New Conversation" || currentConv.title == "Welcome to Astra")) {
                val newTitle = if (userText.length > 28) userText.take(28) + "..." else userText
                repository.updateConversation(currentConv.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
            } else {
                currentConv?.let {
                    repository.updateConversation(it.copy(updatedAt = System.currentTimeMillis()))
                }
            }

            // Prepare history from Room persistence
            val existingMessages = repository.getMessagesListForConversation(convId)
            val historyPairs = existingMessages
                .filter { it.id != userMsgId }
                .takeLast(10)
                .map { it.sender to it.content }

            // Temporary placeholder for Astra response in Room DB
            val astraMsgId = repository.insertMessage(
                MessageEntity(
                    conversationId = convId,
                    sender = "ASTRA",
                    content = "Thinking...",
                    status = "SENDING"
                )
            )

            val customApiKey = preferences.customApiKey.value
            val model = preferences.selectedModel.value
            val tone = preferences.toneStyle.value
            val lang = preferences.languagePreference.value

            activeJob = launch {
                val result = geminiClient.generateAstraResponse(
                    prompt = userText,
                    history = historyPairs,
                    customApiKey = customApiKey,
                    modelName = model,
                    toneStyle = tone,
                    languagePref = lang
                )

                _isGenerating.value = false

                result.onSuccess { reply ->
                    // Persist Astra's successful response in Room
                    repository.updateMessage(
                        MessageEntity(
                            id = astraMsgId,
                            conversationId = convId,
                            sender = "ASTRA",
                            content = reply,
                            status = "SUCCESS",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }.onFailure { ex ->
                    Log.w("AstraViewModel", "Gemini call failed, checking fallback: ${ex.message}")
                    // If no API key or network error, provide Astra's rich offline practical guidance
                    val offlineReply = AstraOfflineSolver.getOfflineSolution(userText)
                    val combinedReply = buildString {
                        append(offlineReply)
                        if (ex.message?.contains("API key") == true || ex.message?.contains("not configured") == true) {
                            append("\n\n---\n*🔑 Note: Configure your Gemini API key in **Settings** (or AI Studio Secrets) to enable unrestricted live Gemini AI answers.*")
                        }
                    }
                    // Persist response in Room
                    repository.updateMessage(
                        MessageEntity(
                            id = astraMsgId,
                            conversationId = convId,
                            sender = "ASTRA",
                            content = combinedReply,
                            status = "SUCCESS",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    fun stopGeneration() {
        activeJob?.cancel()
        _isGenerating.value = false
    }

    fun toggleBookmark(message: MessageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBookmark(message.id, !message.isBookmarked)
        }
    }

    fun toggleSpeakMessage(message: MessageEntity) {
        if (_speakingMessageId.value == message.id) {
            stopSpeaking()
        } else {
            speakText(message.id, message.content)
        }
    }

    private fun speakText(messageId: Long, text: String) {
        stopSpeaking()
        val tts = textToSpeech ?: return
        _speakingMessageId.value = messageId

        // Strip markdown syntax for natural voice synthesis
        val cleanText = text
            .replace(Regex("```[a-zA-Z]*"), "")
            .replace(Regex("```"), "")
            .replace(Regex("[#*`_>~-]"), "")
            .trim()

        tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, messageId.toString())
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        _speakingMessageId.value = null
    }

    fun hasValidApiKey(): Boolean {
        return geminiClient.hasValidApiKey(preferences.customApiKey.value)
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
