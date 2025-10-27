package com.phodal.lotus.aicore.client

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel
import com.phodal.lotus.aicore.config.LLMConfig
import com.phodal.lotus.aicore.config.LLMProvider
import java.time.Duration
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * AI Client implementation using LangChain4j framework
 * Supports multiple LLM providers (DeepSeek, OpenAI, Claude, Gemini)
 *
 * This implementation uses the LangChain4j framework's ChatModel API to make real API calls
 * to various LLM providers.
 */
class LangChain4jAIClient(private val config: LLMConfig) : AIClient {

    private val chatModel: ChatModel = createChatModel()
    private val streamingChatModel: StreamingChatModel = createStreamingChatModel()

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

    private fun createStreamingChatModel(): StreamingChatModel {
        return when (config.provider) {
            LLMProvider.DEEPSEEK -> {
                // DeepSeek uses OpenAI-compatible API
                OpenAiStreamingChatModel.builder()
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
                OpenAiStreamingChatModel.builder()
                    .apiKey(config.apiKey)
                    .modelName(config.model)
                    .temperature(config.temperature)
                    .topP(1.0)
                    .maxTokens(config.maxTokens)
                    .timeout(Duration.ofSeconds(60))
                    .build()
            }
            LLMProvider.CLAUDE -> {
                AnthropicStreamingChatModel.builder()
                    .apiKey(config.apiKey)
                    .modelName(config.model)
                    .temperature(config.temperature)
                    .maxTokens(config.maxTokens)
                    .timeout(Duration.ofSeconds(60))
                    .build()
            }
            LLMProvider.GEMINI -> {
                GoogleAiGeminiStreamingChatModel.builder()
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
        return suspendCancellableCoroutine { continuation ->
            try {
                streamingChatModel.chat(
                    message,
                    object : StreamingChatResponseHandler {
                        override fun onPartialResponse(partialResponse: String) {
                            onChunk(partialResponse)
                        }

                        override fun onCompleteResponse(completeResponse: ChatResponse) {
                            // Stream completed successfully
                            continuation.resume(Unit)
                        }

                        override fun onError(error: Throwable) {
                            continuation.resumeWithException(
                                RuntimeException("Failed to stream response from ${config.provider}: ${error.message}", error)
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                continuation.resumeWithException(
                    RuntimeException("Failed to stream response from ${config.provider}: ${e.message}", e)
                )
            }
        }
    }

    override fun isConfigured(): Boolean {
        return config.apiKey.isNotBlank()
    }
}

