package com.phodal.lotus.aicore.token

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Service for tracking and managing token usage across LLM interactions
 * 
 * This service provides:
 * - Real-time token usage tracking
 * - Aggregation by conversation and model
 * - Observable state for UI updates
 * - Thread-safe operations
 * 
 * Future extensions:
 * - Persistence to disk
 * - Integration with ChatMemory for context window management
 * - Cost estimation based on token usage
 */
class TokenUsageTracker {
    
    private val mutex = Mutex()
    
    // All token usage records
    private val usageHistory = mutableListOf<TokenUsage>()
    
    // Current aggregated usage
    private val _aggregatedUsage = MutableStateFlow(AggregatedTokenUsage.EMPTY)
    val aggregatedUsage: StateFlow<AggregatedTokenUsage> = _aggregatedUsage.asStateFlow()
    
    // Usage by conversation ID
    private val _conversationUsage = MutableStateFlow<Map<String, TokenUsage>>(emptyMap())
    val conversationUsage: StateFlow<Map<String, TokenUsage>> = _conversationUsage.asStateFlow()
    
    /**
     * Record a new token usage
     */
    suspend fun recordUsage(usage: TokenUsage) {
        mutex.withLock {
            usageHistory.add(usage)
            updateAggregatedUsage()
        }
    }
    
    /**
     * Get token usage for a specific conversation
     */
    fun getConversationUsage(conversationId: String): TokenUsage {
        return _conversationUsage.value[conversationId] ?: TokenUsage.EMPTY
    }
    
    /**
     * Get all usage history
     */
    fun getUsageHistory(): List<TokenUsage> {
        return usageHistory.toList()
    }
    
    /**
     * Clear all usage data
     */
    suspend fun clear() {
        mutex.withLock {
            usageHistory.clear()
            _aggregatedUsage.value = AggregatedTokenUsage.EMPTY
            _conversationUsage.value = emptyMap()
        }
    }
    
    /**
     * Clear usage data for a specific conversation
     */
    suspend fun clearConversation(conversationId: String) {
        mutex.withLock {
            usageHistory.removeAll { it.conversationId == conversationId }
            updateAggregatedUsage()
        }
    }
    
    /**
     * Update aggregated usage statistics
     */
    private fun updateAggregatedUsage() {
        val totalInput = usageHistory.sumOf { it.inputTokens.toLong() }
        val totalOutput = usageHistory.sumOf { it.outputTokens.toLong() }
        val interactionCount = usageHistory.size
        
        // Group by model
        val byModel = usageHistory
            .filter { it.modelName != null }
            .groupBy { it.modelName!! }
            .mapValues { (_, usages) ->
                usages.reduce { acc, usage -> acc + usage }
            }
        
        // Group by conversation
        val byConversation = usageHistory
            .filter { it.conversationId != null }
            .groupBy { it.conversationId!! }
            .mapValues { (_, usages) ->
                usages.reduce { acc, usage -> acc + usage }
            }
        
        _aggregatedUsage.value = AggregatedTokenUsage(
            totalInputTokens = totalInput,
            totalOutputTokens = totalOutput,
            totalTokens = totalInput + totalOutput,
            interactionCount = interactionCount,
            byModel = byModel,
            byConversation = byConversation
        )
        
        _conversationUsage.value = byConversation
    }
    
    companion object {
        @Volatile
        private var instance: TokenUsageTracker? = null
        
        /**
         * Get the singleton instance
         */
        fun getInstance(): TokenUsageTracker {
            return instance ?: synchronized(this) {
                instance ?: TokenUsageTracker().also { instance = it }
            }
        }
    }
}

