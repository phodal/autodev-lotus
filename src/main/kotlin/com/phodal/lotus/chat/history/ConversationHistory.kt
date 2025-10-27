package com.phodal.lotus.chat.history

import com.phodal.lotus.chat.model.ChatMessage
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.builtins.ListSerializer
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Represents a conversation session with its messages
 */
@Serializable
data class ConversationHistory(
    val id: String,
    val title: String,
    @Serializable(with = ChatMessageListSerializer::class)
    val messages: List<ChatMessage>,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Get a preview of the conversation (first user message or title)
     */
    fun getPreview(): String {
        val firstUserMessage = messages.firstOrNull { it.isMyMessage }?.content
        return firstUserMessage?.take(50) ?: title
    }

    /**
     * Get the number of messages in this conversation
     */
    fun messageCount(): Int = messages.size
}

/**
 * Custom serializer for ChatMessage list
 * Since ChatMessage uses LocalDateTime, we need to convert to/from epoch millis
 */
object ChatMessageListSerializer : KSerializer<List<ChatMessage>> {
    private val delegateSerializer = ListSerializer(SerializableChatMessage.serializer())

    override val descriptor: SerialDescriptor = delegateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: List<ChatMessage>) {
        val serializableMessages = value.map {
            SerializableChatMessage(
                id = it.id,
                content = it.content,
                author = it.author,
                isMyMessage = it.isMyMessage,
                timestamp = it.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                type = it.type.name,
                format = it.format.name,
                isStreaming = it.isStreaming
            )
        }
        encoder.encodeSerializableValue(delegateSerializer, serializableMessages)
    }

    override fun deserialize(decoder: Decoder): List<ChatMessage> {
        val serializableMessages = decoder.decodeSerializableValue(delegateSerializer)
        return serializableMessages.map {
            ChatMessage(
                id = it.id,
                content = it.content,
                author = it.author,
                isMyMessage = it.isMyMessage,
                timestamp = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(it.timestamp),
                    ZoneId.systemDefault()
                ),
                type = ChatMessage.ChatMessageType.valueOf(it.type),
                format = ChatMessage.MessageFormat.valueOf(it.format),
                isStreaming = it.isStreaming
            )
        }
    }
}

@Serializable
private data class SerializableChatMessage(
    val id: String,
    val content: String,
    val author: String,
    val isMyMessage: Boolean,
    val timestamp: Long,
    val type: String,
    val format: String,
    val isStreaming: Boolean
)

