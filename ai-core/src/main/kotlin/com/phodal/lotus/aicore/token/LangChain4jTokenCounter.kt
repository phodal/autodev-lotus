package com.phodal.lotus.aicore.token

import com.phodal.lotus.aicore.config.LLMProvider

/**
 * Token counter factory for creating provider-specific token counters
 *
 * This implementation provides token counting capabilities for different LLM providers.
 *
 * Note: LangChain4j provides token counting through ChatResponse.tokenUsage() which is
 * returned from actual API calls. For estimation before API calls, we use simple heuristics.
 *
 * Future improvements:
 * - Integrate with LangChain4j's TokenCountEstimator when available
 * - Use provider-specific tokenizers (e.g., tiktoken for OpenAI)
 * - Extract actual token usage from ChatResponse after API calls
 */
object LangChain4jTokenCounter {
    /**
     * Create a token counter for the specified provider and model
     * Uses jtokkit for accurate token counting
     */
    fun create(provider: LLMProvider, modelName: String): TokenCounter {
        return try {
            // Try to use jtokkit for accurate token counting
            JtokKitTokenCounter(modelName)
        } catch (e: Exception) {
            // Fallback to simple token counter if jtokkit fails
            SimpleTokenCounter(modelName)
        }
    }
}

