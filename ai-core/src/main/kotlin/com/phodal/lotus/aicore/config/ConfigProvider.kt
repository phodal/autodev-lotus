package com.phodal.lotus.aicore.config

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for configuration providers
 * Allows AIServiceFactory to work with different configuration sources
 */
interface ConfigProvider {
    val currentConfig: StateFlow<LLMConfig?>
    
    fun isConfigured(): Boolean
}

