package com.phodal.lotus.chat.ui.renderer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.phodal.lotus.chat.model.ChatMessage

/**
 * Interface for rendering different message content formats.
 */
interface MessageRenderer {
    /**
     * Renders the message content.
     * @param message The chat message to render.
     * @param modifier Optional modifier for the rendered content.
     */
    @Composable
    fun render(message: ChatMessage, modifier: Modifier = Modifier)
}

