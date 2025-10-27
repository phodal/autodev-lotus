package com.phodal.lotus.aicore.client

/**
 * Interface for AI client
 */
interface AIClient {
    /**
     * Send a message to the AI and get a response
     */
    suspend fun sendMessage(message: String): String
    
    /**
     * Stream a message response
     */
    suspend fun streamMessage(message: String, onChunk: (String) -> Unit)
    
    /**
     * Check if the client is properly configured
     */
    fun isConfigured(): Boolean
}

