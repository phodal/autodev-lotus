package com.phodal.lotus.chat.repository

import com.intellij.openapi.components.Service
import com.intellij.openapi.application.ApplicationManager
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
import kotlinx.coroutines.sync.Mutex

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
    private val updateMutex = Mutex()

    override val messagesFlow: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private fun updateMessagesOnEDT(updater: (List<ChatMessage>) -> List<ChatMessage>) {
        _messages.value = updater(_messages.value)
    }

    override suspend fun sendMessage(messageContent: String) {
        withContext(Dispatchers.IO) {
            try {
                // Emits the user message to a chat list
                updateMessagesOnEDT { messages ->
                    messages + chatMessageFactory.createUserMessage(messageContent)
                }

                // Simulate AI responding
                simulateAIResponse(messageContent)
            } catch (e: Exception) {
                if (e is CancellationException) {
                    // In case the message sending is canceled before a response is generated,
                    // we remove a loading placeholder message
                    updateMessagesOnEDT { messages ->
                        messages.filter { !it.isAIThinkingMessage() }
                    }

                    throw e

                }

                e.printStackTrace()
            }
        }
    }

    private suspend fun simulateAIResponse(userMessage: String) {
        val aiThinkingMessage = chatMessageFactory
            .createAIThinkingMessage("Thinking...")
        updateMessagesOnEDT { messages ->
            messages + aiThinkingMessage
        }

        try {
            val aiClient = AIServiceFactory.getAIClient()
            if (aiClient != null && aiClient.isConfigured()) {
                // Use real AI service with streaming
                try {
                    val responseBuilder = StringBuilder()
                    var lastUpdateTime = System.currentTimeMillis()
                    val updateIntervalMs = 50L // Update UI at most every 50ms to avoid excessive updates

                    // Stream the response
                    aiClient.streamMessage(userMessage) { chunk ->
                        responseBuilder.append(chunk)

                        // Update the message in real-time as chunks arrive
                        // Use throttling to avoid excessive UI updates
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastUpdateTime >= updateIntervalMs || chunk.endsWith("\n")) {
                            lastUpdateTime = currentTime

                            // Update the thinking message with the response content
                            val responseMessage = chatMessageFactory.createAIMessage(content = responseBuilder.toString())
                            updateMessagesOnEDT { messages ->
                                messages.map { message ->
                                    if (message.isAIThinkingMessage()) {
                                        responseMessage
                                    } else {
                                        message
                                    }
                                }
                            }
                        }
                    }

                    // Final update to ensure all content is displayed
                    val finalResponseMessage = chatMessageFactory.createAIMessage(content = responseBuilder.toString())
                    updateMessagesOnEDT { messages ->
                        messages.map { message -> if (message.isAIThinkingMessage()) finalResponseMessage else message }
                    }
                } catch (e: Exception) {
                    // Error message if AI service fails
                    val errorMessage = "Error: ${e.message}. Please check your AI configuration and try again."
                    val responseMessage = chatMessageFactory.createAIMessage(content = errorMessage)
                    updateMessagesOnEDT { messages ->
                        messages.map { message -> if (message.isAIThinkingMessage()) responseMessage else message }
                    }
                }
            } else {
                // Should not happen as input should be disabled without AI config
                throw IllegalStateException("AI is not configured. Please configure an AI provider first.")
            }
        } catch (e: Exception) {
            // Remove thinking message on error
            updateMessagesOnEDT { messages ->
                messages.filter { !it.isAIThinkingMessage() }
            }
            throw e
        }
    }
}
