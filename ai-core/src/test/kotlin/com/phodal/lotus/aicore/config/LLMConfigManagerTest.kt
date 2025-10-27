package com.phodal.lotus.aicore.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class LLMConfigManagerTest {
    
    private lateinit var tempDir: String
    private lateinit var configManager: LLMConfigManager
    
    @BeforeEach
    fun setUp() {
        // Create a temporary directory for testing
        tempDir = Files.createTempDirectory("lotus_test_").toString()
        configManager = LLMConfigManager(tempDir)
    }
    
    @AfterEach
    fun tearDown() {
        // Clean up temporary directory
        File(tempDir).deleteRecursively()
    }
    
    @Test
    fun testSaveAndLoadConfig() = runBlocking {
        // Create a test configuration
        val testConfig = LLMConfig(
            provider = LLMProvider.DEEPSEEK,
            apiKey = "test-api-key-123",
            model = "deepseek-chat",
            temperature = 0.7,
            maxTokens = 2000
        )
        
        // Save the configuration
        configManager.saveConfig(testConfig)
        
        // Verify the configuration was saved
        val savedConfig = configManager.currentConfig.first()
        assertNotNull(savedConfig)
        assertEquals(LLMProvider.DEEPSEEK, savedConfig?.provider)
        assertEquals("test-api-key-123", savedConfig?.apiKey)
        assertEquals("deepseek-chat", savedConfig?.model)
    }
    
    @Test
    fun testLoadConfigFromFile() = runBlocking {
        // Create a test configuration
        val testConfig = LLMConfig(
            provider = LLMProvider.OPENAI,
            apiKey = "openai-key-456",
            model = "gpt-4",
            temperature = 0.8,
            maxTokens = 4000
        )
        
        // Save the configuration
        configManager.saveConfig(testConfig)
        
        // Create a new manager instance to test loading from file
        val newManager = LLMConfigManager(tempDir)
        val loadedConfig = newManager.currentConfig.first()
        
        assertNotNull(loadedConfig)
        assertEquals(LLMProvider.OPENAI, loadedConfig?.provider)
        assertEquals("openai-key-456", loadedConfig?.apiKey)
    }
    
    @Test
    fun testClearConfig() = runBlocking {
        // Save a configuration
        val testConfig = LLMConfig(
            provider = LLMProvider.CLAUDE,
            apiKey = "claude-key-789"
        )
        configManager.saveConfig(testConfig)
        
        // Verify it was saved
        var currentConfig = configManager.currentConfig.first()
        assertNotNull(currentConfig)
        
        // Clear the configuration
        configManager.clearConfig()
        
        // Verify it was cleared
        currentConfig = configManager.currentConfig.first()
        assertNull(currentConfig)
    }
    
    @Test
    fun testIsConfigured() = runBlocking {
        // Initially should not be configured
        assertFalse(configManager.isConfigured())
        
        // Save a configuration
        val testConfig = LLMConfig(
            provider = LLMProvider.GEMINI,
            apiKey = "gemini-key-000"
        )
        configManager.saveConfig(testConfig)
        
        // Now should be configured
        assertTrue(configManager.isConfigured())
        
        // Clear and verify
        configManager.clearConfig()
        assertFalse(configManager.isConfigured())
    }
    
    @Test
    fun testUpdateConfig() = runBlocking {
        // Save initial configuration
        val initialConfig = LLMConfig(
            provider = LLMProvider.DEEPSEEK,
            apiKey = "initial-key"
        )
        configManager.saveConfig(initialConfig)
        
        // Update with new configuration
        val updatedConfig = LLMConfig(
            provider = LLMProvider.OPENAI,
            apiKey = "updated-key"
        )
        configManager.updateConfig(updatedConfig)
        
        // Verify the update
        val currentConfig = configManager.currentConfig.first()
        assertEquals(LLMProvider.OPENAI, currentConfig?.provider)
        assertEquals("updated-key", currentConfig?.apiKey)
    }
    
    @Test
    fun testSingletonPattern() {
        // Get two instances
        val instance1 = LLMConfigManager.getInstance()
        val instance2 = LLMConfigManager.getInstance()
        
        // They should be the same instance
        assertSame(instance1, instance2)
    }
    
    @Test
    fun testConfigFileLocation() {
        val configFile = File(tempDir, "llm_config.properties")

        // Initially file should not exist
        assertFalse(configFile.exists())

        // Save a configuration
        val testConfig = LLMConfig(
            provider = LLMProvider.DEEPSEEK,
            apiKey = "test-key"
        )
        configManager.saveConfig(testConfig)

        // Now file should exist
        assertTrue(configFile.exists())

        // Verify file contains the configuration
        val content = configFile.readText()
        assertTrue(content.contains("provider=DEEPSEEK"))
        assertTrue(content.contains("apiKey=test-key"))
    }

    @Test
    fun testGetAvailableModels() {
        // Test DeepSeek models
        val deepseekModels = LLMConfig.getAvailableModels(LLMProvider.DEEPSEEK)
        assertTrue(deepseekModels.contains("deepseek-chat"))
        assertTrue(deepseekModels.contains("deepseek-coder"))
        assertTrue(deepseekModels.isNotEmpty())

        // Test OpenAI models
        val openaiModels = LLMConfig.getAvailableModels(LLMProvider.OPENAI)
        assertTrue(openaiModels.contains("gpt-4o"))
        assertTrue(openaiModels.contains("gpt-4o-mini"))
        assertTrue(openaiModels.isNotEmpty())

        // Test Claude models
        val claudeModels = LLMConfig.getAvailableModels(LLMProvider.CLAUDE)
        assertTrue(claudeModels.contains("claude-4.5-sonnet-latest"))
        assertTrue(claudeModels.contains("claude-3-opus-latest"))
        assertTrue(claudeModels.isNotEmpty())

        // Test Gemini models
        val geminiModels = LLMConfig.getAvailableModels(LLMProvider.GEMINI)
        assertTrue(geminiModels.contains("gemini-2.0-flash"))
        assertTrue(geminiModels.contains("gemini-1.5-pro"))
        assertTrue(geminiModels.isNotEmpty())
    }

    @Test
    fun testGetDefaultModels() {
        // Test default models for each provider
        assertEquals("deepseek-chat", LLMConfig.getDefaultModel(LLMProvider.DEEPSEEK))
        assertEquals("gpt-5", LLMConfig.getDefaultModel(LLMProvider.OPENAI))
        assertEquals("claude-4.5-sonnet-latest", LLMConfig.getDefaultModel(LLMProvider.CLAUDE))
        assertEquals("gemini-2.5-pro", LLMConfig.getDefaultModel(LLMProvider.GEMINI))
    }

    @Test
    fun testCustomModelConfiguration() = runBlocking {
        // Test saving with custom model
        val customModel = "my-custom-model-v1"
        val testConfig = LLMConfig(
            provider = LLMProvider.OPENAI,
            apiKey = "test-key",
            model = customModel
        )
        configManager.saveConfig(testConfig)

        // Verify custom model was saved
        val savedConfig = configManager.currentConfig.first()
        assertNotNull(savedConfig)
        assertEquals(customModel, savedConfig?.model)
    }
}

