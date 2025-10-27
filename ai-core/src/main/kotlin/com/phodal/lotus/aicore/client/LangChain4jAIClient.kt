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
import com.phodal.lotus.aicore.token.TokenUsage
import com.phodal.lotus.aicore.token.LangChain4jTokenCounter
import com.phodal.lotus.aicore.token.TokenUsageTracker
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
class LangChain4jAIClient(
    private val config: LLMConfig,
    private val conversationId: String? = null
) : AIClient {

    private val chatModel: ChatModel = createChatModel()
    private val streamingChatModel: StreamingChatModel = createStreamingChatModel()
    private val tokenCounter = LangChain4jTokenCounter.create(config.provider, config.model)
    private val tokenUsageTracker = TokenUsageTracker.getInstance()

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

    override suspend fun sendMessage(message: String): AIMessageResult {
        return try {
            // For non-streaming calls, we estimate tokens
            // LangChain4j's chat() method doesn't return ChatResponse with token usage
            val inputTokens = tokenCounter.estimateTokenCount(message)
            val response = chatModel.chat(message)
            val outputTokens = tokenCounter.estimateTokenCount(response)

            val tokenUsage = TokenUsage.of(
                inputTokens = inputTokens,
                outputTokens = outputTokens,
                modelName = config.model,
                conversationId = conversationId
            )

            // Record token usage
            tokenUsageTracker.recordUsage(tokenUsage)

            AIMessageResult(content = response, tokenUsage = tokenUsage)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get response from ${config.provider}: ${e.message}", e)
        }
    }

    override suspend fun streamMessage(message: String, onChunk: (String) -> Unit): TokenUsage? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputTokens = tokenCounter.estimateTokenCount(message)
                val responseBuilder = StringBuilder()

                streamingChatModel.chat(
                    message,
                    object : StreamingChatResponseHandler {
                        override fun onPartialResponse(partialResponse: String) {
                            responseBuilder.append(partialResponse)
                            onChunk(partialResponse)
                        }

                        override fun onCompleteResponse(completeResponse: ChatResponse) {
                            // Calculate token usage
                            val fullResponse = responseBuilder.toString()
                            val outputTokens = tokenCounter.estimateTokenCount(fullResponse)

                            val tokenUsage = TokenUsage.of(
                                inputTokens = inputTokens,
                                outputTokens = outputTokens,
                                modelName = config.model,
                                conversationId = conversationId
                            )

                            // Record token usage asynchronously
                            kotlinx.coroutines.runBlocking {
                                tokenUsageTracker.recordUsage(tokenUsage)
                            }

                            // Stream completed successfully
                            continuation.resume(tokenUsage)
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

