package com.phodal.lotus.aicore.streaming

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class StreamingCancellationTokenTest {
    
    @Test
    fun testInitialState() {
        val token = StreamingCancellationToken()
        assertFalse(token.isCancellationRequested())
        assertFalse(token.checkCancellation())
    }
    
    @Test
    fun testCancelWithoutReason() {
        val token = StreamingCancellationToken()
        token.cancel()
        
        assertTrue(token.isCancellationRequested())
        assertTrue(token.checkCancellation())
    }
    
    @Test
    fun testCancelWithReason() {
        val token = StreamingCancellationToken()
        val reason = "User interrupted"
        token.cancel(reason)
        
        assertTrue(token.isCancellationRequested())
        assertEquals(reason, token.getCancellationReason())
    }
    
    @Test
    fun testThrowIfCancellationRequested() {
        val token = StreamingCancellationToken()
        token.cancel("Test cancellation")
        
        var exceptionThrown = false
        try {
            runBlocking {
                token.throwIfCancellationRequested()
            }
        } catch (e: CancellationException) {
            exceptionThrown = true
        }
        
        assertTrue(exceptionThrown)
    }
    
    @Test
    fun testNoExceptionWhenNotCancelled() {
        val token = StreamingCancellationToken()
        
        var exceptionThrown = false
        try {
            runBlocking {
                token.throwIfCancellationRequested()
            }
        } catch (e: CancellationException) {
            exceptionThrown = true
        }
        
        assertFalse(exceptionThrown)
    }
}

