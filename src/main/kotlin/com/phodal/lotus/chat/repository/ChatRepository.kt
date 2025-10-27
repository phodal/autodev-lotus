package com.phodal.lotus.chat.repository

import com.phodal.lotus.chat.model.ChatMessage
import kotlinx.coroutines.flow.*

/**
 * Interface defining the contract for managing chat messages and interactions within a chat system.
 * Provides access to the flow of messages and supports operations for sending and editing chat messages.
 */
interface ChatRepositoryApi {
    /**
     * Flow that emits a list of chat messages.
     * Updates with new messages as they are received or edited.
     */
    val messagesFlow: StateFlow<List<ChatMessage>>

    /**
     * Sealed interface representing streaming events during message sending.
     */
    sealed interface ChatStreamEvent {
        /**
         * Emitted when AI response streaming starts.
         * @param aiMessageId The unique ID of the AI message being streamed.
         */
        data class Started(val aiMessageId: String) : ChatStreamEvent

        /**
         * Emitted when a chunk of content is received.
         * @param aiMessageId The unique ID of the AI message.
         * @param delta The incremental content chunk received.
         * @param fullContent The accumulated full content so far.
         */
        data class Delta(val aiMessageId: String, val delta: String, val fullContent: String) : ChatStreamEvent

        /**
         * Emitted when streaming completes successfully.
         * @param aiMessageId The unique ID of the AI message.
         * @param fullContent The complete final content.
         */
        data class Completed(val aiMessageId: String, val fullContent: String) : ChatStreamEvent

        /**
         * Emitted when an error occurs during streaming.
         * @param aiMessageId The unique ID of the AI message (if available).
         * @param throwable The error that occurred.
         */
        data class Error(val aiMessageId: String?, val throwable: Throwable) : ChatStreamEvent
    }

    /**
     * Sends a message with the provided content and returns a flow of streaming events.
     *
     * @param messageContent The content of the message to be sent.
     * @return Flow of ChatStreamEvent representing the streaming progress.
     */
    suspend fun sendMessage(messageContent: String): kotlinx.coroutines.flow.Flow<ChatStreamEvent>
}

