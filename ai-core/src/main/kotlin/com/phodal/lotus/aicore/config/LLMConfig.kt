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
        /**
         * Get available models for a provider (updated to commonly used, current models)
         */
        fun getAvailableModels(provider: LLMProvider): List<String> = when (provider) {
            LLMProvider.DEEPSEEK -> listOf(
                "deepseek-chat",
                "deepseek-coder",
                // Reasoning-capable model
                "deepseek-reasoner"
            )
            LLMProvider.OPENAI -> listOf(
                "gpt-5",
                "gpt-4.5",
                "gpt-4o",
                "gpt-4o-mini",
                "gpt-4-turbo"
            )
            LLMProvider.CLAUDE -> listOf(
                "claude-4.5-sonnet-latest",
                // Use Anthropic "-latest" aliases where available to track current releases
                "claude-3.7-sonnet-latest",
                "claude-3.5-sonnet-latest",
                "claude-3-opus-latest",
                "claude-3-haiku-latest"
            )
            LLMProvider.GEMINI -> listOf(
                "gemini-2.5-pro",
                "gemini-2.0-pro",
                "gemini-2.0-flash",
                "gemini-1.5-pro",
                "gemini-1.5-flash"
            )
        }

        /**
         * Get default model for a provider
         */
        fun getDefaultModel(provider: LLMProvider): String = when (provider) {
            LLMProvider.DEEPSEEK -> "deepseek-chat"
            LLMProvider.OPENAI -> "gpt-5"
            // Prefer the alias to follow the latest Sonnet 4.5 drop automatically
            LLMProvider.CLAUDE -> "claude-4.5-sonnet-latest"
            LLMProvider.GEMINI -> "gemini-2.5-pro"
        }
    }
}
