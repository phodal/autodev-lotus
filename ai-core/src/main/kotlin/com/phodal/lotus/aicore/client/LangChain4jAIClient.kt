package com.phodal.lotus.aicore.client

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import com.phodal.lotus.aicore.config.LLMConfig
import com.phodal.lotus.aicore.config.LLMProvider
import java.time.Duration

/**
 * AI Client implementation using LangChain4j framework
 * Supports multiple LLM providers (DeepSeek, OpenAI, Claude, Gemini)
 *
 * This implementation uses the LangChain4j framework's ChatModel API to make real API calls
 * to various LLM providers.
 */
class LangChain4jAIClient(private val config: LLMConfig) : AIClient {

    private val chatModel: ChatModel = createChatModel()

    private fun createChatModel(): ChatModel {
        return when (config.provider) {
            LLMProvider.DEEPSEEK -> {
                // DeepSeek uses OpenAI-compatible API
                OpenAiChatModel.builder()
                    .apiKey(config.apiKey)
                    .baseUrl("https://api.deepseek.com")
                    .modelName(config.model)
                    .temperature(config.temperature)
                    .topP(1.0)
                    .maxTokens(config.maxTokens)
                    .timeout(Duration.ofSeconds(60))
                    .build()
            }
            LLMProvider.OPENAI -> {
                OpenAiChatModel.builder()
                    .apiKey(config.apiKey)
                    .modelName(config.model)
                    .temperature(config.temperature)
                    .topP(1.0)
                    .maxTokens(config.maxTokens)
                    .timeout(Duration.ofSeconds(60))
                    .build()
            }
            LLMProvider.CLAUDE -> {
                AnthropicChatModel.builder()
                    .apiKey(config.apiKey)
                    .modelName(config.model)
                    .temperature(config.temperature)
                    .maxTokens(config.maxTokens)
                    .timeout(Duration.ofSeconds(60))
                    .build()
            }
            LLMProvider.GEMINI -> {
                GoogleAiGeminiChatModel.builder()
                    .apiKey(config.apiKey)
                    .modelName(config.model)
                    .temperature(config.temperature)
                    .maxOutputTokens(config.maxTokens)
                    .build()
            }
        }
    }

    override suspend fun sendMessage(message: String): String {
        return try {
            chatModel.chat(message)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get response from ${config.provider}: ${e.message}", e)
        }
    }

    override suspend fun streamMessage(message: String, onChunk: (String) -> Unit) {
        return try {
            val response = sendMessage(message)
            onChunk(response)
        } catch (e: Exception) {
            throw RuntimeException("Failed to stream response from ${config.provider}: ${e.message}", e)
        }
    }

    override fun isConfigured(): Boolean {
        return config.apiKey.isNotBlank()
    }
}

