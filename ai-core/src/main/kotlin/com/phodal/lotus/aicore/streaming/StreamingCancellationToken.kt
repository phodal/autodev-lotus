package com.phodal.lotus.aicore.streaming

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

/**
 * A token for controlling the cancellation of streaming operations.
 * This allows graceful interruption of AI response streaming while preserving
 * the content that has already been generated.
 */
class StreamingCancellationToken {
    private var isCancelled = false
    private var cancellationReason: String? = null
    
    /**
     * Request cancellation of the streaming operation.
     * @param reason Optional reason for cancellation
     */
    fun cancel(reason: String? = null) {
        isCancelled = true
        cancellationReason = reason
    }
    
    /**
     * Check if cancellation has been requested.
     */
    fun isCancellationRequested(): Boolean = isCancelled
    
    /**
     * Get the reason for cancellation if available.
     */
    fun getCancellationReason(): String? = cancellationReason
    
    /**
     * Throw CancellationException if cancellation was requested.
     * This should be called periodically during streaming to check for cancellation.
     */
    suspend fun throwIfCancellationRequested() {
        if (isCancelled) {
            throw CancellationException("Streaming cancelled: ${cancellationReason ?: "User requested"}")
        }
    }
    
    /**
     * Check if cancellation was requested without throwing.
     * Useful for graceful shutdown without exceptions.
     */
    fun checkCancellation(): Boolean = isCancelled
}

