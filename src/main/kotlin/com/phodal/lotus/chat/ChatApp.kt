package com.phodal.lotus.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phodal.lotus.chat.history.ConversationHistoryDialog
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.*
import com.phodal.lotus.chat.model.ChatMessage
import com.phodal.lotus.chat.ui.*
import com.phodal.lotus.chat.viewmodel.ChatViewModel
import com.phodal.lotus.chat.viewmodel.MessageInputState

@Composable
fun ChatApp(viewModel: ChatViewModel) {
    val chatMessages by viewModel.chatMessagesFlow.collectAsState(emptyList<ChatMessage>())
    val searchState by viewModel.searchChatMessagesHandler().searchStateFlow.collectAsState(SearchState.Idle)
    val messageInputState by viewModel.promptInputState.collectAsState(MessageInputState.Disabled)
    val isAIConfigured by viewModel.isAIConfigured.collectAsState(false)
    val currentAIProvider by viewModel.currentAIProvider.collectAsState(null)
    val conversationHistories by viewModel.conversationHistories.collectAsState(emptyList())
    val currentConversationTitle by viewModel.currentConversationTitle.collectAsState("New Conversation")

    val listState = rememberLazyListState()
    val textFieldState = rememberTextFieldState()
    var showHistoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(chatMessages.lastOrNull()?.id) {
        if (chatMessages.isNotEmpty() && !searchState.isSearching) {
            listState.animateScrollToItem(chatMessages.lastIndex)
        }
    }

    LaunchedEffect(searchState.currentSelectedSearchResultId) {
        val currentResultId = searchState.currentSelectedSearchResultId
        if (currentResultId != null) {
            val messageIndexInList = chatMessages.indexOfFirst { it.id == currentResultId }
            if (messageIndexInList >= 0) {
                listState.animateScrollToItem(messageIndexInList)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatAppColors.Panel.background)
    ) {
        ChatHeaderWithSearchBar(
            searchState,
            onStartSearch = { viewModel.searchChatMessagesHandler().onStartSearch() },
            onStopSearch = { viewModel.searchChatMessagesHandler().onStopSearch() },
            onSearchQueryChange = { query -> viewModel.searchChatMessagesHandler().onSearchQuery(query) },
            onNextResult = { viewModel.searchChatMessagesHandler().onNavigateToNextSearchResult() },
            onPreviousResult = { viewModel.searchChatMessagesHandler().onNavigateToPreviousSearchResult() },
            onAIConfigSaved = { provider, apiKey, model -> viewModel.onAIConfigSaved(provider, apiKey, model) },
            isAIConfigured = isAIConfigured,
            currentAIProvider = currentAIProvider,
            onShowHistoryDialog = { showHistoryDialog = true },
            onNewConversation = { viewModel.createNewConversation() },
            currentConversationTitle = currentConversationTitle
        )

        // Token usage panel
        if (isAIConfigured) {
            TokenUsagePanel(
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Message area
        ChatList(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            chatMessages = chatMessages,
            listState = listState,
            searchState = searchState
        )

        PromptInput(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 120.dp),
            textFieldState = textFieldState,
            promptInputState = if (isAIConfigured) messageInputState else MessageInputState.Disabled,
            onInputChanged = { viewModel.onPromptInputChanged(it) },
            onSend = { viewModel.onSendMessage() },
            onStop = { viewModel.onAbortSendingMessage() },
            isAIConfigured = isAIConfigured
        )
    }

    // Conversation history dialog
    if (showHistoryDialog) {
        ConversationHistoryDialog(
            conversations = conversationHistories,
            currentConversationId = null, // TODO: Get current conversation ID from viewModel
            onSelectConversation = { conversationId -> viewModel.switchToConversation(conversationId) },
            onDeleteConversation = { conversationId -> viewModel.deleteConversation(conversationId) },
            onRenameConversation = { conversationId, newTitle -> viewModel.updateConversationTitle(newTitle) },
            onDismiss = { showHistoryDialog = false }
        )
    }
}

@Composable
private fun ChatList(
    modifier: Modifier = Modifier,
    chatMessages: List<ChatMessage>,
    listState: LazyListState,
    searchState: SearchState
) {
    Box(modifier = modifier) {
        if (chatMessages.isEmpty()) {
            // Empty state
            EmptyChatListPlaceholder()
        } else {
            VerticallyScrollableContainer(
                modifier = Modifier.fillMaxWidth().safeContentPadding(),
                scrollState = listState,
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(chatMessages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            modifier = Modifier.fillMaxWidth(),
                            isMatchingSearch = searchState.searchQuery?.let { query -> message.matches(query) }
                                ?: false,
                            isHighlightedInSearch = message.id == searchState.currentSelectedSearchResultId,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChatListPlaceholder(
    placeholderText: String = "Start a conversation with your AI Assistant!",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = placeholderText,
            style = JewelTheme.defaultTextStyle.copy(
                color = ChatAppColors.Text.disabled,
                fontSize = 16.sp
            )
        )
    }
}

@Composable
private fun ChatHeaderWithSearchBar(
    searchState: SearchState,
    onStartSearch: () -> Unit,
    onStopSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onNextResult: () -> Unit,
    onPreviousResult: () -> Unit,
    onAIConfigSaved: (com.phodal.lotus.aicore.config.LLMProvider, String, String) -> Unit = { _, _, _ -> },
    isAIConfigured: Boolean = false,
    currentAIProvider: com.phodal.lotus.aicore.config.LLMProvider? = null,
    onShowHistoryDialog: () -> Unit = {},
    onNewConversation: () -> Unit = {},
    currentConversationTitle: String = "Current Conversation"
) {
    val showSearchBar = searchState.isSearching

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatAppColors.Panel.background)
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Search button
        IconButton(onClick = { if (showSearchBar) onStopSearch() else onStartSearch() }) {
            Icon(
                ChatAppIcons.Header.search,
                contentDescription = if (showSearchBar) "Close search" else "Search messages"
            )
        }

        ChatHeaderTitle(
            modifier = Modifier.weight(1f),
            title = currentConversationTitle
        )

        // Compact token usage display
        if (isAIConfigured) {
            CompactTokenUsageDisplay()
        }

        AIConfigButton(
            onConfigSaved = onAIConfigSaved,
            isConfigured = isAIConfigured,
            currentProvider = currentAIProvider
        )

        // History button
        IconButton(onClick = onShowHistoryDialog) {
            Icon(
                ChatAppIcons.Header.history,
                contentDescription = "Show conversation history"
            )
        }

        // Right side: New conversation button
        IconButton(onClick = onNewConversation) {
            Icon(
                ChatAppIcons.Header.plus,
                contentDescription = "New conversation"
            )
        }
    }

    Divider(Orientation.Horizontal, modifier = Modifier.fillMaxWidth().height(1.dp))

    if (showSearchBar) {
        ChatSearchBar(
            searchState = searchState,
            onSearchQueryChange = { query -> onSearchQueryChange(query) },
            onNextResult = { onNextResult() },
            onPreviousResult = { onPreviousResult() },
            onCloseSearch = { onStopSearch() }
        )
    }

    Divider(Orientation.Horizontal, modifier = Modifier.fillMaxWidth().height(1.dp))
}

@Composable
private fun ChatHeaderTitle(modifier: Modifier = Modifier, title: String = "AutoDev Lotus") {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = JewelTheme.defaultTextStyle.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        )
    }
}

@Composable
private fun ChatSearchBar(
    searchState: SearchState,
    modifier: Modifier = Modifier,
    onSearchQueryChange: (String) -> Unit = {},
    onNextResult: () -> Unit = {},
    onPreviousResult: () -> Unit = {},
    onCloseSearch: () -> Unit = {}
) {
    val searchQuery = searchState.searchQuery.orEmpty()
    val hasResults = searchState.hasResults
    val totalResults = searchState.totalResults
    val currentResultIndex = searchState.currentSearchResultIndex

    val searchFieldState = rememberTextFieldState(searchQuery)

    val focusRequester = remember { FocusRequester() }

    // Handle text changes
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()

        snapshotFlow { searchFieldState.text.toString() }
            .distinctUntilChanged()
            .collect { query -> onSearchQueryChange(query) }
    }

    // Handle focus request
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ChatAppColors.Panel.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search input field
        TextField(
            state = searchFieldState,
            placeholder = { Text("Search messages...") },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester = focusRequester)
                .onPreviewKeyEvent { keyEvent ->
                    when {
                        keyEvent.key == Key.Escape && keyEvent.type == KeyEventType.KeyDown -> {
                            onCloseSearch()
                            true
                        }

                        keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown -> {
                            onNextResult()
                            true
                        }

                        keyEvent.key == Key.F3 && keyEvent.type == KeyEventType.KeyDown -> {
                            if (keyEvent.isShiftPressed) {
                                onPreviousResult()
                            } else {
                                onNextResult()
                            }
                            true
                        }

                        else -> false
                    }
                }
        )

        // Results counter
        if (hasResults) {
            Text(
                text = "${currentResultIndex + 1}/$totalResults",
                style = JewelTheme.defaultTextStyle.copy(
                    fontSize = 12.sp,
                    color = ChatAppColors.Text.disabled
                )
            )
        } else if (searchQuery.isNotBlank()) {
            Text(
                text = "No results",
                style = JewelTheme.defaultTextStyle.copy(
                    fontSize = 12.sp,
                    color = ChatAppColors.Text.disabled
                )
            )
        }

        // Navigation buttons
        DefaultButton(
            onClick = onPreviousResult,
            enabled = hasResults && totalResults > 1,
            modifier = Modifier.widthIn(min = 40.dp)
        ) {
            Text("↑")
        }

        DefaultButton(
            onClick = onNextResult,
            enabled = hasResults && totalResults > 1,
            modifier = Modifier.widthIn(min = 40.dp)
        ) {
            Text("↓")
        }

        // Close button
        IconButton(onClick = onCloseSearch) {
            Icon(
                ChatAppIcons.Header.close,
                contentDescription = "Close search"
            )
        }
    }
}