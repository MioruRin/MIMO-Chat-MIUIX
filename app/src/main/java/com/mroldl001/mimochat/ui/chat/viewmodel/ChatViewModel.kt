package com.mroldl001.mimochat.ui.chat.viewmodel

import android.app.Application
import android.content.Intent
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mroldl001.mimochat.data.preferences.PreferencesManager
import com.mroldl001.mimochat.data.repository.ChatRepository
import com.mroldl001.mimochat.data.repository.ModelRepository
import com.mroldl001.mimochat.data.repository.StreamEvent
import com.mroldl001.mimochat.domain.model.AIModel
import com.mroldl001.mimochat.domain.model.Chat
import com.mroldl001.mimochat.domain.model.Message
import com.mroldl001.mimochat.service.ChatService
import com.mroldl001.mimochat.ui.theme.ThemeColor
import com.mroldl001.mimochat.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SkillType {
    POET,
    LEARNING
}

object SkillPrompts {
    const val POET = """
# Role
Classical poet

# Goal
Turn the user's input into a polished classical-style poem.

# Output
Return only the poem title and poem body.
"""

    const val LEARNING = """
# Role
Patient tutor

# Goal
Teach the user step by step, check understanding, and continue only after interaction.
"""

    fun getSkillPrompt(skill: SkillType): String = when (skill) {
        SkillType.POET -> POET
        SkillType.LEARNING -> LEARNING
    }
}

data class StreamState(
    val content: String = "",
    val reasoning: String = "",
    val searchResults: List<com.mroldl001.mimochat.domain.model.WebSearchResult>? = null,
    val isActive: Boolean = false
)

data class ChatUiState(
    val currentChat: Chat? = null,
    val availableModels: List<AIModel> = emptyList(),
    val selectedModel: AIModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val chats: List<Chat> = emptyList(),
    val apiKey: String = "",
    val apiBaseUrl: String = PreferencesManager.DEFAULT_API_BASE_URL,
    val themeColor: ThemeColor = ThemeColor.WHITE,
    val themeMode: ThemeMode = ThemeMode.FOLLOW_SYSTEM,
    val customSystemPrompt: String = "",
    val activeSkill: SkillType? = null,
    val temperature: Float = PreferencesManager.DEFAULT_TEMPERATURE,
    val topP: Float = PreferencesManager.DEFAULT_TOP_P,
    val frequencyPenalty: Float = PreferencesManager.DEFAULT_FREQUENCY_PENALTY,
    val presencePenalty: Float = PreferencesManager.DEFAULT_PRESENCE_PENALTY
)

private const val DEFAULT_CHAT_TITLE = "New Chat"

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val modelRepository: ModelRepository,
    private val preferencesManager: PreferencesManager,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatUiState(
            themeColor = preferencesManager.getThemeColor(),
            themeMode = preferencesManager.getThemeMode(),
            apiKey = preferencesManager.getApiKey(),
            apiBaseUrl = preferencesManager.getApiBaseUrl(),
            customSystemPrompt = preferencesManager.getCustomSystemPrompt(),
            temperature = preferencesManager.getTemperature(),
            topP = preferencesManager.getTopP(),
            frequencyPenalty = preferencesManager.getFrequencyPenalty(),
            presencePenalty = preferencesManager.getPresencePenalty()
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val messages = mutableStateListOf<Message>()

    var streamingContent = mutableStateOf("")
        private set
    var streamingReasoning = mutableStateOf("")
        private set
    var isStreaming = mutableStateOf(false)
        private set

    private var streamJob: kotlinx.coroutines.Job? = null
    private var messagesJob: kotlinx.coroutines.Job? = null
    private var chatStreamStates: MutableMap<Long, StreamState> = mutableMapOf()
    private var activeChatId: Long? = null
    private val activeStreams = mutableSetOf<Long>()

    init {
        loadModels()
        loadChats()
    }

    private fun loadModels() {
        val models = modelRepository.getModels()
        val savedModelId = preferencesManager.getSelectedModelId()
        val selectedModel = if (savedModelId.isNotBlank()) {
            models.find { it.id == savedModelId }
        } else {
            null
        }
        _uiState.update { state ->
            state.copy(
                availableModels = models,
                selectedModel = selectedModel ?: models.firstOrNull()
            )
        }
    }

    private fun loadChats() {
        viewModelScope.launch {
            chatRepository.getAllChats().collect { chats ->
                _uiState.update { it.copy(chats = chats) }
            }
        }
    }

    fun setThemeColor(color: ThemeColor) {
        _uiState.update { it.copy(themeColor = color) }
        preferencesManager.saveThemeColor(color)
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        preferencesManager.saveThemeMode(mode)
    }

    fun setApiKey(apiKey: String) {
        _uiState.update { it.copy(apiKey = apiKey) }
        preferencesManager.saveApiKey(apiKey)
    }

    fun setApiBaseUrl(url: String) {
        _uiState.update { it.copy(apiBaseUrl = url) }
        preferencesManager.saveApiBaseUrl(url)
    }

    fun setCustomSystemPrompt(prompt: String) {
        _uiState.update { it.copy(customSystemPrompt = prompt) }
        preferencesManager.saveCustomSystemPrompt(prompt)
    }

    fun setTemperature(value: Float) {
        _uiState.update { it.copy(temperature = value) }
        preferencesManager.saveTemperature(value)
    }

    fun setTopP(value: Float) {
        _uiState.update { it.copy(topP = value) }
        preferencesManager.saveTopP(value)
    }

    fun setFrequencyPenalty(value: Float) {
        _uiState.update { it.copy(frequencyPenalty = value) }
        preferencesManager.saveFrequencyPenalty(value)
    }

    fun setPresencePenalty(value: Float) {
        _uiState.update { it.copy(presencePenalty = value) }
        preferencesManager.savePresencePenalty(value)
    }

    fun resetParameters() {
        preferencesManager.resetParameters()
        _uiState.update {
            it.copy(
                temperature = PreferencesManager.DEFAULT_TEMPERATURE,
                topP = PreferencesManager.DEFAULT_TOP_P,
                frequencyPenalty = PreferencesManager.DEFAULT_FREQUENCY_PENALTY,
                presencePenalty = PreferencesManager.DEFAULT_PRESENCE_PENALTY
            )
        }
    }

    fun setActiveSkill(skill: SkillType?) {
        _uiState.update { it.copy(activeSkill = skill) }
    }

    fun selectModel(model: AIModel) {
        _uiState.update { it.copy(selectedModel = model) }
        preferencesManager.saveSelectedModelId(model.id)
        viewModelScope.launch {
            _uiState.value.currentChat?.let { chat ->
                val updatedChat = chat.copy(modelId = model.id)
                chatRepository.updateChat(updatedChat)
                _uiState.update { it.copy(currentChat = updatedChat) }
            }
        }
    }

    fun startDraftChat() {
        activeChatId?.let { currentId ->
            chatStreamStates[currentId] = StreamState(
                content = streamingContent.value,
                reasoning = streamingReasoning.value,
                searchResults = null,
                isActive = activeStreams.contains(currentId)
            )
        }

        messagesJob?.cancel()
        messages.clear()
        streamingContent.value = ""
        streamingReasoning.value = ""
        isStreaming.value = false
        activeChatId = null
        _uiState.update { it.copy(currentChat = null, error = null, isLoading = false) }
    }

    fun selectChat(chat: Chat) {
        activeChatId?.let { currentId ->
            chatStreamStates[currentId] = StreamState(
                content = streamingContent.value,
                reasoning = streamingReasoning.value,
                searchResults = null,
                isActive = activeStreams.contains(currentId)
            )
        }

        messagesJob?.cancel()
        viewModelScope.launch {
            val fullChat = chatRepository.getChatById(chat.id)
            val model = modelRepository.getModelById(chat.modelId)
            _uiState.update {
                it.copy(
                    currentChat = fullChat,
                    selectedModel = model ?: it.selectedModel
                )
            }

            val savedState = chatStreamStates[chat.id]
            val chatIsActive = activeStreams.contains(chat.id)
            if (savedState != null) {
                streamingContent.value = savedState.content
                streamingReasoning.value = savedState.reasoning
                isStreaming.value = chatIsActive
            } else {
                streamingContent.value = ""
                streamingReasoning.value = ""
                isStreaming.value = chatIsActive
            }

            activeChatId = chat.id
            messagesJob = viewModelScope.launch {
                chatRepository.getMessages(chat.id).collect { msgs ->
                    messages.clear()
                    messages.addAll(msgs)
                }
            }
        }
    }

    fun sendMessage(content: String, thinkingEnabled: Boolean = true) {
        viewModelScope.launch {
            refreshRuntimePreferences()

            if (activeStreams.contains(activeChatId)) return@launch

            if (_uiState.value.currentChat == null) {
                val modelId = _uiState.value.selectedModel?.id ?: "mimo-v2.5-pro"
                val chatId = chatRepository.createChat(
                    title = DEFAULT_CHAT_TITLE,
                    modelId = modelId
                )
                val chat = chatRepository.getChatById(chatId)
                if (chat != null) {
                    _uiState.update { it.copy(currentChat = chat, error = null) }
                    activeChatId = chat.id
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to create chat") }
                    return@launch
                }
            }

            val chat = _uiState.value.currentChat!!
            activeChatId = chat.id
            val isNewChat = chat.title == DEFAULT_CHAT_TITLE

            val userMessage = Message(
                chatId = chat.id,
                role = "user",
                content = content
            )
            chatRepository.saveMessage(userMessage)
            messages.add(userMessage)

            _uiState.update { it.copy(isLoading = true, error = null) }

            val apiKey = _uiState.value.apiKey
            if (apiKey.isBlank()) {
                _uiState.update { it.copy(isLoading = false, error = "Please set API Key first") }
                return@launch
            }

            val modelId = _uiState.value.selectedModel?.id ?: "mimo-v2.5-pro"
            val apiBaseUrl = _uiState.value.apiBaseUrl

            if (isNewChat) {
                val chatId = chat.id
                viewModelScope.launch {
                    runCatching {
                        chatRepository.generateChatTitle(
                            apiKey = apiKey,
                            baseUrl = apiBaseUrl,
                            modelId = modelId,
                            firstMessage = content
                        ).getOrNull()
                    }.getOrNull()?.let { title ->
                        val currentChatFromDb = chatRepository.getChatById(chatId)
                        if (currentChatFromDb != null && currentChatFromDb.title == DEFAULT_CHAT_TITLE) {
                            val updatedChat = currentChatFromDb.copy(title = title)
                            chatRepository.updateChat(updatedChat)
                            _uiState.update { state ->
                                val updatedChats = state.chats.map {
                                    if (it.id == chatId) it.copy(title = title) else it
                                }
                                state.copy(currentChat = updatedChat, chats = updatedChats)
                            }
                        }
                    }
                }
            }

            streamingContent.value = ""
            streamingReasoning.value = ""
            isStreaming.value = true
            activeStreams.add(chat.id)

            val contextMessages = messages.filter { !it.isAborted && !it.isFailed }.toList()
            val targetChatId = chat.id
            val contentBuffer = mutableListOf<String>()
            val reasoningBuffer = mutableListOf<String>()
            var streamError: String? = null
            var isStreamDone = false

            val activeSkill = _uiState.value.activeSkill
            val skillPrompt = activeSkill?.let { SkillPrompts.getSkillPrompt(it) } ?: ""
            val effectiveThinkingEnabled = if (activeSkill != null) false else thinkingEnabled

            application.startForegroundService(
                Intent(application, ChatService::class.java).apply {
                    action = ChatService.ACTION_START
                }
            )

            streamJob = viewModelScope.launch {
                chatRepository.sendMessageStream(
                    apiKey = apiKey,
                    baseUrl = apiBaseUrl,
                    chatId = chat.id,
                    messages = contextMessages,
                    modelId = modelId,
                    thinkingEnabled = effectiveThinkingEnabled,
                    skillPrompt = skillPrompt,
                    customSystemPrompt = _uiState.value.customSystemPrompt
                ).collect { event ->
                    when (event) {
                        is StreamEvent.ContentDelta -> contentBuffer.add(event.delta)
                        is StreamEvent.ReasoningDelta -> reasoningBuffer.add(event.delta)
                        is StreamEvent.Done -> {
                            isStreamDone = true
                            streamJob = null
                        }
                        is StreamEvent.Error -> {
                            isStreamDone = true
                            streamError = event.message
                            streamJob = null
                        }
                    }
                }
            }

            viewModelScope.launch {
                var reasoningPhase = true
                var currentContent = ""
                var currentReasoning = ""
                var lastNotificationUpdate = 0L
                val notificationUpdateInterval = 500L

                while (!isStreamDone || contentBuffer.isNotEmpty() || reasoningBuffer.isNotEmpty()) {
                    var consumed = false

                    if (reasoningPhase && reasoningBuffer.isNotEmpty()) {
                        val delta = reasoningBuffer.removeAt(0)
                        currentReasoning += delta
                        consumed = true
                    } else if (contentBuffer.isNotEmpty()) {
                        val delta = contentBuffer.removeAt(0)
                        currentContent += delta
                        consumed = true
                        reasoningPhase = false
                    } else if (reasoningBuffer.isNotEmpty()) {
                        val delta = reasoningBuffer.removeAt(0)
                        currentReasoning += delta
                        consumed = true
                    }

                    if (activeChatId == targetChatId && activeStreams.contains(targetChatId)) {
                        streamingContent.value = currentContent
                        streamingReasoning.value = currentReasoning
                    }

                    chatStreamStates[targetChatId] = StreamState(
                        content = currentContent,
                        reasoning = currentReasoning,
                        searchResults = null,
                        isActive = !isStreamDone
                    )

                    val now = System.currentTimeMillis()
                    if (now - lastNotificationUpdate > notificationUpdateInterval) {
                        val displayText = when {
                            reasoningPhase && currentReasoning.isNotBlank() -> "Thinking..."
                            currentContent.isNotBlank() -> {
                                val preview = currentContent.take(30)
                                if (currentContent.length > 30) "$preview..." else preview
                            }
                            else -> "MiMo is replying..."
                        }
                        application.startService(
                            Intent(application, ChatService::class.java).apply {
                                action = ChatService.ACTION_UPDATE_NOTIFICATION
                                putExtra(ChatService.EXTRA_NOTIFICATION_TEXT, displayText)
                            }
                        )
                        lastNotificationUpdate = now
                    }

                    delay(if (consumed) 8 else 4)
                }

                if (activeChatId == targetChatId) {
                    isStreaming.value = false
                }
                activeStreams.remove(targetChatId)

                chatStreamStates[targetChatId] = StreamState(
                    content = currentContent,
                    reasoning = currentReasoning,
                    searchResults = null,
                    isActive = false
                )

                val error = streamError
                if (error != null) {
                    val lastUserMessage = messages.lastOrNull { it.role == "user" && it.chatId == targetChatId }
                    if (lastUserMessage != null && !lastUserMessage.isFailed) {
                        val failedUserMessage = lastUserMessage.copy(isFailed = true)
                        viewModelScope.launch {
                            chatRepository.updateMessage(failedUserMessage)
                        }
                    }

                    val displayError = if (
                        error.contains("Unable to resolve host") ||
                        error.contains("Failed to connect") ||
                        error.contains("timeout") ||
                        error.contains("network") ||
                        error.contains("Network")
                    ) {
                        "网络不可用：$error"
                    } else {
                        error
                    }

                    if (activeChatId == targetChatId) {
                        _uiState.update { it.copy(isLoading = false, error = displayError) }
                    }
                } else {
                    val finalMessage = Message(
                        chatId = targetChatId,
                        role = "assistant",
                        content = currentContent,
                        reasoningContent = currentReasoning.ifBlank { null }
                    )
                    chatRepository.saveMessage(finalMessage)
                    if (activeChatId == targetChatId) {
                        messages.add(finalMessage)
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }

                if (activeStreams.isEmpty()) {
                    application.startService(
                        Intent(application, ChatService::class.java).apply {
                            action = ChatService.ACTION_STOP
                        }
                    )
                }
            }
        }
    }

    fun stopGenerating() {
        activeStreams.remove(activeChatId)
        streamJob?.cancel()
        streamJob = null
        isStreaming.value = false

        val content = streamingContent.value
        val reasoning = streamingReasoning.value
        val chatId = _uiState.value.currentChat?.id ?: 0

        chatId.takeIf { it > 0 }?.let {
            chatStreamStates[it] = StreamState(content, reasoning, null, false)
        }

        val lastUserIndex = messages.indexOfLast { it.role == "user" }
        if (lastUserIndex >= 0) {
            val lastUserMessage = messages[lastUserIndex]
            if (!lastUserMessage.isAborted) {
                val abortedUserMessage = lastUserMessage.copy(isAborted = true)
                messages[lastUserIndex] = abortedUserMessage
                viewModelScope.launch {
                    chatRepository.updateMessage(abortedUserMessage)
                }
            }
        }

        if (content.isNotBlank() || reasoning.isNotBlank()) {
            val abortedMessage = Message(
                chatId = chatId,
                role = "assistant",
                content = content,
                reasoningContent = reasoning.ifBlank { null },
                isAborted = true
            )
            messages.add(abortedMessage)
            viewModelScope.launch {
                chatRepository.saveMessage(abortedMessage)
            }
        }

        streamingContent.value = ""
        streamingReasoning.value = ""
        _uiState.update { it.copy(isLoading = false) }

        if (activeStreams.isEmpty()) {
            application.startService(
                Intent(application, ChatService::class.java).apply {
                    action = ChatService.ACTION_STOP
                }
            )
        }
    }

    fun deleteChat(chat: Chat) {
        viewModelScope.launch {
            chatRepository.deleteChat(chat.id)
            if (_uiState.value.currentChat?.id == chat.id) {
                messagesJob?.cancel()
                messages.clear()
                _uiState.update { it.copy(currentChat = null) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun refreshRuntimePreferences() {
        _uiState.update {
            it.copy(
                apiKey = preferencesManager.getApiKey(),
                apiBaseUrl = preferencesManager.getApiBaseUrl(),
                customSystemPrompt = preferencesManager.getCustomSystemPrompt(),
                temperature = preferencesManager.getTemperature(),
                topP = preferencesManager.getTopP(),
                frequencyPenalty = preferencesManager.getFrequencyPenalty(),
                presencePenalty = preferencesManager.getPresencePenalty()
            )
        }
    }
}
