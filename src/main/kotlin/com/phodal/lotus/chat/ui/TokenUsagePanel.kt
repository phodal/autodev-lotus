package com.phodal.lotus.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import com.phodal.lotus.aicore.token.TokenUsageTracker
import com.phodal.lotus.aicore.token.AggregatedTokenUsage
import com.phodal.lotus.chat.ChatAppColors

/**
 * Compact one-line token usage display
 */
@Composable
fun TokenUsagePanel(
    modifier: Modifier = Modifier,
    conversationId: String? = null
) {
    val tracker = TokenUsageTracker.getInstance()
    val aggregatedUsage by tracker.aggregatedUsage.collectAsState()

    // Get conversation-specific usage if conversationId is provided
    val currentUsage = if (conversationId != null) {
        tracker.getConversationUsage(conversationId)
    } else {
        null
    }

    if (aggregatedUsage.totalTokens > 0) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(ChatAppColors.Panel.background)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                text = "🪙",
                style = JewelTheme.defaultTextStyle.copy(fontSize = 14.sp)
            )

            // Current conversation
            if (currentUsage != null && currentUsage.totalTokens > 0) {
                Text(
                    text = "Current: ${currentUsage.totalTokens}",
                    style = JewelTheme.defaultTextStyle.copy(
                        fontSize = 11.sp,
                        color = ChatAppColors.Text.normal
                    )
                )
                Text(text = "•", style = JewelTheme.defaultTextStyle.copy(fontSize = 11.sp))
            }

            // Total usage
            Text(
                text = "Total: ${aggregatedUsage.totalTokens}",
                style = JewelTheme.defaultTextStyle.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = ChatAppColors.Text.normal
                )
            )

            Text(text = "•", style = JewelTheme.defaultTextStyle.copy(fontSize = 11.sp))

            // Interactions
            Text(
                text = "${aggregatedUsage.interactionCount} calls",
                style = JewelTheme.defaultTextStyle.copy(
                    fontSize = 11.sp,
                    color = ChatAppColors.Text.disabled
                )
            )
        }
    }
}

/**
 * Compact version of token usage display for header
 */
@Composable
fun CompactTokenUsageDisplay(
    modifier: Modifier = Modifier
) {
    val tracker = TokenUsageTracker.getInstance()
    val aggregatedUsage by tracker.aggregatedUsage.collectAsState()
    
    if (aggregatedUsage.totalTokens > 0) {
        Row(
            modifier = modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🪙",
                style = JewelTheme.defaultTextStyle.copy(fontSize = 12.sp)
            )
            Text(
                text = "${aggregatedUsage.totalTokens}",
                style = JewelTheme.defaultTextStyle.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = ChatAppColors.Text.normal
                )
            )
        }
    }
}

