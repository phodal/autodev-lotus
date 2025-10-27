package com.phodal.lotus.aicore.token

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class JtokKitTokenCounterTest {
    
    @Test
    fun testJtokKitTokenCounterCreation() {
        val counter = JtokKitTokenCounter("gpt-4")
        assertEquals("gpt-4", counter.getModelName())
    }
    
    @Test
    fun testTokenCountingForGPT4() {
        val counter = JtokKitTokenCounter("gpt-4")
        
        val text = "Hello world this is a test"
        val tokenCount = counter.estimateTokenCount(text)
        
        // Should return a positive number
        assertTrue(tokenCount > 0)
        println("Token count for '$text': $tokenCount")
    }
    
    @Test
    fun testTokenCountingForClaude() {
        val counter = JtokKitTokenCounter("claude-3-5-sonnet-latest")
        
        val text = "This is a test message for Claude"
        val tokenCount = counter.estimateTokenCount(text)
        
        assertTrue(tokenCount > 0)
        println("Token count for Claude: $tokenCount")
    }
    
    @Test
    fun testTokenCountingForDeepSeek() {
        val counter = JtokKitTokenCounter("deepseek-chat")
        
        val text = "DeepSeek model test"
        val tokenCount = counter.estimateTokenCount(text)
        
        assertTrue(tokenCount > 0)
        println("Token count for DeepSeek: $tokenCount")
    }
    
    @Test
    fun testEmptyTextTokenCount() {
        val counter = JtokKitTokenCounter("gpt-4")
        
        assertEquals(0, counter.estimateTokenCount(""))
        assertEquals(0, counter.estimateTokenCount("   "))
    }
    
    @Test
    fun testLongerTextTokenCount() {
        val counter = JtokKitTokenCounter("gpt-4")
        
        val shortText = "Hello"
        val longText = "Hello world this is a much longer text that should have more tokens than the short text"
        
        val shortTokens = counter.estimateTokenCount(shortText)
        val longTokens = counter.estimateTokenCount(longText)
        
        assertTrue(longTokens > shortTokens)
        println("Short text tokens: $shortTokens, Long text tokens: $longTokens")
    }
    
    @Test
    fun testMultipleMessagesTokenCount() {
        val counter = JtokKitTokenCounter("gpt-4")
        
        val messages = listOf(
            "Hello, how are you?",
            "I'm doing great, thanks for asking!",
            "That's wonderful to hear."
        )
        
        val totalTokens = counter.estimateTokenCount(messages)
        
        assertTrue(totalTokens > 0)
        println("Total tokens for multiple messages: $totalTokens")
    }
}

