package com.phodal.lotus.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.*
import com.phodal.lotus.chat.history.ConversationHistory
import com.phodal.lotus.chat.ChatAppColors
import com.phodal.lotus.chat.ChatAppIcons
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ConversationHistoryDialog(
    conversations: List<ConversationHistory>,
    currentConversationId: String?,
    onSelectConversation: (String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onRenameConversation: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }

    val filteredConversations = conversations.filter { conversation ->
        conversation.title.contains(searchQuery, ignoreCase = true) ||
                conversation.messages.any { it.content.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .size(400.dp, 600.dp)
                .background(ChatAppColors.Panel.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Text(
                    text = "Conversations",
                    style = JewelTheme.defaultTextStyle.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.padding(12.dp)
                )

                Divider(Orientation.Horizontal, modifier = Modifier.fillMaxWidth().height(1.dp))

                // Search field
                val searchFieldState = rememberTextFieldState(searchQuery)
                LaunchedEffect(Unit) {
                    snapshotFlow { searchFieldState.text.toString() }
                        .collect { query -> searchQuery = query }
                }

                TextField(
                    state = searchFieldState,
                    placeholder = { Text("Search...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )

                Divider(Orientation.Horizontal, modifier = Modifier.fillMaxWidth().height(1.dp))

                // Conversation list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredConversations, key = { it.id }) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            isSelected = conversation.id == currentConversationId,
                            onClick = { 
                                onSelectConversation(conversation.id)
                                onDismiss()
                            },
                            onDelete = { onDeleteConversation(conversation.id) },
                            onRename = {
                                showRenameDialog = conversation.id
                                renameText = conversation.title
                            }
                        )
                    }
                }
            }
        }
    }

    // Rename dialog
    if (showRenameDialog != null) {
        RenameConversationDialog(
            currentTitle = renameText,
            onConfirm = { newTitle ->
                onRenameConversation(showRenameDialog!!, newTitle)
                showRenameDialog = null
            },
            onDismiss = { showRenameDialog = null }
        )
    }
}

@Composable
private fun ConversationItem(
    conversation: ConversationHistory,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
        .withZone(ZoneId.systemDefault())
    val formattedTime = formatter.format(Instant.ofEpochMilli(conversation.updatedAt))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) ChatAppColors.Panel.background.copy(alpha = 0.8f)
                else ChatAppColors.Panel.background
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = conversation.title,
                style = JewelTheme.defaultTextStyle.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "$formattedTime • ${conversation.messages.size} messages",
                style = JewelTheme.defaultTextStyle.copy(
                    fontSize = 10.sp,
                    color = ChatAppColors.Text.disabled
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onRename, modifier = Modifier.size(24.dp)) {
            Icon(
                ChatAppIcons.Header.search,
                contentDescription = "Rename",
                modifier = Modifier.size(16.dp)
            )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(
                ChatAppIcons.Header.close,
                contentDescription = "Delete",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun RenameConversationDialog(
    currentTitle: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newTitle by remember { mutableStateOf(currentTitle) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .size(300.dp, 150.dp)
                .background(ChatAppColors.Panel.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Rename Conversation",
                    style = JewelTheme.defaultTextStyle.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )

                val textFieldState = rememberTextFieldState(newTitle)
                LaunchedEffect(Unit) {
                    snapshotFlow { textFieldState.text.toString() }
                        .collect { text -> newTitle = text }
                }

                TextField(
                    state = textFieldState,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    DefaultButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    DefaultButton(onClick = { onConfirm(newTitle) }) {
                        Text("Rename")
                    }
                }
            }
        }
    }
}

