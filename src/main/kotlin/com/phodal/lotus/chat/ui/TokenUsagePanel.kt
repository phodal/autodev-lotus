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
 * Panel displaying token usage statistics
 * 
 * Shows:
 * - Total tokens used (input + output)
 * - Number of interactions
 * - Breakdown by model (if multiple models used)
 */
@Composable
fun TokenUsagePanel(
    modifier: Modifier = Modifier,
    conversationId: String? = null
) {
    val tracker = TokenUsageTracker.getInstance()
    val aggregatedUsage by tracker.aggregatedUsage.collectAsState()
    
    // Get conversation-specific usage if conversationId is provided
    val displayUsage = if (conversationId != null) {
        tracker.getConversationUsage(conversationId)
    } else {
        null
    }
    
    Column(
        modifier = modifier
            .background(ChatAppColors.Panel.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Title
        Text(
            text = "Token Usage",
            style = JewelTheme.defaultTextStyle.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = ChatAppColors.Text.normal
            )
        )
        
        // Current conversation stats (if available)
        if (displayUsage != null && displayUsage.totalTokens > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current:",
                    style = JewelTheme.defaultTextStyle.copy(
                        fontSize = 11.sp,
                        color = ChatAppColors.Text.normal
                    )
                )
                Text(
                    text = "${displayUsage.totalTokens} tokens",
                    style = JewelTheme.defaultTextStyle.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ChatAppColors.Text.normal
                    )
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Input:",
                    style = JewelTheme.defaultTextStyle.copy(
                        fontSize = 10.sp,
                        color = ChatAppColors.Text.disabled
                    )
                )
                Text(
                    text = "${displayUsage.inputTokens}",
                    style = JewelTheme.defaultTextStyle.copy(
                        fontSize = 10.sp,
                        color = ChatAppColors.Text.normal
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Output:",
                    style = JewelTheme.defaultTextStyle.copy(
                        fontSize = 10.sp,
                        color = ChatAppColors.Text.disabled
                    )
                )
                Text(
                    text = "${displayUsage.outputTokens}",
                    style = JewelTheme.defaultTextStyle.copy(
                        fontSize = 10.sp,
                        color = ChatAppColors.Text.normal
                    )
                )
            }
        }
        
        // Total stats
        if (aggregatedUsage.totalTokens > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total:",
                    style = JewelTheme.defaultTextStyle.copy(
                        fontSize = 11.sp,
                        color = ChatAppColors.Text.normal
                    )
                )
                Text(
                    text = "${aggregatedUsage.totalTokens} tokens",
                    style = JewelTheme.defaultTextStyle.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ChatAppColors.Text.normal
                    )
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Interactions:",
                    style = JewelTheme.defaultTextStyle.copy(
                        fontSize = 10.sp,
                        color = ChatAppColors.Text.disabled
                    )
                )
                Text(
                    text = "${aggregatedUsage.interactionCount}",
                    style = JewelTheme.defaultTextStyle.copy(
                        fontSize = 10.sp,
                        color = ChatAppColors.Text.normal
                    )
                )
            }
        } else {
            // No usage yet
            Text(
                text = "No tokens used yet",
                style = JewelTheme.defaultTextStyle.copy(
                    fontSize = 11.sp,
                    color = ChatAppColors.Text.disabled
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
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

