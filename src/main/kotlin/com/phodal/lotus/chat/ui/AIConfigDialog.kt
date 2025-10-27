package com.phodal.lotus.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.*
import com.phodal.lotus.aicore.config.LLMProvider
import com.phodal.lotus.chat.ChatAppColors
import com.phodal.lotus.chat.config.AIConfigService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * AI Configuration Dialog for setting up LLM provider and API key
 */
@Composable
fun AIConfigDialog(
    onDismiss: () -> Unit,
    onSave: (provider: LLMProvider, apiKey: String) -> Unit,
    currentProvider: LLMProvider? = null,
    currentApiKey: String = ""
) {
    // Load saved configuration from AIConfigService
    val configService = AIConfigService.getInstance()
    val savedConfig = runBlocking { configService.currentConfig.first() }

    val initialProvider = currentProvider ?: savedConfig?.provider ?: LLMProvider.DEEPSEEK
    val initialApiKey = currentApiKey.ifBlank { savedConfig?.apiKey ?: "" }

    var selectedProvider by remember { mutableStateOf(initialProvider) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val apiKeyState = rememberTextFieldState(initialApiKey)

    Column(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .background(ChatAppColors.Panel.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "AI Configuration",
            style = JewelTheme.defaultTextStyle.copy(fontSize = 16.sp)
        )

        Divider(Orientation.Horizontal, modifier = Modifier.fillMaxWidth().height(1.dp))

        // Provider Selection with Dropdown
        Text(
            text = "LLM Provider",
            style = JewelTheme.defaultTextStyle
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            DefaultButton(
                onClick = { isDropdownExpanded = !isDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        selectedProvider.name,
                        style = JewelTheme.defaultTextStyle.copy(fontSize = 14.sp)
                    )
                    Text("▼", modifier = Modifier.padding(start = 8.dp))
                }
            }

            if (isDropdownExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ChatAppColors.Panel.background)
                        .padding(top = 2.dp)
                ) {
                    LLMProvider.values().forEach { provider ->
                        DefaultButton(
                            onClick = {
                                selectedProvider = provider
                                isDropdownExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    provider.name,
                                    style = JewelTheme.defaultTextStyle.copy(fontSize = 13.sp)
                                )
                                if (provider == selectedProvider) {
                                    Text(
                                        " ✓",
                                        modifier = Modifier.padding(start = 8.dp),
                                        style = JewelTheme.defaultTextStyle.copy(fontSize = 13.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // API Key Input
        Text(
            text = "API Key",
            style = JewelTheme.defaultTextStyle
        )

        TextArea(
            state = apiKeyState,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )

        // Info text
        Text(
            text = "Your API key is stored locally and never sent to external servers.",
            style = JewelTheme.defaultTextStyle.copy(fontSize = 12.sp),
            color = ChatAppColors.Text.timestamp
        )

        // Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultButton(
                onClick = onDismiss,
                modifier = Modifier.wrapContentSize()
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.width(8.dp))

            DefaultButton(
                onClick = {
                    val apiKey = apiKeyState.text.toString()
                    if (apiKey.isNotBlank()) {
                        onSave(selectedProvider, apiKey)
                        onDismiss()
                    }
                },
                modifier = Modifier.wrapContentSize(),
                enabled = apiKeyState.text.isNotEmpty()
            ) {
                Text("Save")
            }
        }
    }
}

