package com.phodal.lotus.aicore.context.summarization

import com.phodal.lotus.aicore.token.TokenUsage

/**
 * Represents a message in the conversation for summarization
 */
data class ConversationMessage(
    val id: String,
    val content: String,
    val author: String,
    val isUserMessage: Boolean,
    val timestamp: Long
)

/**
 * Result of conversation summarization
 */
data class SummarizationResult(
    val summary: String,
    val tokenUsage: TokenUsage? = null,
    val messagesIncluded: Int = 0,
    val originalTokenCount: Int = 0,
    val summaryTokenCount: Int = 0
)

/**
 * Configuration for conversation summarization
 */
data class SummarizationConfig(
    /**
     * Maximum tokens to use for the summary
     */
    val maxSummaryTokens: Int = 500,

    /**
     * Maximum tokens to include from the conversation history
     */
    val maxContextTokens: Int = 2000,

    /**
     * Whether to prioritize user messages
     */
    val prioritizeUserMessages: Boolean = true,

    /**
     * System prompt for summarization
     */
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT
) {
    companion object {
        val DEFAULT_SYSTEM_PROMPT = """
            You are a helpful assistant that summarizes conversations concisely.
            Focus on the main topics, key decisions, and important information.
            Keep the summary clear and organized.
            Do not include unnecessary details or repetitions.
        """.trimIndent()
    }
}

/**
 * Interface for conversation summarization
 * 
 * This interface defines the contract for summarizing conversations using AI.
 * Implementations should handle token counting and content selection to ensure
 * the summary fits within token limits while preserving important information.
 */
interface ConversationSummarizer {
    /**
     * Summarize a conversation
     * 
     * @param messages The messages to summarize
     * @param config Configuration for summarization
     * @return SummarizationResult containing the summary and metadata
     */
    suspend fun summarize(
        messages: List<ConversationMessage>,
        config: SummarizationConfig = SummarizationConfig()
    ): SummarizationResult
    
    /**
     * Check if the summarizer is ready to use
     */
    fun isReady(): Boolean
}

