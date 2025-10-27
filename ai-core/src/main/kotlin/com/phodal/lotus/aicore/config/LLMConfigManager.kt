package com.phodal.lotus.aicore.config

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Manages LLM configurations
 * Stores and retrieves configurations from local storage
 */
class LLMConfigManager(private val configDir: String = System.getProperty("user.home") + "/.lotus/ai") {
    
    private val _currentConfig = MutableStateFlow<LLMConfig?>(null)
    val currentConfig: StateFlow<LLMConfig?> = _currentConfig.asStateFlow()
    
    private val configFile = File(configDir, "llm_config.properties")
    
    init {
        ensureConfigDirExists()
        loadConfig()
    }
    
    private fun ensureConfigDirExists() {
        File(configDir).mkdirs()
    }
    
    /**
     * Load configuration from file
     */
    fun loadConfig() {
        if (configFile.exists()) {
            try {
                val properties = java.util.Properties()
                properties.load(configFile.inputStream())
                
                val provider = properties.getProperty("provider")?.let { LLMProvider.valueOf(it) }
                val apiKey = properties.getProperty("apiKey")
                val model = properties.getProperty("model")
                val temperature = properties.getProperty("temperature")?.toDoubleOrNull() ?: 0.7
                val maxTokens = properties.getProperty("maxTokens")?.toIntOrNull() ?: 2000
                
                if (provider != null && apiKey != null) {
                    val config = LLMConfig(
                        provider = provider,
                        apiKey = apiKey,
                        model = model ?: LLMConfig.getDefaultModel(provider),
                        temperature = temperature,
                        maxTokens = maxTokens
                    )
                    _currentConfig.value = config
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Save configuration to file
     */
    fun saveConfig(config: LLMConfig) {
        try {
            val properties = java.util.Properties()
            properties.setProperty("provider", config.provider.name)
            properties.setProperty("apiKey", config.apiKey)
            properties.setProperty("model", config.model)
            properties.setProperty("temperature", config.temperature.toString())
            properties.setProperty("maxTokens", config.maxTokens.toString())
            
            configFile.parentFile?.mkdirs()
            properties.store(configFile.outputStream(), "LLM Configuration")
            
            _currentConfig.value = config
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Update current configuration
     */
    fun updateConfig(config: LLMConfig) {
        saveConfig(config)
    }
    
    /**
     * Clear configuration
     */
    fun clearConfig() {
        if (configFile.exists()) {
            configFile.delete()
        }
        _currentConfig.value = null
    }
    
    /**
     * Check if configuration is set
     */
    fun isConfigured(): Boolean = _currentConfig.value != null
}

