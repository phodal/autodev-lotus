package com.phodal.lotus.aicore

import com.phodal.lotus.aicore.client.AIClient
import com.phodal.lotus.aicore.client.KoogAIClient
import com.phodal.lotus.aicore.config.LLMConfig
import com.phodal.lotus.aicore.config.LLMConfigManager

/**
 * Factory for creating AI service instances
 */
object AIServiceFactory {
    
    private var configManager: LLMConfigManager? = null
    private var aiClient: AIClient? = null
    
    /**
     * Initialize the AI service with a configuration manager
     */
    fun initialize(configManager: LLMConfigManager) {
        this.configManager = configManager
        updateAIClient()
    }
    
    /**
     * Get the current AI client
     */
    fun getAIClient(): AIClient? {
        return aiClient
    }
    
    /**
     * Create a new AI client with the given configuration
     */
    fun createAIClient(config: LLMConfig): AIClient {
        val client = KoogAIClient(config)
        aiClient = client
        return client
    }
    
    /**
     * Update the AI client based on the current configuration
     */
    fun updateAIClient() {
        val config = configManager?.currentConfig?.value
        if (config != null) {
            aiClient = KoogAIClient(config)
        }
    }
    
    /**
     * Check if AI service is configured
     */
    fun isConfigured(): Boolean {
        return configManager?.isConfigured() == true && aiClient?.isConfigured() == true
    }
    
    /**
     * Get the configuration manager
     */
    fun getConfigManager(): LLMConfigManager? {
        return configManager
    }
}

