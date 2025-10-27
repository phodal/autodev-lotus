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

/**
 * A simple token counter that estimates tokens by word count
 * This is a fallback implementation when provider-specific counters are not available
 */
class SimpleTokenCounter(private val modelName: String = "unknown") : TokenCounter {
    companion object {
        // Rough estimate: 1 token ≈ 0.75 words (or 1 word ≈ 1.33 tokens)
        private const val WORDS_PER_TOKEN = 0.75
    }
    
    override fun estimateTokenCount(text: String): Int {
        if (text.isBlank()) return 0
        
        // Simple word-based estimation
        val wordCount = text.split(Regex("\\s+")).size
        return (wordCount / WORDS_PER_TOKEN).toInt()
    }
    
    override fun getModelName(): String = modelName
}

