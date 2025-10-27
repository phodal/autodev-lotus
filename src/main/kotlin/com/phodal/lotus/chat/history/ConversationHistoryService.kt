package com.phodal.lotus.chat.history

import com.phodal.lotus.chat.model.ChatMessage
import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.exodus.entitystore.PersistentEntityStores
import jetbrains.exodus.env.Environment
import jetbrains.exodus.env.Environments
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Paths
import java.time.Instant
import java.util.*

/**
 * Service for managing conversation history using Xodus database
 * 
 * This service stores and retrieves conversation sessions with their messages.
 * Each conversation is stored as an entity in the Xodus database.
 */
class ConversationHistoryService(
    private val dataDir: String = System.getProperty("user.home") + "/.autodev-lotus"
) : AutoCloseable {
    
    private val storeName = "conversationHistory"
    private val entityType = "Conversation"
    
    private val environment: Environment by lazy {
        val dbPath = Paths.get(dataDir, "history").toString()
        Environments.newInstance(dbPath)
    }
    
    private val entityStore: PersistentEntityStore by lazy {
        PersistentEntityStores.newInstance(environment, storeName)
    }
    
    /**
     * Save a new conversation or update an existing one
     */
    fun saveConversation(
        id: String = UUID.randomUUID().toString(),
        title: String,
        messages: List<ChatMessage>
    ): ConversationHistory {
        val now = Instant.now().toEpochMilli()
        val history = ConversationHistory(
            id = id,
            title = title,
            messages = messages,
            createdAt = now,
            updatedAt = now
        )
        
        val jsonHistory = Json.encodeToString(history)
        
        entityStore.executeInTransaction { txn ->
            // Check if conversation already exists
            val existingEntity = txn.find(entityType, "id", id).firstOrNull()
            
            val entity = existingEntity ?: txn.newEntity(entityType)
            entity.setProperty("id", id)
            entity.setProperty("title", title)
            entity.setBlobString("data", jsonHistory)
            entity.setProperty("createdAt", history.createdAt)
            entity.setProperty("updatedAt", now)
            entity.setProperty("messageCount", messages.size)
        }
        
        return history
    }
    
    /**
     * Update an existing conversation with new messages
     */
    fun updateConversation(id: String, messages: List<ChatMessage>): ConversationHistory? {
        var updatedHistory: ConversationHistory? = null
        
        entityStore.executeInTransaction { txn ->
            val entity = txn.find(entityType, "id", id).firstOrNull()
            entity?.let {
                val jsonHistory = it.getBlobString("data")
                if (jsonHistory != null) {
                    val existingHistory = Json.decodeFromString<ConversationHistory>(jsonHistory)
                    val now = Instant.now().toEpochMilli()
                    
                    updatedHistory = existingHistory.copy(
                        messages = messages,
                        updatedAt = now
                    )
                    
                    val newJsonHistory = Json.encodeToString(updatedHistory)
                    it.setBlobString("data", newJsonHistory)
                    it.setProperty("updatedAt", now)
                    it.setProperty("messageCount", messages.size)
                }
            }
        }
        
        return updatedHistory
    }
    
    /**
     * Get a conversation by ID
     */
    fun getConversation(id: String): ConversationHistory? {
        var history: ConversationHistory? = null
        
        entityStore.executeInReadonlyTransaction { txn ->
            val entity = txn.find(entityType, "id", id).firstOrNull()
            entity?.let {
                val jsonHistory = it.getBlobString("data")
                if (jsonHistory != null) {
                    history = Json.decodeFromString<ConversationHistory>(jsonHistory)
                }
            }
        }
        
        return history
    }
    
    /**
     * Get all conversations, sorted by update time (newest first)
     */
    fun getAllConversations(): List<ConversationHistory> {
        val histories = mutableListOf<ConversationHistory>()
        
        entityStore.executeInReadonlyTransaction { txn ->
            txn.getAll(entityType).forEach { entity ->
                val jsonHistory = entity.getBlobString("data")
                if (jsonHistory != null) {
                    try {
                        histories.add(Json.decodeFromString<ConversationHistory>(jsonHistory))
                    } catch (e: Exception) {
                        // Log error or handle corrupted data
                        println("Error decoding conversation history: ${entity.getProperty("id")}, ${e.message}")
                    }
                }
            }
        }
        
        // Sort by update time, newest first
        return histories.sortedByDescending { it.updatedAt }
    }
    
    /**
     * Delete a conversation by ID
     */
    fun deleteConversation(id: String): Boolean {
        var deleted = false
        
        entityStore.executeInTransaction { txn ->
            val entity = txn.find(entityType, "id", id).firstOrNull()
            entity?.let {
                deleted = it.delete()
            }
        }
        
        return deleted
    }
    
    /**
     * Get the most recent conversation
     */
    fun getLatestConversation(): ConversationHistory? {
        return getAllConversations().firstOrNull()
    }
    
    /**
     * Search conversations by title or content
     */
    fun searchConversations(query: String): List<ConversationHistory> {
        val allConversations = getAllConversations()
        val lowerQuery = query.lowercase()
        
        return allConversations.filter { conversation ->
            conversation.title.lowercase().contains(lowerQuery) ||
            conversation.messages.any { it.content.lowercase().contains(lowerQuery) }
        }
    }
    
    /**
     * Clear all conversations
     */
    fun clearAll(): Int {
        var count = 0
        
        entityStore.executeInTransaction { txn ->
            txn.getAll(entityType).forEach { entity ->
                if (entity.delete()) {
                    count++
                }
            }
        }
        
        return count
    }
    
    override fun close() {
        entityStore.close()
        environment.close()
    }
    
    companion object {
        @Volatile
        private var instance: ConversationHistoryService? = null
        
        fun getInstance(dataDir: String = System.getProperty("user.home") + "/.autodev-lotus"): ConversationHistoryService {
            return instance ?: synchronized(this) {
                instance ?: ConversationHistoryService(dataDir).also { instance = it }
            }
        }
    }
}

