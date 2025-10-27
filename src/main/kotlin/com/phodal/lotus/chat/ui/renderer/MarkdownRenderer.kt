package com.phodal.lotus.chat.ui.renderer

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import com.phodal.lotus.chat.ChatAppColors
import com.phodal.lotus.chat.model.ChatMessage

/**
 * Renderer for Markdown/plain text content.
 */
class MarkdownRenderer : MessageRenderer {
    @Composable
    override fun render(message: ChatMessage, modifier: Modifier) {
        Text(
            text = message.content,
            style = JewelTheme.defaultTextStyle.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = ChatAppColors.Text.normal,
                lineHeight = 20.sp
            ),
            modifier = modifier.padding(bottom = 8.dp)
        )
    }
}

