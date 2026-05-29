package com.mroldl001.mimochat.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mroldl001.mimochat.data.api.ChatCompletionChunk
import com.mroldl001.mimochat.data.api.ChatCompletionRequest
import com.mroldl001.mimochat.data.api.ChatCompletionResponse
import com.mroldl001.mimochat.data.api.MessageRequest
import com.mroldl001.mimochat.data.api.ThinkingConfig
import com.mroldl001.mimochat.data.local.ChatDao
import com.mroldl001.mimochat.data.local.ChatEntity
import com.mroldl001.mimochat.data.local.MessageDao
import com.mroldl001.mimochat.data.local.MessageEntity
import com.mroldl001.mimochat.data.preferences.PreferencesManager
import com.mroldl001.mimochat.di.ApiServiceFactory
import com.mroldl001.mimochat.domain.model.ApiErrorCode
import com.mroldl001.mimochat.domain.model.Chat
import com.mroldl001.mimochat.domain.model.Message
import com.mroldl001.mimochat.domain.model.SearchResult
import com.mroldl001.mimochat.domain.model.WebSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val chatDao: ChatDao,
    private val apiServiceFactory: ApiServiceFactory,
    private val okHttpClient: OkHttpClient,
    private val preferencesManager: PreferencesManager
) {
    private val gson = Gson()
    private val searchResultsType = object : TypeToken<List<WebSearchResult>>() {}.type

    fun getAllChats(): Flow<List<Chat>> {
        return chatDao.getAllChats().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getChatById(chatId: Long): Chat? {
        return chatDao.getChatById(chatId)?.toDomain()
    }

    suspend fun createChat(title: String, modelId: String): Long {
        val entity = ChatEntity(
            title = title,
            modelId = modelId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return chatDao.insertChat(entity)
    }

    suspend fun updateChat(chat: Chat) {
        chatDao.updateChat(chat.toEntity())
    }

    suspend fun deleteChat(chatId: Long) {
        chatDao.deleteChatById(chatId)
        messageDao.deleteMessagesByChatId(chatId)
    }

    fun getMessages(chatId: Long): Flow<List<Message>> {
        return messageDao.getMessagesByChatId(chatId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun searchMessages(query: String): Flow<List<SearchResult>> {
        return messageDao.searchMessagesWithChat(query).map { messageEntities ->
            messageEntities.mapNotNull { messageEntity ->
                val chatEntity = chatDao.getChatById(messageEntity.chatId)
                if (chatEntity != null) {
                    SearchResult(
                        message = messageEntity.toDomain(),
                        chat = chatEntity.toDomain(),
                        highlightedContent = highlightSearchResult(messageEntity.content, query)
                    )
                } else {
                    null
                }
            }
        }
    }

    private fun highlightSearchResult(content: String, query: String, contextLength: Int = 50): String {
        val index = content.indexOf(query, ignoreCase = true)
        if (index == -1) return content.take(100)

        val start = maxOf(0, index - contextLength)
        val end = minOf(content.length, index + query.length + contextLength)
        val prefix = if (start > 0) "..." else ""
        val suffix = if (end < content.length) "..." else ""
        return "$prefix${content.substring(start, end)}$suffix"
    }

    suspend fun saveMessage(message: Message): Long {
        return messageDao.insertMessage(message.toEntity())
    }

    suspend fun updateMessage(message: Message) {
        messageDao.updateMessage(message.toEntity())
    }

    suspend fun sendMessage(
        apiKey: String,
        baseUrl: String,
        chatId: Long,
        messages: List<Message>,
        modelId: String,
        thinkingEnabled: Boolean = true,
        skillPrompt: String = "",
        customSystemPrompt: String = ""
    ): Result<Message> {
        return try {
            val chatRequest = buildRequest(modelId, messages, thinkingEnabled, stream = false, skillPrompt, customSystemPrompt)
            val apiService = apiServiceFactory.getService(baseUrl)
            val response = apiService.createChatCompletion(
                apiKey = apiKey,
                request = chatRequest
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                return Result.failure(Exception(buildApiErrorMessage(response.code(), errorBody, baseUrl, apiKey)))
            }

            val chatCompletionResponse = response.body()
                ?: return Result.failure(Exception("API returned an empty response"))

            val assistantMessage = chatCompletionResponse.choices.firstOrNull()?.message
                ?: return Result.failure(Exception("No response from API"))

            Result.success(
                Message(
                    chatId = chatId,
                    role = "assistant",
                    content = assistantMessage.content ?: "",
                    reasoningContent = assistantMessage.reasoningContent
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Send failed: ${e.message ?: "Unknown error"}"))
        }
    }

    fun sendMessageStream(
        apiKey: String,
        baseUrl: String,
        chatId: Long,
        messages: List<Message>,
        modelId: String,
        thinkingEnabled: Boolean = true,
        skillPrompt: String = "",
        customSystemPrompt: String = ""
    ): Flow<StreamEvent> = flow {
        try {
            val chatRequest = buildRequest(modelId, messages, thinkingEnabled, stream = true, skillPrompt, customSystemPrompt)
            val normalizedBaseUrl = baseUrl.trimEnd('/')
            val endpoint = if (normalizedBaseUrl.endsWith("/v1")) {
                "$normalizedBaseUrl/chat/completions"
            } else {
                "$normalizedBaseUrl/v1/chat/completions"
            }

            val json = gson.toJson(chatRequest)
            executeChatCompletionRequest(endpoint, apiKey, json).use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No error body"
                    emit(StreamEvent.Error(buildApiErrorMessage(response.code, errorBody, baseUrl, apiKey)))
                    return@flow
                }

                val body = response.body
                if (body == null) {
                    emit(StreamEvent.Error("Response body is empty"))
                    return@flow
                }

                val source = body.source()
                var content = ""
                var reasoningContent = ""

                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: continue
                    if (!line.startsWith("data: ")) continue

                    val data = line.substring(6)
                    if (data == "[DONE]") {
                        emit(
                            StreamEvent.Done(
                                Message(
                                    chatId = chatId,
                                    role = "assistant",
                                    content = content,
                                    reasoningContent = reasoningContent.ifBlank { null }
                                )
                            )
                        )
                        break
                    }

                    runCatching {
                        gson.fromJson(data, ChatCompletionChunk::class.java)
                    }.getOrNull()?.let { chunk ->
                        val delta = chunk.choices.firstOrNull()?.delta

                        delta?.content?.let {
                            content += it
                            emit(StreamEvent.ContentDelta(it, content))
                        }

                        delta?.reasoningContent?.let {
                            reasoningContent += it
                            emit(StreamEvent.ReasoningDelta(it, reasoningContent))
                        }

                        if (chunk.choices.firstOrNull()?.finish_reason != null) {
                            emit(
                                StreamEvent.Done(
                                    Message(
                                        chatId = chatId,
                                        role = "assistant",
                                        content = content,
                                        reasoningContent = reasoningContent.ifBlank { null }
                                    )
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            emit(StreamEvent.Error(e.message ?: "Stream failed"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun generateChatTitle(
        apiKey: String,
        baseUrl: String,
        modelId: String,
        firstMessage: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val promptMessages = listOf(
                    MessageRequest(
                        role = "system",
                        content = "Generate a concise title from the user's first message. Return only the title."
                    ),
                    MessageRequest(
                        role = "user",
                        content = "Create a title for this conversation: $firstMessage"
                    )
                )

                val requestBody = ChatCompletionRequest(
                    model = modelId,
                    messages = promptMessages,
                    stream = false,
                    temperature = 0.7,
                    maxCompletionTokens = 128,
                    thinking = ThinkingConfig("disabled")
                )

                val normalizedBaseUrl = baseUrl.trimEnd('/')
                val url = if (normalizedBaseUrl.endsWith("/v1")) {
                    "$normalizedBaseUrl/chat/completions"
                } else {
                    "$normalizedBaseUrl/v1/chat/completions"
                }

                val json = gson.toJson(requestBody)
                val request = Request.Builder()
                    .url(url)
                    .addAuthHeaders(apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(json.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception(ApiErrorCode.getDisplayMessage(response.code)))
                }

                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty response"))
                val chatResponse = gson.fromJson(responseBody, ChatCompletionResponse::class.java)
                val title = chatResponse.choices.firstOrNull()?.message?.content?.trim()
                    ?: return@withContext Result.failure(Exception("Empty title"))

                Result.success(title)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun buildRequest(
        modelId: String,
        messages: List<Message>,
        thinkingEnabled: Boolean,
        stream: Boolean,
        skillPrompt: String = "",
        customSystemPrompt: String = ""
    ): ChatCompletionRequest {
        val effectiveThinking = if (thinkingEnabled) ThinkingConfig("enabled") else ThinkingConfig("disabled")

        val dateFormatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val weekFormatter = java.text.SimpleDateFormat("EEEE", java.util.Locale.CHINA)
        val today = java.util.Date()
        val dateStr = dateFormatter.format(today)
        val weekStr = weekFormatter.format(today)

        val systemPromptParts = mutableListOf(
            """
            [Official prompt]
            You are MiMo, Xiaomi's AI assistant.
            Today is $dateStr ($weekStr).
            Knowledge cutoff: February 2024.
            Formatting rules:
            1. Use inline code for literal dollar signs like `$`.
            2. Use block LaTeX with `$$ ... $$` for display formulas.
            3. Keep answers clean and readable.
            """.trimIndent()
        )

        if (skillPrompt.isNotBlank()) {
            systemPromptParts.add(
                """
                [Skill prompt]
                $skillPrompt
                """.trimIndent()
            )
        }

        if (customSystemPrompt.isNotBlank()) {
            systemPromptParts.add(
                """
                [Custom prompt]
                $customSystemPrompt
                """.trimIndent()
            )
        }

        systemPromptParts.add(
            """
            [Priority]
            If prompts conflict, follow this order:
            official prompt > skill prompt > custom user prompt
            """.trimIndent()
        )

        val requestMessages = mutableListOf<MessageRequest>()
        requestMessages.add(MessageRequest("system", systemPromptParts.joinToString("\n\n")))
        requestMessages.addAll(messages.map { message ->
            MessageRequest(
                role = message.role,
                content = message.content,
                reasoningContent = if (message.role == "assistant" && !message.reasoningContent.isNullOrBlank()) {
                    message.reasoningContent
                } else {
                    null
                }
            )
        })

        return ChatCompletionRequest(
            model = modelId,
            messages = requestMessages,
            stream = stream,
            thinking = effectiveThinking,
            tools = null,
            toolChoice = null,
            temperature = preferencesManager.getTemperature().toDouble(),
            topP = preferencesManager.getTopP().toDouble(),
            maxCompletionTokens = 1024,
            stop = null,
            frequencyPenalty = preferencesManager.getFrequencyPenalty().toDouble(),
            presencePenalty = preferencesManager.getPresencePenalty().toDouble()
        )
    }

    private fun executeChatCompletionRequest(
        endpoint: String,
        apiKey: String,
        json: String
    ): Response {
        val request = Request.Builder()
            .url(endpoint)
            .addAuthHeaders(apiKey)
            .addHeader("Content-Type", "application/json")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()
        return okHttpClient.newCall(request).execute()
    }

    private fun Request.Builder.addAuthHeaders(apiKey: String): Request.Builder {
        addHeader("api-key", apiKey)
        addHeader("Authorization", "Bearer $apiKey")
        return this
    }

    private fun buildApiErrorMessage(
        statusCode: Int,
        errorBody: String,
        baseUrl: String,
        apiKey: String
    ): String {
        val tokenPlanHint = if (statusCode == 401 && apiKey.startsWith("tp-") && baseUrl.contains("api.xiaomimimo.com")) {
            "\n检测到 tp- Token Plan Key，当前仍在使用标准接口。请在设置的接口地址里选择订阅接口：${PreferencesManager.TOKEN_PLAN_API_BASE_URL}"
        } else {
            ""
        }
        return "HTTP $statusCode: $errorBody$tokenPlanHint"
    }

    private fun ChatEntity.toDomain() = Chat(
        id = id,
        title = title,
        modelId = modelId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Chat.toEntity() = ChatEntity(
        id = id,
        title = title,
        modelId = modelId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun MessageEntity.toDomain() = Message(
        id = id,
        chatId = chatId,
        role = role,
        content = content,
        reasoningContent = reasoningContent,
        searchResults = searchResultsJson?.let { json ->
            runCatching { gson.fromJson<List<WebSearchResult>>(json, searchResultsType) }.getOrNull()
        },
        timestamp = timestamp,
        isStreaming = isStreaming,
        isAborted = isAborted,
        isFailed = isFailed
    )

    private fun Message.toEntity() = MessageEntity(
        id = id,
        chatId = chatId,
        role = role,
        content = content,
        reasoningContent = reasoningContent,
        searchResultsJson = searchResults?.let { gson.toJson(it) },
        timestamp = timestamp,
        isStreaming = isStreaming,
        isAborted = isAborted,
        isFailed = isFailed
    )
}

sealed class StreamEvent {
    data class ContentDelta(val delta: String, val accumulated: String) : StreamEvent()
    data class ReasoningDelta(val delta: String, val accumulated: String) : StreamEvent()
    data class Done(val message: Message) : StreamEvent()
    data class Error(val message: String) : StreamEvent()
}
