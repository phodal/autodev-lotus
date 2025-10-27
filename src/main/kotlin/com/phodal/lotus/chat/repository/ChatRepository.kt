package com.phodal.lotus.chat.repository

import com.intellij.openapi.components.Service
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.isActive
import kotlinx.coroutines.currentCoroutineContext
import com.phodal.lotus.chat.model.ChatMessage
import com.phodal.lotus.aicore.AIServiceFactory
import com.phodal.lotus.aicore.streaming.StreamingCancellationToken

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

@Service
class ChatRepository : ChatRepositoryApi {

    private val chatMessageFactory = ChatMessageFactory("AI Buddy", "Super Engineer")
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val updateMutex = Mutex()

    // Cancellation token for the current streaming operation
    private var currentStreamingCancellationToken: StreamingCancellationToken? = null

    override val messagesFlow: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private suspend fun updateMessages(updater: (List<ChatMessage>) -> List<ChatMessage>) {
        updateMutex.withLock {
            _messages.value = updater(_messages.value)
        }
    }

    /**
     * Cancel the current streaming operation gracefully.
     * The already-generated content will be preserved.
     */
    fun cancelCurrentStreaming(reason: String = "User requested") {
        currentStreamingCancellationToken?.cancel(reason)
    }

    override suspend fun sendMessage(messageContent: String): Flow<ChatRepositoryApi.ChatStreamEvent> = flow {
        var aiMessageId: String? = null
        // Create a new cancellation token for this streaming operation
        val cancellationToken = StreamingCancellationToken()
        currentStreamingCancellationToken = cancellationToken

        try {
            // Emits the user message to a chat list
            updateMessages { messages ->
                messages + chatMessageFactory.createUserMessage(messageContent)
            }

            // Generate AI response with streaming and emit events
            streamAIResponse(messageContent, cancellationToken).collect { event ->
                aiMessageId = when (event) {
                    is ChatRepositoryApi.ChatStreamEvent.Started -> event.aiMessageId
                    is ChatRepositoryApi.ChatStreamEvent.Delta -> event.aiMessageId
                    is ChatRepositoryApi.ChatStreamEvent.Completed -> event.aiMessageId
                    is ChatRepositoryApi.ChatStreamEvent.Error -> event.aiMessageId
                }
                emit(event)
            }
        } catch (e: Exception) {
            if (e is CancellationException) {
                // Check if this was a user-initiated cancellation (via Stop button)
                // If so, preserve the content that was already generated
                if (cancellationToken.checkCancellation()) {
                    // User initiated cancellation - preserve the content
                    aiMessageId?.let { id ->
                        val currentContent = _messages.value.find { it.id == id }?.content ?: ""
                        if (currentContent.isNotEmpty() && currentContent != "Thinking...") {
                            // Content was generated, keep it
                            updateMessages { messages ->
                                messages.map { message ->
                                    if (message.id == id) {
                                        message.copy(
                                            type = ChatMessage.ChatMessageType.TEXT,
                                            isStreaming = false
                                        )
                                    } else {
                                        message
                                    }
                                }
                            }
                        } else {
                            // No content was generated, remove the thinking message
                            updateMessages { messages ->
                                messages.filter { it.id != id }
                            }
                        }
                    }
                } else {
                    // System cancellation - remove the AI thinking placeholder message
                    aiMessageId?.let { id ->
                        updateMessages { messages ->
                            messages.filter { it.id != id }
                        }
                    }
                }
                throw e
            }

            e.printStackTrace()
            aiMessageId?.let { id ->
                emit(ChatRepositoryApi.ChatStreamEvent.Error(id, e))
            } ?: emit(ChatRepositoryApi.ChatStreamEvent.Error(null, e))
        } finally {
            currentStreamingCancellationToken = null
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Streams AI response and emits streaming events.
     * Updates the message content incrementally as chunks arrive.
     * @param userMessage The user's message to send to AI
     * @param cancellationToken Token to control streaming cancellation
     */
    private suspend fun streamAIResponse(
        userMessage: String,
        cancellationToken: StreamingCancellationToken
    ): Flow<ChatRepositoryApi.ChatStreamEvent> = flow {
        // Generate a unique ID for this AI response
        val aiMessageId = java.util.UUID.randomUUID().toString()

        // Create and add the initial "thinking" placeholder message
        val aiThinkingMessage = chatMessageFactory.createAIThinkingMessage(
            content = "Thinking...",
            id = aiMessageId
        ).copy(isStreaming = true)
        updateMessages { messages ->
            messages + aiThinkingMessage
        }

        // Emit Started event
        emit(ChatRepositoryApi.ChatStreamEvent.Started(aiMessageId))

        try {
            val aiClient = AIServiceFactory.getAIClient()
            if (aiClient != null && aiClient.isConfigured()) {
                // Use real AI service with streaming
                try {
                    val responseBuilder = StringBuilder()
                    var lastUpdateTime = System.currentTimeMillis()
                    val updateIntervalMs = 50L // Update UI at most every 50ms to avoid excessive updates

                    // Stream the response with cancellation token support
                    val tokenUsage = aiClient.streamMessage(userMessage, { chunk ->
                        // Check for cancellation request
                        if (cancellationToken.checkCancellation()) {
                            return@streamMessage
                        }

                        responseBuilder.append(chunk)

                        // Update the message in real-time as chunks arrive
                        // Use throttling to avoid excessive UI updates
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastUpdateTime >= updateIntervalMs || chunk.endsWith("\n")) {
                            lastUpdateTime = currentTime

                            // Update the message by ID with the accumulated response content
                            // Use direct state update instead of runBlocking to avoid EDT freezing
                            if (updateMutex.tryLock()) {
                                try {
                                    _messages.value = _messages.value.map { message ->
                                        if (message.id == aiMessageId) {
                                            message.copy(
                                                content = responseBuilder.toString(),
                                                isStreaming = true
                                            )
                                        } else {
                                            message
                                        }
                                    }
                                } finally {
                                    updateMutex.unlock()
                                }
                            }
                        }
                    }, cancellationToken)

                    // Final update: convert to TEXT type, mark as not streaming, and ensure all content is displayed
                    val finalContent = responseBuilder.toString()
                    updateMessages { messages ->
                        messages.map { message ->
                            if (message.id == aiMessageId) {
                                message.copy(
                                    content = finalContent,
                                    type = ChatMessage.ChatMessageType.TEXT,
                                    isStreaming = false
                                )
                            } else {
                                message
                            }
                        }
                    }

                    // Emit Completed event (token usage is now tracked automatically in AIClient)
                    emit(ChatRepositoryApi.ChatStreamEvent.Completed(aiMessageId, finalContent))
                } catch (e: Exception) {
                    // Check if this was a cancellation
                    if (cancellationToken.checkCancellation()) {
                        // Preserve the content that was already generated
                        val currentContent = _messages.value.find { it.id == aiMessageId }?.content ?: ""
                        if (currentContent.isNotEmpty() && currentContent != "Thinking...") {
                            updateMessages { messages ->
                                messages.map { message ->
                                    if (message.id == aiMessageId) {
                                        message.copy(
                                            content = currentContent,
                                            type = ChatMessage.ChatMessageType.TEXT,
                                            isStreaming = false
                                        )
                                    } else {
                                        message
                                    }
                                }
                            }
                            emit(ChatRepositoryApi.ChatStreamEvent.Completed(aiMessageId, currentContent))
                        } else {
                            // No content was generated, remove the thinking message
                            updateMessages { messages ->
                                messages.filter { it.id != aiMessageId }
                            }
                        }
                    } else {
                        // Error message if AI service fails
                        val errorMessage = "Error: ${e.message}. Please check your AI configuration and try again."
                        updateMessages { messages ->
                            messages.map { message ->
                                if (message.id == aiMessageId) {
                                    message.copy(
                                        content = errorMessage,
                                        type = ChatMessage.ChatMessageType.TEXT
                                    )
                                } else {
                                    message
                                }
                            }
                        }
                        emit(ChatRepositoryApi.ChatStreamEvent.Error(aiMessageId, e))
                    }
                }
            } else {
                // Should not happen as input should be disabled without AI config
                val error = IllegalStateException("AI is not configured. Please configure an AI provider first.")
                emit(ChatRepositoryApi.ChatStreamEvent.Error(aiMessageId, error))
                throw error
            }
        } catch (e: Exception) {
            if (e !is CancellationException) {
                // Remove thinking message on error
                updateMessages { messages ->
                    messages.filter { it.id != aiMessageId }
                }
                emit(ChatRepositoryApi.ChatStreamEvent.Error(aiMessageId, e))
            }
            throw e
        }
    }
}
