package com.phodal.lotus.aicore.summarization

import com.phodal.lotus.aicore.client.AIClient
import com.phodal.lotus.aicore.token.TokenCounter
import com.phodal.lotus.aicore.token.TokenUsage

/**
 * AI-powered conversation summarizer
 * 
 * This implementation uses an AI client to generate summaries of conversations.
 * It handles token counting and content selection to ensure the summary
 * fits within token limits while preserving important information.
 */
class AIConversationSummarizer(
    private val aiClient: AIClient,
    private val tokenCounter: TokenCounter,
    private val contentSelectionStrategy: ContentSelectionStrategy = DefaultContentSelectionStrategy()
) : ConversationSummarizer {
    
    override suspend fun summarize(
        messages: List<ConversationMessage>,
        config: SummarizationConfig
    ): SummarizationResult {
        if (messages.isEmpty()) {
            return SummarizationResult(
                summary = "No messages to summarize",
                messagesIncluded = 0,
                originalTokenCount = 0,
                summaryTokenCount = 0
            )
        }
        
        // Select content based on token limits
        val selectedContent = contentSelectionStrategy.selectMessages(
            messages,
            config.maxContextTokens,
            tokenCounter
        )
        
        if (selectedContent.messages.isEmpty()) {
            return SummarizationResult(
                summary = "Unable to select messages within token limit",
                messagesIncluded = 0,
                originalTokenCount = selectedContent.totalTokens,
                summaryTokenCount = 0
            )
        }
        
        // Build the prompt for summarization
        val prompt = buildSummarizationPrompt(selectedContent.messages, config)
        
        // Call AI to generate summary
        val result = aiClient.sendMessage(prompt)
        
        val summaryTokenCount = tokenCounter.estimateTokenCount(result.content)
        
        return SummarizationResult(
            summary = result.content,
            tokenUsage = result.tokenUsage,
            messagesIncluded = selectedContent.messages.size,
            originalTokenCount = selectedContent.totalTokens,
            summaryTokenCount = summaryTokenCount
        )
    }
    
    override fun isReady(): Boolean {
        return aiClient.isConfigured()
    }
    
    private fun buildSummarizationPrompt(
        messages: List<ConversationMessage>,
        config: SummarizationConfig
    ): String {
        val conversationText = messages.joinToString("\n\n") { msg ->
            val role = if (msg.isUserMessage) "User" else "Assistant"
            "$role: ${msg.content}"
        }
        
        return """
            ${config.systemPrompt}
            
            Please summarize the following conversation:
            
            $conversationText
            
            Provide a concise summary that captures the main points and key information.
        """.trimIndent()
    }
}

/**
 * No-op conversation summarizer for when AI is not configured
 * 
 * This implementation returns a placeholder summary without calling any AI service.
 */
class NoOpConversationSummarizer : ConversationSummarizer {
    
    override suspend fun summarize(
        messages: List<ConversationMessage>,
        config: SummarizationConfig
    ): SummarizationResult {
        return SummarizationResult(
            summary = "Summarization not available - AI not configured",
            messagesIncluded = messages.size,
            originalTokenCount = 0,
            summaryTokenCount = 0
        )
    }
    
    override fun isReady(): Boolean = false
}

