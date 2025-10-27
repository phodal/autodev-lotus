package com.phodal.lotus.aicore.client

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.streaming.StreamFrame
import com.phodal.lotus.aicore.config.LLMConfig
import com.phodal.lotus.aicore.config.LLMProvider
import kotlinx.coroutines.flow.Flow

/**
 * AI Client implementation using Koog framework
 * Supports multiple LLM providers (DeepSeek, OpenAI, Claude, Gemini)
 *
 * This implementation uses the Koog framework's LLM clients to make real API calls
 * to various LLM providers.
 */
class KoogAIClient(private val config: LLMConfig) : AIClient {

    private val llmClient = createLLMClient()

    private fun createLLMClient() = when (config.provider) {
        LLMProvider.DEEPSEEK -> DeepSeekLLMClient(apiKey = config.apiKey)
        LLMProvider.OPENAI -> OpenAILLMClient(apiKey = config.apiKey)
        LLMProvider.CLAUDE -> AnthropicLLMClient(apiKey = config.apiKey)
        LLMProvider.GEMINI -> GoogleLLMClient(apiKey = config.apiKey)
    }

    private fun createLLModel(): LLModel {
        val koogProvider = when (config.provider) {
            LLMProvider.DEEPSEEK -> ai.koog.prompt.llm.LLMProvider.DeepSeek
            LLMProvider.OPENAI ->ai.koog.prompt.llm.LLMProvider.OpenAI
            LLMProvider.CLAUDE -> ai.koog.prompt.llm.LLMProvider.Anthropic
            LLMProvider.GEMINI -> ai.koog.prompt.llm.LLMProvider.Google
        }

        return LLModel(
            provider = koogProvider,
            id = config.model,
            capabilities = listOf(LLMCapability.Tools),
            contextLength = 128000L,
            maxOutputTokens = config.maxTokens.toLong()
        )
    }

    override suspend fun sendMessage(message: String): String {
        return try {
            val prompt = prompt("") {
                user(message)
            }

            val model = createLLModel()

            val responses = llmClient.execute(
                prompt = prompt,
                model = model
            )

            // Extract and combine all response messages
            responses.joinToString("\n") { response ->
                try {
                    response.content
                } catch (e: Exception) {
                    response.toString()
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to get response from ${config.provider}: ${e.message}", e)
        }
    }

    override suspend fun streamMessage(message: String, onChunk: (String) -> Unit) {
        return try {
            val prompt = prompt("") {
                user(message)
            }

            val model = createLLModel()

            val streamFlow: Flow<StreamFrame> = llmClient.executeStreaming(
                prompt = prompt,
                model = model
            )

            streamFlow.collect { streamFrame ->
                try {
                    val content = when(streamFrame) {
                        is StreamFrame.Append -> streamFrame.text
                        is StreamFrame.End -> streamFrame.toString()
                        is StreamFrame.ToolCall -> streamFrame.content
                    }
                    onChunk(content)
                } catch (e: Exception) {
                    onChunk(streamFrame.toString())
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to stream response from ${config.provider}: ${e.message}", e)
        }
    }

    override fun isConfigured(): Boolean {
        return config.apiKey.isNotBlank()
    }
}

