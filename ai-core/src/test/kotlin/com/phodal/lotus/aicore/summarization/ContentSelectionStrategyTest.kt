package com.phodal.lotus.aicore.summarization

import com.phodal.lotus.aicore.token.SimpleTokenCounter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ContentSelectionStrategyTest {
    
    private val tokenCounter = SimpleTokenCounter("test-model")
    
    private fun createTestMessages(): List<ConversationMessage> {
        return listOf(
            ConversationMessage(
                id = "1",
                content = "Hello, can you help me with Kotlin?",
                author = "User",
                isUserMessage = true,
                timestamp = 1000L
            ),
            ConversationMessage(
                id = "2",
                content = "Of course! I'd be happy to help with Kotlin. What would you like to know?",
                author = "Assistant",
                isUserMessage = false,
                timestamp = 2000L
            ),
            ConversationMessage(
                id = "3",
                content = "How do I use coroutines?",
                author = "User",
                isUserMessage = true,
                timestamp = 3000L
            ),
            ConversationMessage(
                id = "4",
                content = "Coroutines are a way to write asynchronous code in Kotlin. You can use suspend functions and launch or async builders.",
                author = "Assistant",
                isUserMessage = false,
                timestamp = 4000L
            ),
            ConversationMessage(
                id = "5",
                content = "Thanks! That's very helpful.",
                author = "User",
                isUserMessage = true,
                timestamp = 5000L
            )
        )
    }
    
    @Test
    fun testDefaultContentSelectionStrategy() {
        val strategy = DefaultContentSelectionStrategy()
        val messages = createTestMessages()
        
        val selected = strategy.selectMessages(messages, 1000, tokenCounter)
        
        assertNotNull(selected)
        assertTrue(selected.messages.isNotEmpty())
        assertTrue(selected.userMessageCount > 0)
        println("Selected ${selected.messages.size} messages with ${selected.totalTokens} tokens")
    }
    
    @Test
    fun testDefaultStrategyIncludesFirstUserMessage() {
        val strategy = DefaultContentSelectionStrategy()
        val messages = createTestMessages()
        
        val selected = strategy.selectMessages(messages, 1000, tokenCounter)
        
        // Should include the first user message
        val firstUserMessage = messages.first { it.isUserMessage }
        assertTrue(selected.messages.any { it.id == firstUserMessage.id })
    }
    
    @Test
    fun testDefaultStrategyRespectTokenLimit() {
        val strategy = DefaultContentSelectionStrategy()
        val messages = createTestMessages()
        val maxTokens = 50
        
        val selected = strategy.selectMessages(messages, maxTokens, tokenCounter)
        
        assertTrue(selected.totalTokens <= maxTokens)
        println("Selected ${selected.messages.size} messages with ${selected.totalTokens} tokens (limit: $maxTokens)")
    }
    
    @Test
    fun testRecencyFocusedStrategy() {
        val strategy = RecencyFocusedStrategy()
        val messages = createTestMessages()
        
        val selected = strategy.selectMessages(messages, 1000, tokenCounter)
        
        assertNotNull(selected)
        assertTrue(selected.messages.isNotEmpty())
        println("Recency strategy selected ${selected.messages.size} messages")
    }
    
    @Test
    fun testRecencyStrategyPrioritizesRecentMessages() {
        val strategy = RecencyFocusedStrategy()
        val messages = createTestMessages()
        
        val selected = strategy.selectMessages(messages, 1000, tokenCounter)
        
        // Should include the most recent message
        val mostRecentMessage = messages.maxByOrNull { it.timestamp }
        assertTrue(selected.messages.any { it.id == mostRecentMessage?.id })
    }
    
    @Test
    fun testEmptyMessagesList() {
        val strategy = DefaultContentSelectionStrategy()
        
        val selected = strategy.selectMessages(emptyList(), 1000, tokenCounter)
        
        assertTrue(selected.messages.isEmpty())
        assertEquals(0, selected.totalTokens)
        assertEquals(0, selected.userMessageCount)
        assertEquals(0, selected.aiMessageCount)
    }
    
    @Test
    fun testVerySmallTokenLimit() {
        val strategy = DefaultContentSelectionStrategy()
        val messages = createTestMessages()
        
        val selected = strategy.selectMessages(messages, 5, tokenCounter)
        
        // Should select at most a few messages
        assertTrue(selected.messages.size <= messages.size)
        assertTrue(selected.totalTokens <= 5)
    }
    
    @Test
    fun testMessageOrderPreservation() {
        val strategy = DefaultContentSelectionStrategy()
        val messages = createTestMessages()
        
        val selected = strategy.selectMessages(messages, 1000, tokenCounter)
        
        // Messages should be in chronological order
        for (i in 0 until selected.messages.size - 1) {
            assertTrue(selected.messages[i].timestamp <= selected.messages[i + 1].timestamp)
        }
    }
}

