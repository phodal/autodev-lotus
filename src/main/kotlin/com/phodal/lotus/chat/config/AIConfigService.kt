package com.phodal.lotus.chat.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.phodal.lotus.aicore.config.LLMConfig
import com.phodal.lotus.aicore.config.LLMProvider
import com.phodal.lotus.aicore.config.ConfigProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * IntelliJ Platform service for persisting AI configuration
 * Uses PersistentStateComponent to store configuration in IDE settings
 */
@Service
@State(
    name = "AIConfigService",
    storages = [Storage("ai-config.xml")]
)
class AIConfigService : PersistentStateComponent<AIConfigService.State>, ConfigProvider {
    
    data class State(
        var provider: String = "",
        var apiKey: String = "",
        var model: String = "",
        var temperature: Double = 0.7,
        var maxTokens: Int = 2000
    )
    
    private var state = State()

    private val _currentConfig = MutableStateFlow<LLMConfig?>(null)
    override val currentConfig: StateFlow<LLMConfig?> = _currentConfig.asStateFlow()
    
    init {
        loadConfigFromState()
    }
    
    private fun loadConfigFromState() {
        if (state.provider.isNotBlank() && state.apiKey.isNotBlank()) {
            try {
                val provider = LLMProvider.valueOf(state.provider)
                val config = LLMConfig(
                    provider = provider,
                    apiKey = state.apiKey,
                    model = state.model.ifBlank { LLMConfig.getDefaultModel(provider) },
                    temperature = state.temperature,
                    maxTokens = state.maxTokens
                )
                _currentConfig.value = config
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun saveConfig(config: LLMConfig) {
        state.provider = config.provider.name
        state.apiKey = config.apiKey
        state.model = config.model
        state.temperature = config.temperature
        state.maxTokens = config.maxTokens
        _currentConfig.value = config
    }
    
    fun clearConfig() {
        state = State()
        _currentConfig.value = null
    }

    override fun isConfigured(): Boolean = _currentConfig.value != null
    
    override fun getState(): State = state
    
    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
        loadConfigFromState()
    }
    
    companion object {
        fun getInstance(): AIConfigService {
            return com.intellij.openapi.components.service()
        }
    }
}

