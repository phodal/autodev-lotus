package com.phodal.lotus.aicore.summarization

import com.phodal.lotus.aicore.client.AIClient
import com.phodal.lotus.aicore.client.AIMessageResult
import com.phodal.lotus.aicore.token.SimpleTokenCounter
import com.phodal.lotus.aicore.token.TokenUsage
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ConversationSummarizerTest {
    
    private val tokenCounter = SimpleTokenCounter("test-model")
    
    private fun createMockAIClient(): AIClient {
        return object : AIClient {
            override suspend fun sendMessage(message: String): AIMessageResult {
                return AIMessageResult(
                    content = "This is a mock summary of the conversation.",
                    tokenUsage = TokenUsage.of(100, 50, "test-model", "test-conv")
                )
            }
            
            override suspend fun streamMessage(
                message: String,
                onChunk: (String) -> Unit,
                cancellationToken: Any?
            ): TokenUsage? {
                return TokenUsage.of(100, 50, "test-model", "test-conv")
            }
            
            override fun isConfigured(): Boolean = true
        }
    }
    
    private fun createTestMessages(): List<ConversationMessage> {
        return listOf(
            ConversationMessage(
                id = "1",
                content = "What is Kotlin?",
                author = "User",
                isUserMessage = true,
                timestamp = 1000L
            ),
            ConversationMessage(
                id = "2",
                content = "Kotlin is a modern programming language that runs on the JVM.",
                author = "Assistant",
                isUserMessage = false,
                timestamp = 2000L
            ),
            ConversationMessage(
                id = "3",
                content = "How is it different from Java?",
                author = "User",
                isUserMessage = true,
                timestamp = 3000L
            ),
            ConversationMessage(
                id = "4",
                content = "Kotlin has null safety, extension functions, and more concise syntax than Java.",
                author = "Assistant",
                isUserMessage = false,
                timestamp = 4000L
            )
        )
    }
    
    @Test
    fun testAIConversationSummarizerCreation() {
        val aiClient = createMockAIClient()
        val summarizer = AIConversationSummarizer(aiClient, tokenCounter)
        
        assertTrue(summarizer.isReady())
    }
    
    @Test
    fun testSummarizeConversation() = runBlocking {
        val aiClient = createMockAIClient()
        val summarizer = AIConversationSummarizer(aiClient, tokenCounter)
        val messages = createTestMessages()
        
        val result = summarizer.summarize(messages)
        
        assertNotNull(result)
        assertNotNull(result.summary)
        assertTrue(result.summary.isNotEmpty())
        assertEquals(messages.size, result.messagesIncluded)
        println("Summary: ${result.summary}")
    }
    
    @Test
    fun testSummarizeWithCustomConfig() = runBlocking {
        val aiClient = createMockAIClient()
        val summarizer = AIConversationSummarizer(aiClient, tokenCounter)
        val messages = createTestMessages()
        
        val config = SummarizationConfig(
            maxSummaryTokens = 200,
            maxContextTokens = 500,
            prioritizeUserMessages = true
        )
        
        val result = summarizer.summarize(messages, config)
        
        assertNotNull(result)
        assertTrue(result.summary.isNotEmpty())
    }
    
    @Test
    fun testSummarizeEmptyConversation() = runBlocking {
        val aiClient = createMockAIClient()
        val summarizer = AIConversationSummarizer(aiClient, tokenCounter)
        
        val result = summarizer.summarize(emptyList())
        
        assertNotNull(result)
        assertEquals(0, result.messagesIncluded)
    }
    
    @Test
    fun testNoOpSummarizer() = runBlocking {
        val summarizer = NoOpConversationSummarizer()
        val messages = createTestMessages()
        
        assertFalse(summarizer.isReady())
        
        val result = summarizer.summarize(messages)
        
        assertNotNull(result)
        assertTrue(result.summary.contains("not available"))
    }
    
    @Test
    fun testSummarizationResultMetadata() = runBlocking {
        val aiClient = createMockAIClient()
        val summarizer = AIConversationSummarizer(aiClient, tokenCounter)
        val messages = createTestMessages()
        
        val result = summarizer.summarize(messages)
        
        assertTrue(result.messagesIncluded > 0)
        assertTrue(result.summaryTokenCount >= 0)
        assertNotNull(result.tokenUsage)
    }
}

