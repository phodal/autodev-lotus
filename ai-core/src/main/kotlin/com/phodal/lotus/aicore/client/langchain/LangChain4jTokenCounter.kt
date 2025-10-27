package com.phodal.lotus.aicore.client.langchain

import com.phodal.lotus.aicore.config.LLMProvider
import com.phodal.lotus.aicore.token.FallbackTokenCounter
import com.phodal.lotus.aicore.token.JtokKitTokenCounter
import com.phodal.lotus.aicore.token.TokenCounter

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
            FallbackTokenCounter(modelName)
        }
    }
}