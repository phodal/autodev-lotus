package com.phodal.lotus.aicore.token

/**
 * Token counter using jtokkit library for accurate token counting
 * 
 * This implementation provides precise token counting for different LLM models
 * using the jtokkit library which implements the same tokenization as OpenAI's tiktoken.
 * 
 * Note: This class uses reflection to load jtokkit at runtime to make it optional.
 * If jtokkit is not available, it falls back to SimpleTokenCounter.
 * 
 * Supported models:
 * - OpenAI: gpt-4, gpt-4-turbo, gpt-3.5-turbo, etc.
 * - Claude: Uses cl100k_base encoding (compatible with OpenAI)
 * - DeepSeek: Uses cl100k_base encoding (compatible with OpenAI)
 * - Gemini: Uses cl100k_base encoding (approximation)
 */
class JtokKitTokenCounter(private val modelName: String) : TokenCounter {
    
    private val delegate: TokenCounter = try {
        createJtokKitCounter(modelName)
    } catch (e: Exception) {
        // Fallback to SimpleTokenCounter if jtokkit is not available
        FallbackTokenCounter(modelName)
    }
    
    override fun estimateTokenCount(text: String): Int {
        return delegate.estimateTokenCount(text)
    }
    
    override fun getModelName(): String = modelName
    
    companion object {
        /**
         * Create a jtokkit-based token counter using reflection
         */
        private fun createJtokKitCounter(modelName: String): TokenCounter {
            try {
                // Load jtokkit classes using reflection
                val encodingsClass = Class.forName("com.knuddelsgmbh.jtokkit.Encodings")
                val encodingTypeClass = Class.forName("com.knuddelsgmbh.jtokkit.api.EncodingType")
                
                // Get the newDefaultEncodingRegistry method
                val newRegistryMethod = encodingsClass.getMethod("newDefaultEncodingRegistry")
                val registry = newRegistryMethod.invoke(null)
                
                // Get the CL100K_BASE encoding type
                val cl100kField = encodingTypeClass.getField("CL100K_BASE")
                val encodingType = cl100kField.get(null)
                
                // Get the encoding
                val getEncodingMethod = registry.javaClass.getMethod("getEncoding", encodingTypeClass)
                val encoding = getEncodingMethod.invoke(registry, encodingType)
                
                // Create a wrapper that uses the encoding
                return JtokKitEncodingWrapper(encoding, modelName)
            } catch (e: Exception) {
                throw RuntimeException("Failed to initialize jtokkit token counter", e)
            }
        }
    }
}

/**
 * Wrapper for jtokkit Encoding using reflection
 */
private class JtokKitEncodingWrapper(private val encoding: Any, private val modelName: String) : TokenCounter {
    
    override fun estimateTokenCount(text: String): Int {
        if (text.isBlank()) return 0
        return try {
            val countTokensMethod = encoding.javaClass.getMethod("countTokens", String::class.java)
            (countTokensMethod.invoke(encoding, text) as Number).toInt()
        } catch (e: Exception) {
            // Fallback to simple estimation
            (text.split(Regex("\\s+")).size / 0.75).toInt()
        }
    }
    
    override fun getModelName(): String = modelName
}

