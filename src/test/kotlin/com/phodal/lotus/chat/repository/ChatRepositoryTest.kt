package com.phodal.lotus.chat.repository

import com.phodal.lotus.chat.model.ChatMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Basic tests for ChatRepository.
 * Note: These tests verify the data model and flow structure.
 * Full integration tests with AI client mocking are challenging in IntelliJ Platform test environment.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryTest {

    private lateinit var repository: ChatRepository

    @BeforeEach
    fun setUp() {
        // Create repository instance
        repository = ChatRepository()
    }

    @Test
    fun `test repository initializes with empty messages`() = runTest {
        // Verify repository starts with empty message list
        val messages = repository.messagesFlow.value
        assertTrue(messages.isEmpty(), "Repository should start with empty messages")
    }

    @Test
    fun `test ChatMessage data model has required fields`() {
        // Test that ChatMessage has all the new fields we added
        val message = ChatMessage(
            id = "test-id",
            content = "Test content",
            author = "Test Author",
            isMyMessage = true,
            format = ChatMessage.MessageFormat.MARKDOWN,
            isStreaming = false
        )

        assertEquals("test-id", message.id)
        assertEquals("Test content", message.content)
        assertEquals("Test Author", message.author)
        assertTrue(message.isMyMessage)
        assertEquals(ChatMessage.MessageFormat.MARKDOWN, message.format)
        assertFalse(message.isStreaming)
    }

    @Test
    fun `test ChatMessage supports different formats`() {
        // Verify all message formats are available
        val formats = ChatMessage.MessageFormat.values()

        assertTrue(formats.contains(ChatMessage.MessageFormat.MARKDOWN))
        assertTrue(formats.contains(ChatMessage.MessageFormat.MERMAID))
        assertTrue(formats.contains(ChatMessage.MessageFormat.DIFF))
    }

    @Test
    fun `test ChatStreamEvent types are defined`() {
        // Verify all event types exist
        val startedEvent = ChatRepositoryApi.ChatStreamEvent.Started("test-id")
        assertEquals("test-id", startedEvent.aiMessageId)

        val deltaEvent = ChatRepositoryApi.ChatStreamEvent.Delta("test-id", "chunk", "full")
        assertEquals("test-id", deltaEvent.aiMessageId)
        assertEquals("chunk", deltaEvent.delta)
        assertEquals("full", deltaEvent.fullContent)

        val completedEvent = ChatRepositoryApi.ChatStreamEvent.Completed("test-id", "final")
        assertEquals("test-id", completedEvent.aiMessageId)
        assertEquals("final", completedEvent.fullContent)

        val errorEvent = ChatRepositoryApi.ChatStreamEvent.Error("test-id", RuntimeException("test"))
        assertEquals("test-id", errorEvent.aiMessageId)
        assertNotNull(errorEvent.throwable)
    }

    @Test
    fun `test ChatMessage copy with isStreaming flag`() {
        val original = ChatMessage(
            id = "test",
            content = "Original",
            author = "Author",
            isStreaming = true
        )

        val updated = original.copy(isStreaming = false)

        assertEquals(original.id, updated.id)
        assertEquals(original.content, updated.content)
        assertTrue(original.isStreaming)
        assertFalse(updated.isStreaming)
    }

    @Test
    fun `test ChatMessage copy with format`() {
        val markdown = ChatMessage(
            id = "test",
            content = "# Title",
            author = "Author",
            format = ChatMessage.MessageFormat.MARKDOWN
        )

        val mermaid = markdown.copy(
            content = "graph TD; A-->B",
            format = ChatMessage.MessageFormat.MERMAID
        )

        assertEquals(ChatMessage.MessageFormat.MARKDOWN, markdown.format)
        assertEquals(ChatMessage.MessageFormat.MERMAID, mermaid.format)
        assertEquals("graph TD; A-->B", mermaid.content)
    }
}

