package com.phodal.lotus.aicore.token

/**
 * Interface for counting tokens in text
 * 
 * This abstraction allows for different token counting implementations
 * for different LLM providers, as each may use different tokenization methods.
 * 
 * Future extensions can support other languages beyond Kotlin/Java.
 */
interface TokenCounter {
    /**
     * Estimate the number of tokens in the given text
     * 
     * @param text The text to count tokens for
     * @return Estimated number of tokens
     */
    fun estimateTokenCount(text: String): Int
    
    /**
     * Estimate the number of tokens in a list of messages
     * 
     * @param messages List of message texts
     * @return Estimated total number of tokens
     */
    fun estimateTokenCount(messages: List<String>): Int {
        return messages.sumOf { estimateTokenCount(it) }
    }
    
    /**
     * Get the model name this counter is configured for
     */
    fun getModelName(): String
}

