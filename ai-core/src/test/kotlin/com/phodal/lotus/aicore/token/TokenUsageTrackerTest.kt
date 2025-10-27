package com.phodal.lotus.aicore.token

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class TokenUsageTrackerTest {
    
    private lateinit var tracker: TokenUsageTracker
    
    @BeforeEach
    fun setup() {
        tracker = TokenUsageTracker.getInstance()
        runBlocking {
            tracker.clear()
        }
    }
    
    @Test
    fun testRecordUsage() = runBlocking {
        val usage = TokenUsage.of(100, 200, "gpt-4", "conv-1")
        tracker.recordUsage(usage)
        
        val aggregated = tracker.aggregatedUsage.value
        assertEquals(100, aggregated.totalInputTokens)
        assertEquals(200, aggregated.totalOutputTokens)
        assertEquals(300, aggregated.totalTokens)
        assertEquals(1, aggregated.interactionCount)
    }
    
    @Test
    fun testMultipleRecords() = runBlocking {
        tracker.recordUsage(TokenUsage.of(100, 200, "gpt-4", "conv-1"))
        tracker.recordUsage(TokenUsage.of(50, 75, "gpt-4", "conv-1"))
        
        val aggregated = tracker.aggregatedUsage.value
        assertEquals(150, aggregated.totalInputTokens)
        assertEquals(275, aggregated.totalOutputTokens)
        assertEquals(425, aggregated.totalTokens)
        assertEquals(2, aggregated.interactionCount)
    }
    
    @Test
    fun testConversationUsage() = runBlocking {
        tracker.recordUsage(TokenUsage.of(100, 200, "gpt-4", "conv-1"))
        tracker.recordUsage(TokenUsage.of(50, 75, "gpt-4", "conv-2"))
        
        val conv1Usage = tracker.getConversationUsage("conv-1")
        assertEquals(100, conv1Usage.inputTokens)
        assertEquals(200, conv1Usage.outputTokens)
        
        val conv2Usage = tracker.getConversationUsage("conv-2")
        assertEquals(50, conv2Usage.inputTokens)
        assertEquals(75, conv2Usage.outputTokens)
    }
    
    @Test
    fun testModelBreakdown() = runBlocking {
        tracker.recordUsage(TokenUsage.of(100, 200, "gpt-4", "conv-1"))
        tracker.recordUsage(TokenUsage.of(50, 75, "claude-3", "conv-1"))
        
        val aggregated = tracker.aggregatedUsage.value
        assertEquals(2, aggregated.byModel.size)
        assertTrue(aggregated.byModel.containsKey("gpt-4"))
        assertTrue(aggregated.byModel.containsKey("claude-3"))
    }
    
    @Test
    fun testClear() = runBlocking {
        tracker.recordUsage(TokenUsage.of(100, 200, "gpt-4", "conv-1"))
        tracker.clear()
        
        val aggregated = tracker.aggregatedUsage.value
        assertEquals(0, aggregated.totalTokens)
        assertEquals(0, aggregated.interactionCount)
    }
    
    @Test
    fun testClearConversation() = runBlocking {
        tracker.recordUsage(TokenUsage.of(100, 200, "gpt-4", "conv-1"))
        tracker.recordUsage(TokenUsage.of(50, 75, "gpt-4", "conv-2"))
        
        tracker.clearConversation("conv-1")
        
        val aggregated = tracker.aggregatedUsage.value
        assertEquals(50, aggregated.totalInputTokens)
        assertEquals(75, aggregated.totalOutputTokens)
        assertEquals(1, aggregated.interactionCount)
    }
    
    @Test
    fun testGetUsageHistory() = runBlocking {
        tracker.recordUsage(TokenUsage.of(100, 200, "gpt-4", "conv-1"))
        tracker.recordUsage(TokenUsage.of(50, 75, "gpt-4", "conv-1"))
        
        val history = tracker.getUsageHistory()
        assertEquals(2, history.size)
    }
}

