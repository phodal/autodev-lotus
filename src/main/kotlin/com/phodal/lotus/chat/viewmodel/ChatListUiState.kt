package com.phodal.lotus.chat.viewmodel

import com.phodal.lotus.chat.model.ChatMessage

data class ChatListUiState(
    val messages: List<ChatMessage> = emptyList(),
) {
    companion object Companion {
        val EMPTY = ChatListUiState()
    }
}