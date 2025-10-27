package com.phodal.lotus.chat.history

import com.phodal.lotus.aicore.context.summarization.ConversationMessage
import com.phodal.lotus.chat.model.ChatMessage
import com.phodal.lotus.aicore.context.summarization.ConversationSummarizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.ZoneId
import java.util.*

/**
 * Manages the current conversation and provides access to conversation history.
 * Handles switching between conversations, creating new ones, and persisting them.
 */
class ConversationManager(
    private val historyService: ConversationHistoryService = ConversationHistoryService.getInstance(),
    private val conversationSummarizer: ConversationSummarizer? = null
) {
    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    private val _currentConversationTitle = MutableStateFlow<String>("Current Conversation")
    val currentConversationTitle: StateFlow<String> = _currentConversationTitle.asStateFlow()

    private val _conversationHistories = MutableStateFlow<List<ConversationHistory>>(emptyList())
    val conversationHistories: StateFlow<List<ConversationHistory>> = _conversationHistories.asStateFlow()

    private val _currentConversation = MutableStateFlow<ConversationHistory?>(null)
    val currentConversation: StateFlow<ConversationHistory?> = _currentConversation.asStateFlow()

    init {
        refreshConversationList()
    }

    /**
     * Create a new conversation
     */
    fun createNewConversation(title: String = "New Conversation"): String {
        val conversationId = UUID.randomUUID().toString()
        val newConversation = ConversationHistory(
            id = conversationId,
            title = title,
            messages = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        historyService.saveConversation(
            id = conversationId,
            title = title,
            messages = emptyList()
        )
        
        switchToConversation(conversationId)
        refreshConversationList()
        
        return conversationId
    }

    /**
     * Switch to an existing conversation
     */
    fun switchToConversation(conversationId: String) {
        val conversation = historyService.getConversation(conversationId)
        if (conversation != null) {
            _currentConversationId.value = conversationId
            _currentConversationTitle.value = conversation.title
            _currentConversation.value = conversation
        }
    }

    /**
     * Save the current conversation with new messages
     */
    fun saveCurrentConversation(messages: List<ChatMessage>) {
        val conversationId = _currentConversationId.value ?: return
        val title = _currentConversationTitle.value
        
        val updatedConversation = historyService.updateConversation(conversationId, messages)
        if (updatedConversation != null) {
            _currentConversation.value = updatedConversation
            refreshConversationList()
        }
    }

    /**
     * Update the title of the current conversation
     */
    fun updateCurrentConversationTitle(newTitle: String) {
        val conversationId = _currentConversationId.value ?: return
        val currentMessages = _currentConversation.value?.messages ?: emptyList()
        
        historyService.saveConversation(
            id = conversationId,
            title = newTitle,
            messages = currentMessages
        )
        
        _currentConversationTitle.value = newTitle
        refreshConversationList()
    }

    /**
     * Delete a conversation
     */
    fun deleteConversation(conversationId: String) {
        historyService.deleteConversation(conversationId)
        
        // If we deleted the current conversation, create a new one
        if (_currentConversationId.value == conversationId) {
            createNewConversation()
        } else {
            refreshConversationList()
        }
    }

    /**
     * Refresh the list of conversations
     */
    fun refreshConversationList() {
        _conversationHistories.value = historyService.getAllConversations()
    }

    /**
     * Get all conversations
     */
    fun getAllConversations(): List<ConversationHistory> {
        return _conversationHistories.value
    }

    /**
     * Search conversations
     */
    fun searchConversations(query: String): List<ConversationHistory> {
        return historyService.searchConversations(query)
    }

    /**
     * Clear all conversations
     */
    fun clearAllConversations() {
        historyService.clearAll()
        createNewConversation()
        refreshConversationList()
    }

    /**
     * Summarize a conversation
     *
     * @param conversationId The ID of the conversation to summarize
     * @return The summary text, or null if summarization is not available
     */
    suspend fun summarizeConversation(conversationId: String): String? {
        if (conversationSummarizer == null || !conversationSummarizer.isReady()) {
            return null
        }

        val conversation = historyService.getConversation(conversationId) ?: return null

        if (conversation.messages.isEmpty()) {
            return null
        }

        // Convert ChatMessage to ConversationMessage
        val messages = conversation.messages.map { msg ->
            ConversationMessage(
                id = msg.id,
                content = msg.content,
                author = msg.author,
                isUserMessage = msg.isMyMessage,
                timestamp = msg.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        }

        // Generate summary
        val result = conversationSummarizer.summarize(messages)

        // Save summary to conversation
        historyService.updateConversationSummary(conversationId, result.summary)

        // Update current conversation if it's the one being summarized
        if (_currentConversationId.value == conversationId) {
            val updated = historyService.getConversation(conversationId)
            if (updated != null) {
                _currentConversation.value = updated
            }
        }

        return result.summary
    }

    /**
     * Close the manager and cleanup resources
     */
    fun close() {
        historyService.close()
    }

    companion object {
        @Volatile
        private var instance: ConversationManager? = null

        fun getInstance(): ConversationManager {
            return instance ?: synchronized(this) {
                instance ?: ConversationManager().also { instance = it }
            }
        }
    }
}

