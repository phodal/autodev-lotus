package com.phodal.lotus.chat.repository

import com.phodal.lotus.chat.model.ChatMessage
import java.time.LocalDateTime


/**
 * Factory class responsible for creating instances of `ChatMessage`.
 *
 * @param aiCompanionName The name of the AI companion, used as the author for AI-generated messages.
 * @param myUserName The name of the user, used as the author for user-generated messages.
 */
class ChatMessageFactory(
    private val aiCompanionName: String,
    private val myUserName: String
) {

    /**
     * Creates a new instance of `ChatMessage` representing an AI-generated message emitted
     * while AI is processing the request.
     *
     * @param content The content of the message.
     * @param id The unique identifier for the message. Defaults to a new UUID.
     * @param timestamp The timestamp of the message. Defaults to the current time.
     */
    fun createAIThinkingMessage(
        content: String,
        id: String = java.util.UUID.randomUUID().toString(),
        timestamp: LocalDateTime = LocalDateTime.now(),
    ): ChatMessage {
        return ChatMessage(
            id = id,
            content = content,
            author = aiCompanionName,
            timestamp = timestamp,
            isMyMessage = false,
            type = ChatMessage.ChatMessageType.AI_THINKING
        )
    }

    /**
     * Creates a new instance of `ChatMessage` representing an AI-generated message response.
     *
     * @param content The content of the message.
     * @param id The unique identifier for the message. Defaults to a new UUID.
     * @param timestamp The timestamp of the message. Defaults to the current time.
     */
    fun createAIMessage(
        content: String,
        id: String = java.util.UUID.randomUUID().toString(),
        timestamp: LocalDateTime = LocalDateTime.now(),
    ): ChatMessage {
        return ChatMessage(
            id = id,
            content = content,
            author = aiCompanionName,
            timestamp = timestamp,
            isMyMessage = false,
            type = ChatMessage.ChatMessageType.TEXT
        )
    }

    /**
     * Creates a new instance of `ChatMessage` representing a user message.
     *
     * @param content The content of the message.
     * @param timestamp The timestamp of the message. Defaults to the current time.
     */
    fun createUserMessage(content: String, timestamp: LocalDateTime = LocalDateTime.now()): ChatMessage {
        return ChatMessage(
            content = content,
            author = myUserName,
            timestamp = timestamp,
            isMyMessage = true,
            type = ChatMessage.ChatMessageType.TEXT
        )
    }
}