package com.phodal.lotus.aicore

import com.phodal.lotus.aicore.config.LLMConfig
import com.phodal.lotus.aicore.config.LLMConfigManager
import com.phodal.lotus.aicore.config.LLMProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AIServiceFactoryTest {
    
    private lateinit var tempDir: String
    private lateinit var configManager: LLMConfigManager
    
    @BeforeEach
    fun setUp() {
        // Create a temporary directory for testing
        tempDir = Files.createTempDirectory("lotus_factory_test_").toString()
        configManager = LLMConfigManager(tempDir)
    }
    
    @Test
    fun testInitializeFactory() {
        // Initialize the factory
        AIServiceFactory.initialize(configManager)

        // Verify the factory is initialized
        assertNotNull(AIServiceFactory.getConfigProvider())
    }
    
    @Test
    fun testCreateAIClient() {
        AIServiceFactory.initialize(configManager)

        // Create a configuration
        val config = LLMConfig(
            provider = LLMProvider.DEEPSEEK,
            apiKey = "test-key"
        )

        // Create AI client
        val client = AIServiceFactory.createAIClient(config)

        // Verify client was created
        assertNotNull(client)
        assertTrue(client.isConfigured())
    }
    
    @Test
    fun testUpdateAIClientWhenConfigChanges() = runBlocking {
        AIServiceFactory.initialize(configManager)
        
        // Initially no client
        assertNull(AIServiceFactory.getAIClient())
        
        // Save a configuration
        val config = LLMConfig(
            provider = LLMProvider.OPENAI,
            apiKey = "openai-key"
        )
        configManager.saveConfig(config)
        
        // Update the factory
        AIServiceFactory.updateAIClient()
        
        // Now should have a client
        val client = AIServiceFactory.getAIClient()
        assertNotNull(client)
        assertTrue(client?.isConfigured() ?: false)
    }
    
    @Test
    fun testIsConfiguredCheck() = runBlocking {
        AIServiceFactory.initialize(configManager)
        
        // Initially not configured
        assertFalse(AIServiceFactory.isConfigured())
        
        // Save a configuration
        val config = LLMConfig(
            provider = LLMProvider.CLAUDE,
            apiKey = "claude-key"
        )
        configManager.saveConfig(config)
        AIServiceFactory.updateAIClient()
        
        // Now should be configured
        assertTrue(AIServiceFactory.isConfigured())
    }
    
    @Test
    fun testMultipleProviders() = runBlocking {
        AIServiceFactory.initialize(configManager)
        
        // Test with DeepSeek
        var config = LLMConfig(
            provider = LLMProvider.DEEPSEEK,
            apiKey = "deepseek-key"
        )
        configManager.saveConfig(config)
        AIServiceFactory.updateAIClient()
        
        var client = AIServiceFactory.getAIClient()
        assertNotNull(client)
        
        // Switch to OpenAI
        config = LLMConfig(
            provider = LLMProvider.OPENAI,
            apiKey = "openai-key"
        )
        configManager.saveConfig(config)
        AIServiceFactory.updateAIClient()
        
        client = AIServiceFactory.getAIClient()
        assertNotNull(client)
        
        // Switch to Claude
        config = LLMConfig(
            provider = LLMProvider.CLAUDE,
            apiKey = "claude-key"
        )
        configManager.saveConfig(config)
        AIServiceFactory.updateAIClient()
        
        client = AIServiceFactory.getAIClient()
        assertNotNull(client)
        
        // Switch to Gemini
        config = LLMConfig(
            provider = LLMProvider.GEMINI,
            apiKey = "gemini-key"
        )
        configManager.saveConfig(config)
        AIServiceFactory.updateAIClient()
        
        client = AIServiceFactory.getAIClient()
        assertNotNull(client)
    }
}

