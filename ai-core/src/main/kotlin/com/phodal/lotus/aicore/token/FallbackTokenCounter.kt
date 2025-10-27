package com.phodal.lotus.aicore.token

class FallbackTokenCounter(private val modelName: String = "unknown") : TokenCounter {
    companion object Companion {
        private const val WORDS_PER_TOKEN = 0.75
    }

    override fun estimateTokenCount(text: String): Int {
        if (text.isBlank()) return 0

        val wordCount = text.split(Regex("\\s+")).size
        return (wordCount / WORDS_PER_TOKEN).toInt()
    }

    override fun getModelName(): String = modelName
}