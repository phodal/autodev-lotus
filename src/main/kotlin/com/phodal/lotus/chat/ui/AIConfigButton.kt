package com.phodal.lotus.chat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import com.phodal.lotus.aicore.config.LLMProvider
import com.phodal.lotus.chat.ChatAppColors
import com.phodal.lotus.chat.config.AIConfigService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * AI Configuration Button with Dialog
 */
@Composable
fun AIConfigButton(
    onConfigSaved: (provider: LLMProvider, apiKey: String, model: String) -> Unit,
    currentProvider: LLMProvider? = null,
    currentApiKey: String = "",
    currentModel: String = "",
    isConfigured: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }

    // Load saved configuration from AIConfigService
    val configService = AIConfigService.getInstance()
    val savedConfig = runBlocking { configService.currentConfig.first() }

    val displayProvider = currentProvider ?: savedConfig?.provider
    val displayApiKey = currentApiKey.ifBlank { savedConfig?.apiKey ?: "" }
    val displayConfigured = isConfigured || savedConfig != null

    Row(
        modifier = Modifier.wrapContentSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Show configured model info if available
        if (displayConfigured && displayProvider != null) {
            Column(
                modifier = Modifier.wrapContentSize(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = displayProvider.name,
                    style = JewelTheme.defaultTextStyle.copy(fontSize = 12.sp),
                    color = ChatAppColors.Text.timestamp
                )
                Text(
                    text = "Configured",
                    style = JewelTheme.defaultTextStyle.copy(fontSize = 10.sp),
                    color = ChatAppColors.Text.disabled
                )
            }
        } else {
            Column(
                modifier = Modifier.wrapContentSize(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "AI Config",
                    style = JewelTheme.defaultTextStyle.copy(fontSize = 12.sp),
                    color = ChatAppColors.Text.disabled
                )
                Text(
                    text = "Not configured",
                    style = JewelTheme.defaultTextStyle.copy(fontSize = 10.sp),
                    color = ChatAppColors.Text.disabled
                )
            }
        }

        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { showDialog = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    AllIconsKeys.General.Settings,
                    contentDescription = "AI Configuration"
                )
            }
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            AIConfigDialog(
                onDismiss = { showDialog = false },
                onSave = { provider, apiKey, model ->
                    onConfigSaved(provider, apiKey, model)
                },
                currentProvider = displayProvider,
                currentApiKey = displayApiKey,
                currentModel = currentModel
            )
        }
    }
}

