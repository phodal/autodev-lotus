package com.phodal.lotus.aicore.client

import com.phodal.lotus.aicore.token.TokenUsage

/**
 * Result of an AI message call with token usage information
 */
data class AIMessageResult(
    val content: String,
    val tokenUsage: TokenUsage? = null
)

/**
 * Interface for AI client
 */
interface AIClient {
    /**
     * Send a message to the AI and get a response
     * @return AIMessageResult containing the response and token usage
     */
    suspend fun sendMessage(message: String): AIMessageResult

    /**
     * Stream a message response
     * @param message The message to send
     * @param onChunk Callback for each chunk of the response
     * @return TokenUsage information after streaming completes
     */
    suspend fun streamMessage(message: String, onChunk: (String) -> Unit): TokenUsage?

    /**
     * Check if the client is properly configured
     */
    fun isConfigured(): Boolean
}

