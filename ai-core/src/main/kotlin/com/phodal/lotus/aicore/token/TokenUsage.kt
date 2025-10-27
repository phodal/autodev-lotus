package com.phodal.lotus.aicore.token

import java.time.Instant

/**
 * Represents token usage statistics for a single LLM interaction
 * 
 * This data class captures the token consumption details for both input and output,
 * which is essential for cost tracking and usage monitoring.
 */
data class TokenUsage(
    /**
     * Number of tokens in the input/prompt
     */
    val inputTokens: Int = 0,
    
    /**
     * Number of tokens in the output/response
     */
    val outputTokens: Int = 0,
    
    /**
     * Total number of tokens (input + output)
     */
    val totalTokens: Int = inputTokens + outputTokens,
    
    /**
     * Timestamp when this usage was recorded
     */
    val timestamp: Instant = Instant.now(),
    
    /**
     * Optional model name that was used
     */
    val modelName: String? = null,
    
    /**
     * Optional conversation/session ID for grouping
     */
    val conversationId: String? = null
) {
    companion object {
        /**
         * Empty token usage (no tokens consumed)
         */
        val EMPTY = TokenUsage(0, 0, 0)
        
        /**
         * Create a TokenUsage from input and output counts
         */
        fun of(inputTokens: Int, outputTokens: Int, modelName: String? = null, conversationId: String? = null): TokenUsage {
            return TokenUsage(
                inputTokens = inputTokens,
                outputTokens = outputTokens,
                totalTokens = inputTokens + outputTokens,
                modelName = modelName,
                conversationId = conversationId
            )
        }
    }
    
    /**
     * Combine this token usage with another
     */
    operator fun plus(other: TokenUsage): TokenUsage {
        return TokenUsage(
            inputTokens = this.inputTokens + other.inputTokens,
            outputTokens = this.outputTokens + other.outputTokens,
            totalTokens = this.totalTokens + other.totalTokens,
            timestamp = this.timestamp, // Keep the earlier timestamp
            modelName = this.modelName ?: other.modelName,
            conversationId = this.conversationId ?: other.conversationId
        )
    }
}

/**
 * Aggregated token usage statistics
 * Useful for displaying cumulative usage over time or across conversations
 */
data class AggregatedTokenUsage(
    /**
     * Total input tokens across all interactions
     */
    val totalInputTokens: Long = 0,
    
    /**
     * Total output tokens across all interactions
     */
    val totalOutputTokens: Long = 0,
    
    /**
     * Total tokens (input + output)
     */
    val totalTokens: Long = totalInputTokens + totalOutputTokens,
    
    /**
     * Number of LLM interactions/calls
     */
    val interactionCount: Int = 0,
    
    /**
     * Breakdown by model name
     */
    val byModel: Map<String, TokenUsage> = emptyMap(),
    
    /**
     * Breakdown by conversation ID
     */
    val byConversation: Map<String, TokenUsage> = emptyMap()
) {
    companion object {
        val EMPTY = AggregatedTokenUsage()
    }
}

