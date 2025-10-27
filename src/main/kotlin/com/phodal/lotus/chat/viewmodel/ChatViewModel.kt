package com.phodal.lotus.chat.viewmodel

import com.intellij.openapi.Disposable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.phodal.lotus.chat.model.ChatMessage
import com.phodal.lotus.chat.repository.ChatRepositoryApi
import com.phodal.lotus.aicore.config.LLMProvider
import com.phodal.lotus.aicore.config.LLMConfigManager
import com.phodal.lotus.aicore.AIServiceFactory
import com.phodal.lotus.chat.config.AIConfigService

interface ChatViewModelApi : Disposable {
    val chatMessagesFlow: StateFlow<List<ChatMessage>>

    fun onPromptInputChanged(input: String)

    fun onSendMessage()

    fun onAbortSendingMessage()

    fun searchChatMessagesHandler(): SearchChatMessagesHandler

    val promptInputState: StateFlow<MessageInputState>

    fun onAIConfigSaved(provider: LLMProvider, apiKey: String)

    val isAIConfigured: StateFlow<Boolean>

    val currentAIProvider: StateFlow<LLMProvider?>
}

class ChatViewModel(
    private val coroutineScope: CoroutineScope,
    private val repository: ChatRepositoryApi
) : ChatViewModelApi {

    private val configService: AIConfigService = AIConfigService.getInstance()

    private val _chatMessagesFlow = MutableStateFlow(emptyList<ChatMessage>())

    override val chatMessagesFlow: StateFlow<List<ChatMessage>> = _chatMessagesFlow.asStateFlow()

    private val _promptInputState = MutableStateFlow<MessageInputState>(MessageInputState.Disabled)
    override val promptInputState: StateFlow<MessageInputState> = _promptInputState.asStateFlow()

    private val _isAIConfigured = MutableStateFlow(false)
    override val isAIConfigured: StateFlow<Boolean> = _isAIConfigured.asStateFlow()

    private val _currentAIProvider = MutableStateFlow<LLMProvider?>(null)
    override val currentAIProvider: StateFlow<LLMProvider?> = _currentAIProvider.asStateFlow()

    private val searchChatMessagesHandler: SearchChatMessagesHandler = SearchChatMessagesHandlerImpl(
        coroutineScope = coroutineScope,
        messagesFlow = repository.messagesFlow
    )

    /**
     * A nullable [Job] instance used to manage the coroutine responsible for sending a message.
     * This property holds a reference to the currently active job related to the `onSendMessage`
     * operation in the [ChatViewModel]. It enables tracking, cancellation, and lifecycle management
     * of the send message process.
     */
    private var currentSendMessageJob: Job? = null

    init {
        // Initialize AIServiceFactory with the config service
        AIServiceFactory.initialize(configService)

        // Emit all messages from the repository to the UI
        repository
            .messagesFlow
            .onEach { messages -> _chatMessagesFlow.value = messages }
            .launchIn(coroutineScope)

        // Monitor AI configuration status
        configService.currentConfig
            .onEach { config ->
                _isAIConfigured.value = config != null
                _currentAIProvider.value = config?.provider
                // Update AIServiceFactory when config changes
                AIServiceFactory.updateAIClient()
            }
            .launchIn(coroutineScope)
    }

    override fun onPromptInputChanged(input: String) {
        val currentPromptInputState = _promptInputState.value
        _promptInputState.value = when {
            currentPromptInputState is MessageInputState.Sending -> MessageInputState.Sending(input)
            input.isEmpty() -> MessageInputState.Disabled
            else -> MessageInputState.Enabled(input)
        }
    }

    override fun onSendMessage() {
        currentSendMessageJob = coroutineScope.launch {
            try {
                val currentUserMessage = getCurrentInputTextIfNotEmpty() ?: return@launch
                emitPromptInputState(MessageInputState.Sending(""))

                repository.sendMessage(currentUserMessage)

                emitPromptInputState(
                    when (val currentInputState = getCurrentInputTextIfNotEmpty()) {
                        null -> MessageInputState.Disabled
                        else -> MessageInputState.Enabled(currentInputState)
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) throw e

                emitPromptInputState(MessageInputState.SendFailed(e.message ?: "Unknown error", e))
            }
        }
    }

    override fun onAbortSendingMessage() {
        currentSendMessageJob?.cancel()

        emitPromptInputState(
            when (val currentPromptInput = getCurrentInputTextIfNotEmpty()) {
                null -> MessageInputState.Disabled
                else -> MessageInputState.Enabled(currentPromptInput)
            }
        )
    }

    override fun searchChatMessagesHandler(): SearchChatMessagesHandler = searchChatMessagesHandler

    override fun onAIConfigSaved(provider: LLMProvider, apiKey: String) {
        val config = com.phodal.lotus.aicore.config.LLMConfig(
            provider = provider,
            apiKey = apiKey
        )
        configService.saveConfig(config)
    }

    override fun dispose() {
        coroutineScope.cancel()
    }

    private fun emitPromptInputState(state: MessageInputState) {
        _promptInputState.value = state
    }

    private fun getCurrentInputTextIfNotEmpty(): String? = _promptInputState.value.inputText.takeIf { it.isNotBlank() }
}