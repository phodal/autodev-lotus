package com.phodal.lotus.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.phodal.lotus.aicore.config.LLMConfig
import com.phodal.lotus.chat.ChatAppColors
import com.phodal.lotus.config.AIConfigService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * AI Configuration Dialog for setting up LLM provider and API key
 */
@Composable
fun AIConfigDialog(
    onDismiss: () -> Unit,
    onSave: (provider: LLMProvider, apiKey: String, model: String) -> Unit,
    currentProvider: LLMProvider? = null,
    currentApiKey: String = "",
    currentModel: String = ""
) {
    // Load saved configuration from AIConfigService
    val configService = AIConfigService.getInstance()
    val savedConfig = runBlocking { configService.currentConfig.first() }

    val initialProvider = currentProvider ?: savedConfig?.provider ?: LLMProvider.DEEPSEEK
    val initialApiKey = currentApiKey.ifBlank { savedConfig?.apiKey ?: "" }
    val initialModel = currentModel.ifBlank { savedConfig?.model ?: LLMConfig.getDefaultModel(initialProvider) }

    var selectedProvider by remember { mutableStateOf(initialProvider) }
    var isProviderDropdownExpanded by remember { mutableStateOf(false) }
    var isModelDropdownExpanded by remember { mutableStateOf(false) }
    var useCustomModel by remember { mutableStateOf(false) }

    val availableModels = LLMConfig.getAvailableModels(selectedProvider)
    val apiKeyState = rememberTextFieldState(initialApiKey)
    val customModelState = rememberTextFieldState(initialModel)

    var selectedModel by remember { mutableStateOf(initialModel) }

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
                onClick = { isProviderDropdownExpanded = !isProviderDropdownExpanded },
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

            if (isProviderDropdownExpanded) {
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
                                selectedModel = LLMConfig.getDefaultModel(provider)
                                useCustomModel = false
                                isProviderDropdownExpanded = false
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

        // Model Selection
        Text(
            text = "Model",
            style = JewelTheme.defaultTextStyle
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            DefaultButton(
                onClick = { isModelDropdownExpanded = !isModelDropdownExpanded },
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
                        if (useCustomModel) "Custom: ${customModelState.text}" else selectedModel,
                        style = JewelTheme.defaultTextStyle.copy(fontSize = 14.sp),
                        maxLines = 1
                    )
                    Text("▼", modifier = Modifier.padding(start = 8.dp))
                }
            }

            if (isModelDropdownExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ChatAppColors.Panel.background)
                        .padding(top = 2.dp)
                ) {
                    // Available models
                    availableModels.forEach { model ->
                        DefaultButton(
                            onClick = {
                                selectedModel = model
                                useCustomModel = false
                                isModelDropdownExpanded = false
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
                                    model,
                                    style = JewelTheme.defaultTextStyle.copy(fontSize = 13.sp)
                                )
                                if (model == selectedModel && !useCustomModel) {
                                    Text(
                                        " ✓",
                                        modifier = Modifier.padding(start = 8.dp),
                                        style = JewelTheme.defaultTextStyle.copy(fontSize = 13.sp)
                                    )
                                }
                            }
                        }
                    }

                    // Divider
                    Divider(Orientation.Horizontal, modifier = Modifier.fillMaxWidth().height(1.dp))

                    // Custom model option
                    DefaultButton(
                        onClick = {
                            useCustomModel = true
                            isModelDropdownExpanded = false
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
                                "Custom Model...",
                                style = JewelTheme.defaultTextStyle.copy(fontSize = 13.sp)
                            )
                            if (useCustomModel) {
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

        // Custom Model Input
        if (useCustomModel) {
            Text(
                text = "Enter custom model name:",
                style = JewelTheme.defaultTextStyle.copy(fontSize = 12.sp),
                color = ChatAppColors.Text.timestamp
            )
            TextArea(
                state = customModelState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            )
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
                    val model = if (useCustomModel) customModelState.text.toString() else selectedModel
                    if (apiKey.isNotBlank() && model.isNotBlank()) {
                        onSave(selectedProvider, apiKey, model)
                        onDismiss()
                    }
                },
                modifier = Modifier.wrapContentSize(),
                enabled = apiKeyState.text.isNotEmpty() && (if (useCustomModel) customModelState.text.isNotEmpty() else true)
            ) {
                Text("Save")
            }
        }
    }
}

