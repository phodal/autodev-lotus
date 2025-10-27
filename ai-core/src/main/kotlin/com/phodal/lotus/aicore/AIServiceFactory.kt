package com.phodal.lotus.aicore

import com.phodal.lotus.aicore.client.AIClient
import com.phodal.lotus.aicore.client.KoogAIClient
import com.phodal.lotus.aicore.config.LLMConfig
import com.phodal.lotus.aicore.config.ConfigProvider

/**
 * Factory for creating AI service instances
 * Works with any ConfigProvider implementation
 */
object AIServiceFactory {

    private var configProvider: ConfigProvider? = null
    private var aiClient: AIClient? = null

    /**
     * Initialize the AI service with a configuration provider
     */
    fun initialize(configProvider: ConfigProvider) {
        this.configProvider = configProvider
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
        val config = configProvider?.currentConfig?.value
        if (config != null) {
            aiClient = KoogAIClient(config)
        }
    }

    /**
     * Check if AI service is configured
     */
    fun isConfigured(): Boolean {
        return configProvider?.isConfigured() == true && aiClient?.isConfigured() == true
    }

    /**
     * Get the configuration provider
     */
    fun getConfigProvider(): ConfigProvider? {
        return configProvider
    }
}

