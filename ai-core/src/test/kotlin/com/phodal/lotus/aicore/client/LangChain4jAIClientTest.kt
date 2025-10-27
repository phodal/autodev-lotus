package com.phodal.lotus.aicore.client

import com.phodal.lotus.aicore.config.LLMConfig
import com.phodal.lotus.aicore.config.LLMProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class LangChain4jAIClientTest {

    private lateinit var client: LangChain4jAIClient

    @BeforeEach
    fun setUp() {
        // Create a test configuration with a dummy API key
        val config = LLMConfig(
            provider = LLMProvider.DEEPSEEK,
            apiKey = "test-api-key",
            model = "deepseek-chat"
        )
        client = LangChain4jAIClient(config)
    }

    @Test
    fun testClientIsConfigured() {
        assertTrue(client.isConfigured())
    }

    @Test
    fun testClientNotConfiguredWithEmptyKey() {
        val config = LLMConfig(
            provider = LLMProvider.OPENAI,
            apiKey = "",
            model = "gpt-4o"
        )
        val unconfiguredClient = LangChain4jAIClient(config)
        assertFalse(unconfiguredClient.isConfigured())
    }

    @Test
    fun testClientCreationWithDifferentProviders() {
        val providers = listOf(
            LLMProvider.DEEPSEEK,
            LLMProvider.OPENAI,
            LLMProvider.CLAUDE,
            LLMProvider.GEMINI
        )

        providers.forEach { provider ->
            val config = LLMConfig(
                provider = provider,
                apiKey = "test-key-$provider",
                model = LLMConfig.getDefaultModel(provider)
            )
            val testClient = LangChain4jAIClient(config)
            assertTrue(testClient.isConfigured())
        }
    }

    @Test
    fun testDefaultModels() {
        assertEquals("deepseek-chat", LLMConfig.getDefaultModel(LLMProvider.DEEPSEEK))
        assertEquals("gpt-5", LLMConfig.getDefaultModel(LLMProvider.OPENAI))
        assertEquals("claude-4.5-sonnet-latest", LLMConfig.getDefaultModel(LLMProvider.CLAUDE))
        assertEquals("gemini-2.5-pro", LLMConfig.getDefaultModel(LLMProvider.GEMINI))
    }
}

