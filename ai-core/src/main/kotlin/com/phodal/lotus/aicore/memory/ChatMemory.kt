package com.phodal.lotus.aicore.memory

import com.phodal.lotus.aicore.token.TokenUsage

/**
 * Interface for chat memory management
 * 
 * This interface is designed to be compatible with LangChain4j's ChatMemory concept
 * while providing flexibility for future extensions.
 * 
 * Key features to support:
 * - Message storage and retrieval
 * - Token-aware eviction policies (e.g., TokenWindowChatMemory)
 * - Persistence through ChatMemoryStore
 * - Special handling of SystemMessage
 * - Integration with token usage tracking
 * 
 * Reference: https://github.com/langchain4j/langchain4j/blob/main/docs/docs/tutorials/chat-memory.md
 */
interface ChatMemory {
    /**
     * Add a message to the memory
     * @param message The message to add
     */
    fun addMessage(message: ChatMemoryMessage)
    
    /**
     * Get all messages in the memory
     * @return List of messages
     */
    fun getMessages(): List<ChatMemoryMessage>
    
    /**
     * Clear all messages from the memory
     */
    fun clear()
    
    /**
     * Get the current token usage for messages in memory
     * This is useful for token-aware eviction policies
     */
    fun getCurrentTokenUsage(): TokenUsage
}

/**
 * Represents a message in chat memory
 * This is a simplified version that can be extended to support
 * LangChain4j's message types (UserMessage, AiMessage, SystemMessage, etc.)
 */
data class ChatMemoryMessage(
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Role of the message sender
 */
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

/**
 * Interface for persisting chat memory
 * Compatible with LangChain4j's ChatMemoryStore concept
 */
interface ChatMemoryStore {
    /**
     * Get messages for a specific memory ID (e.g., conversation ID)
     */
    fun getMessages(memoryId: String): List<ChatMemoryMessage>
    
    /**
     * Update messages for a specific memory ID
     */
    fun updateMessages(memoryId: String, messages: List<ChatMemoryMessage>)
    
    /**
     * Delete messages for a specific memory ID
     */
    fun deleteMessages(memoryId: String)
}

/**
 * Eviction policy for managing memory size
 * This can be based on message count, token count, or other criteria
 */
interface EvictionPolicy {
    /**
     * Determine which messages should be evicted
     * @param messages Current messages in memory
     * @param newMessage The new message being added
     * @return List of messages after eviction
     */
    fun evict(messages: List<ChatMemoryMessage>, newMessage: ChatMemoryMessage): List<ChatMemoryMessage>
}

/**
 * Token-aware eviction policy
 * Evicts messages when token limit is exceeded
 * Compatible with LangChain4j's TokenWindowChatMemory concept
 */
class TokenWindowEvictionPolicy(
    private val maxTokens: Int,
    private val tokenCounter: (String) -> Int
) : EvictionPolicy {
    
    override fun evict(messages: List<ChatMemoryMessage>, newMessage: ChatMemoryMessage): List<ChatMemoryMessage> {
        val allMessages = messages + newMessage
        var totalTokens = allMessages.sumOf { tokenCounter(it.content) }
        
        // Keep system messages
        val systemMessages = allMessages.filter { it.role == MessageRole.SYSTEM }
        var otherMessages = allMessages.filter { it.role != MessageRole.SYSTEM }
        
        // Evict oldest messages until we're under the limit
        while (totalTokens > maxTokens && otherMessages.isNotEmpty()) {
            val evicted = otherMessages.first()
            otherMessages = otherMessages.drop(1)
            totalTokens -= tokenCounter(evicted.content)
        }
        
        return systemMessages + otherMessages
    }
}

/**
 * Simple message count-based eviction policy
 * Compatible with LangChain4j's MessageWindowChatMemory concept
 */
class MessageWindowEvictionPolicy(
    private val maxMessages: Int
) : EvictionPolicy {
    
    override fun evict(messages: List<ChatMemoryMessage>, newMessage: ChatMemoryMessage): List<ChatMemoryMessage> {
        val allMessages = messages + newMessage
        
        // Keep system messages
        val systemMessages = allMessages.filter { it.role == MessageRole.SYSTEM }
        var otherMessages = allMessages.filter { it.role != MessageRole.SYSTEM }
        
        // Keep only the most recent N messages
        if (otherMessages.size > maxMessages) {
            otherMessages = otherMessages.takeLast(maxMessages)
        }
        
        return systemMessages + otherMessages
    }
}

