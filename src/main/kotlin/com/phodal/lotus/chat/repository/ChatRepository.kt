package com.phodal.lotus.chat.repository

import com.intellij.openapi.components.Service
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import com.phodal.lotus.chat.model.ChatMessage
import com.phodal.lotus.aicore.AIServiceFactory
import java.time.LocalDateTime

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
     * Sends a message with the provided content.
     *
     * @param messageContent The content of the message to be sent.
     */
    suspend fun sendMessage(messageContent: String)
}

@Service
class ChatRepository : ChatRepositoryApi {

    private val chatMessageFactory = ChatMessageFactory("AI Buddy", "Super Engineer")
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())

    override val messagesFlow: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    override suspend fun sendMessage(messageContent: String) {
        withContext(Dispatchers.IO) {
            try {
                // Emits the user message to a chat list
                _messages.value += chatMessageFactory.createUserMessage(messageContent)

                // Simulate AI responding
                simulateAIResponse(messageContent)
            } catch (e: Exception) {
                if (e is CancellationException) {
                    // In case the message sending is canceled before a response is generated,
                    // we remove a loading placeholder message
                    _messages.value = _messages.value.filter { !it.isAIThinkingMessage() }

                    throw e

                }

                e.printStackTrace()
            }
        }
    }

    private suspend fun simulateAIResponse(userMessage: String) {
        val aiThinkingMessage = chatMessageFactory
            .createAIThinkingMessage("Thinking...")
        _messages.value += aiThinkingMessage

        try {
            val aiClient = AIServiceFactory.getAIClient()
            if (aiClient != null && aiClient.isConfigured()) {
                // Use real AI service with streaming
                try {
                    val responseBuilder = StringBuilder()

                    // Stream the response
                    aiClient.streamMessage(userMessage) { chunk ->
                        responseBuilder.append(chunk)

                        // Update the message in real-time as chunks arrive
                        val responseMessage = chatMessageFactory.createAIMessage(content = responseBuilder.toString())
                        _messages.value = _messages.value
                            .map { message -> if (message.id == aiThinkingMessage.id) responseMessage else message }
                    }
                } catch (e: Exception) {
                    // Error message if AI service fails
                    val errorMessage = "Error: ${e.message}. Please check your AI configuration and try again."
                    val responseMessage = chatMessageFactory.createAIMessage(content = errorMessage)
                    _messages.value = _messages.value
                        .map { message -> if (message.id == aiThinkingMessage.id) responseMessage else message }
                }
            } else {
                // Should not happen as input should be disabled without AI config
                throw IllegalStateException("AI is not configured. Please configure an AI provider first.")
            }
        } catch (e: Exception) {
            // Remove thinking message on error
            _messages.value = _messages.value.filter { !it.isAIThinkingMessage() }
            throw e
        }
    }
}
