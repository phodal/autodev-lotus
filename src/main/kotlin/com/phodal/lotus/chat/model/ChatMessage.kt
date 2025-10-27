package com.phodal.lotus.chat.model

import com.phodal.lotus.chat.model.ChatMessage.ChatMessageType.AI_THINKING
import com.phodal.lotus.chat.model.ChatMessage.ChatMessageType.TEXT
import com.phodal.lotus.components.Searchable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private val timeFormatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("HH:mm")

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val author: String,
    val isMyMessage: Boolean = false,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val type: ChatMessageType = TEXT,
    val format: MessageFormat = MessageFormat.MARKDOWN,
    val isStreaming: Boolean = false
) : Searchable {

    enum class ChatMessageType {
        AI_THINKING,
        TEXT;
    }

    /**
     * Enum representing different content formats for rendering.
     */
    enum class MessageFormat {
        /** Plain text or Markdown content */
        MARKDOWN,
        /** Mermaid diagram code */
        MERMAID,
        /** Unified diff format */
        DIFF
    }

    @JvmOverloads
    fun formattedTime(dateTimeFormatter: DateTimeFormatter? = timeFormatter): String {
        return timestamp.format(dateTimeFormatter)
    }


    fun isTextMessage(): Boolean = this.type == TEXT

    fun isAIThinkingMessage(): Boolean = this.type == AI_THINKING

    override fun matches(query: String): Boolean {
        if (query.isBlank()) return false

        return content.contains(query, ignoreCase = true)
    }
}
