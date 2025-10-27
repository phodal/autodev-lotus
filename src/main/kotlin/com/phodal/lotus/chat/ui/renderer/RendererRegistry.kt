package com.phodal.lotus.chat.ui.renderer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.phodal.lotus.chat.model.ChatMessage

/**
 * Registry for managing message renderers.
 * Allows pluggable rendering of different message formats.
 */
object RendererRegistry {
    private val renderers = mutableMapOf<ChatMessage.MessageFormat, MessageRenderer>()

    init {
        // Register default renderers
        register(ChatMessage.MessageFormat.MARKDOWN, MarkdownRenderer())
        register(ChatMessage.MessageFormat.MERMAID, MermaidRenderer())
        register(ChatMessage.MessageFormat.DIFF, DiffRenderer())
    }

    /**
     * Registers a renderer for a specific message format.
     * @param format The message format to register the renderer for.
     * @param renderer The renderer implementation.
     */
    fun register(format: ChatMessage.MessageFormat, renderer: MessageRenderer) {
        renderers[format] = renderer
    }

    /**
     * Gets the renderer for a specific message format.
     * Falls back to Markdown renderer if no specific renderer is found.
     * @param format The message format.
     * @return The renderer for the format.
     */
    fun getRenderer(format: ChatMessage.MessageFormat): MessageRenderer {
        return renderers[format] ?: renderers[ChatMessage.MessageFormat.MARKDOWN]!!
    }

    /**
     * Renders a message using the appropriate renderer based on its format.
     * @param message The message to render.
     * @param modifier Optional modifier for the rendered content.
     */
    @Composable
    fun renderMessage(message: ChatMessage, modifier: Modifier = Modifier) {
        val renderer = getRenderer(message.format)
        renderer.render(message, modifier)
    }
}

