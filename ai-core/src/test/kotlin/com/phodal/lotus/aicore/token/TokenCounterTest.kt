package com.phodal.lotus.aicore.token

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TokenCounterTest {
    
    @Test
    fun testSimpleTokenCounter() {
        val counter = SimpleTokenCounter("test-model")
        
        val text = "Hello world this is a test"
        val tokenCount = counter.estimateTokenCount(text)
        
        // Simple counter estimates ~1.33 tokens per word
        // 6 words ≈ 8 tokens
        assertTrue(tokenCount > 0)
        assertEquals("test-model", counter.getModelName())
    }
    
    @Test
    fun testSimpleTokenCounterEmptyText() {
        val counter = SimpleTokenCounter()
        
        assertEquals(0, counter.estimateTokenCount(""))
        assertEquals(0, counter.estimateTokenCount("   "))
    }
    
    @Test
    fun testSimpleTokenCounterMultipleMessages() {
        val counter = SimpleTokenCounter()
        
        val messages = listOf(
            "Hello world",
            "How are you?",
            "I am fine"
        )
        
        val totalTokens = counter.estimateTokenCount(messages)
        assertTrue(totalTokens > 0)
    }
}

