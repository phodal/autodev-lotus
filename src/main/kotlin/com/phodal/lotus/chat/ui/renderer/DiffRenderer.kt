package com.phodal.lotus.chat.ui.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import com.phodal.lotus.chat.model.ChatMessage

/**
 * Renderer for unified diff content.
 * TODO: Implement syntax highlighting for diff content.
 * For now, displays the diff in a styled code block.
 */
class DiffRenderer : MessageRenderer {
    @Composable
    override fun render(message: ChatMessage, modifier: Modifier) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Color(0xFF2B2B2B), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "📝 Diff:\n\n${message.content}",
                style = JewelTheme.defaultTextStyle.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                ),
                color = Color(0xFFABB2BF)
            )
        }
    }
}

