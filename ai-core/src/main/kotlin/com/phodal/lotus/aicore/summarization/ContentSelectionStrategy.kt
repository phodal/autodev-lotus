package com.phodal.lotus.aicore.summarization

import com.phodal.lotus.aicore.token.TokenCounter

/**
 * Result of content selection
 */
data class SelectedContent(
    val messages: List<ConversationMessage>,
    val totalTokens: Int,
    val userMessageCount: Int,
    val aiMessageCount: Int
)

/**
 * Strategy for selecting which messages to include in the summarization context
 * 
 * This interface allows different strategies for choosing which messages to include
 * based on token limits and other criteria.
 */
interface ContentSelectionStrategy {
    /**
     * Select messages to include in the summarization context
     * 
     * @param messages All messages in the conversation
     * @param maxTokens Maximum tokens to include
     * @param tokenCounter Token counter for calculating token counts
     * @return SelectedContent with selected messages and metadata
     */
    fun selectMessages(
        messages: List<ConversationMessage>,
        maxTokens: Int,
        tokenCounter: TokenCounter
    ): SelectedContent
}

/**
 * Default content selection strategy
 * 
 * This strategy:
 * 1. Always includes the first user message (to establish context)
 * 2. Prioritizes user messages over AI responses
 * 3. Works backwards from the most recent messages
 * 4. Respects the token limit
 */
class DefaultContentSelectionStrategy : ContentSelectionStrategy {
    
    override fun selectMessages(
        messages: List<ConversationMessage>,
        maxTokens: Int,
        tokenCounter: TokenCounter
    ): SelectedContent {
        if (messages.isEmpty()) {
            return SelectedContent(emptyList(), 0, 0, 0)
        }
        
        val selected = mutableListOf<ConversationMessage>()
        var totalTokens = 0
        var userMessageCount = 0
        var aiMessageCount = 0
        
        // Always include the first user message for context
        val firstUserMessage = messages.firstOrNull { it.isUserMessage }
        if (firstUserMessage != null) {
            val tokens = tokenCounter.estimateTokenCount(firstUserMessage.content)
            if (tokens <= maxTokens) {
                selected.add(firstUserMessage)
                totalTokens += tokens
                userMessageCount++
            }
        }
        
        // Work backwards from the most recent messages
        // Prioritize user messages
        val userMessages = messages.filter { it.isUserMessage && it != firstUserMessage }
        val aiMessages = messages.filter { !it.isUserMessage }
        
        // Add user messages first (in reverse order, most recent first)
        for (msg in userMessages.asReversed()) {
            val tokens = tokenCounter.estimateTokenCount(msg.content)
            if (totalTokens + tokens <= maxTokens) {
                selected.add(msg)
                totalTokens += tokens
                userMessageCount++
            } else {
                break
            }
        }
        
        // Then add AI messages if space allows
        for (msg in aiMessages.asReversed()) {
            val tokens = tokenCounter.estimateTokenCount(msg.content)
            if (totalTokens + tokens <= maxTokens) {
                selected.add(msg)
                totalTokens += tokens
                aiMessageCount++
            } else {
                break
            }
        }
        
        // Sort by timestamp to maintain chronological order
        selected.sortBy { it.timestamp }
        
        return SelectedContent(
            messages = selected,
            totalTokens = totalTokens,
            userMessageCount = userMessageCount,
            aiMessageCount = aiMessageCount
        )
    }
}

/**
 * Recency-focused content selection strategy
 * 
 * This strategy prioritizes recent messages over older ones,
 * regardless of whether they are user or AI messages.
 */
class RecencyFocusedStrategy : ContentSelectionStrategy {
    
    override fun selectMessages(
        messages: List<ConversationMessage>,
        maxTokens: Int,
        tokenCounter: TokenCounter
    ): SelectedContent {
        if (messages.isEmpty()) {
            return SelectedContent(emptyList(), 0, 0, 0)
        }
        
        val selected = mutableListOf<ConversationMessage>()
        var totalTokens = 0
        var userMessageCount = 0
        var aiMessageCount = 0
        
        // Work backwards from the most recent messages
        for (msg in messages.asReversed()) {
            val tokens = tokenCounter.estimateTokenCount(msg.content)
            if (totalTokens + tokens <= maxTokens) {
                selected.add(msg)
                totalTokens += tokens
                if (msg.isUserMessage) userMessageCount++ else aiMessageCount++
            } else {
                break
            }
        }
        
        // Sort by timestamp to maintain chronological order
        selected.sortBy { it.timestamp }
        
        return SelectedContent(
            messages = selected,
            totalTokens = totalTokens,
            userMessageCount = userMessageCount,
            aiMessageCount = aiMessageCount
        )
    }
}

