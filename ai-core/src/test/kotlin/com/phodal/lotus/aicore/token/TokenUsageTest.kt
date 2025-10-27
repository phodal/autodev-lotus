package com.phodal.lotus.aicore.token

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TokenUsageTest {
    
    @Test
    fun testTokenUsageCreation() {
        val usage = TokenUsage.of(100, 200, "gpt-4", "conv-123")
        
        assertEquals(100, usage.inputTokens)
        assertEquals(200, usage.outputTokens)
        assertEquals(300, usage.totalTokens)
        assertEquals("gpt-4", usage.modelName)
        assertEquals("conv-123", usage.conversationId)
    }
    
    @Test
    fun testTokenUsageAddition() {
        val usage1 = TokenUsage.of(100, 200, "gpt-4", "conv-123")
        val usage2 = TokenUsage.of(50, 75, "gpt-4", "conv-123")
        
        val combined = usage1 + usage2
        
        assertEquals(150, combined.inputTokens)
        assertEquals(275, combined.outputTokens)
        assertEquals(425, combined.totalTokens)
    }
    
    @Test
    fun testEmptyTokenUsage() {
        val empty = TokenUsage.EMPTY
        
        assertEquals(0, empty.inputTokens)
        assertEquals(0, empty.outputTokens)
        assertEquals(0, empty.totalTokens)
    }
    
    @Test
    fun testAggregatedTokenUsage() {
        val aggregated = AggregatedTokenUsage(
            totalInputTokens = 1000,
            totalOutputTokens = 2000,
            interactionCount = 10
        )
        
        assertEquals(1000, aggregated.totalInputTokens)
        assertEquals(2000, aggregated.totalOutputTokens)
        assertEquals(3000, aggregated.totalTokens)
        assertEquals(10, aggregated.interactionCount)
    }
}

