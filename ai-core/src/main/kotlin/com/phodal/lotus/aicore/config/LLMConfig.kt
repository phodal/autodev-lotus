package com.phodal.lotus.aicore.config

/**
 * Supported LLM providers
 */
enum class LLMProvider {
    DEEPSEEK,
    OPENAI,
    CLAUDE,
    GEMINI
}

/**
 * LLM Configuration
 */
data class LLMConfig(
    val provider: LLMProvider,
    val apiKey: String,
    val model: String = getDefaultModel(provider),
    val temperature: Double = 0.7,
    val maxTokens: Int = 2000
) {
    companion object {
        fun getDefaultModel(provider: LLMProvider): String = when (provider) {
            LLMProvider.DEEPSEEK -> "deepseek-chat"
            LLMProvider.OPENAI -> "gpt-4"
            LLMProvider.CLAUDE -> "claude-3-sonnet-20240229"
            LLMProvider.GEMINI -> "gemini-pro"
        }
    }
}

