package com.mroldl001.mimochat.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mroldl001.mimochat.domain.model.Chat
import com.mroldl001.mimochat.domain.model.SearchResult
import com.mroldl001.mimochat.ui.chat.components.InputBar
import com.mroldl001.mimochat.ui.chat.components.MessageBubble
import com.mroldl001.mimochat.ui.chat.components.ModelSelector
import com.mroldl001.mimochat.ui.chat.components.SkillToggleBar
import com.mroldl001.mimochat.ui.chat.components.StreamingMessageBubble
import com.mroldl001.mimochat.ui.chat.viewmodel.ChatViewModel
import com.mroldl001.mimochat.ui.search.SearchBarFake
import com.mroldl001.mimochat.ui.search.SearchPager
import com.mroldl001.mimochat.ui.search.SearchStatus
import com.mroldl001.mimochat.ui.search.SearchViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FabPosition
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Messages
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class ChatStage {
    Archive,
    Detail,
}

@Composable
fun MiuixChatPage(
    outerPadding: PaddingValues,
    selectedChatId: Long?,
    onDetailModeChanged: (Boolean) -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenChat: (Long) -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messages = viewModel.messages
    val streamingContent by viewModel.streamingContent
    val streamingReasoning by viewModel.streamingReasoning
    val isStreaming by viewModel.isStreaming
    val searchQuery by searchViewModel.searchQuery
    val searchResults by searchViewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by searchViewModel.isSearching
    val hasSearched by searchViewModel.hasSearched
    val listState = rememberLazyListState()
    val backdrop = rememberHostBackdrop()
    var stage by remember { mutableStateOf(ChatStage.Archive) }
    var isThinkingMode by remember { mutableStateOf(false) }
    var searchStatus by remember { mutableStateOf(SearchStatus(label = "搜索")) }

    LaunchedEffect(searchQuery, searchResults, isSearching, hasSearched) {
        searchStatus = searchStatus.copy(
            searchText = searchQuery,
            resultStatus = when {
                searchQuery.isBlank() -> SearchStatus.ResultStatus.DEFAULT
                isSearching -> SearchStatus.ResultStatus.LOAD
                hasSearched && searchResults.isEmpty() -> SearchStatus.ResultStatus.EMPTY
                searchResults.isNotEmpty() -> SearchStatus.ResultStatus.SHOW
                else -> SearchStatus.ResultStatus.DEFAULT
            },
        )
    }

    LaunchedEffect(selectedChatId, uiState.chats) {
        selectedChatId?.let { chatId ->
            uiState.chats.find { it.id == chatId }?.let {
                viewModel.selectChat(it)
                stage = ChatStage.Detail
            }
        }
    }

    LaunchedEffect(messages.size, isStreaming) {
        if (stage == ChatStage.Detail) {
            val totalItems = messages.size + if (isStreaming) 1 else 0
            if (totalItems > 0) {
                listState.animateScrollToItem(totalItems - 1)
            }
        }
    }

    LaunchedEffect(stage) {
        onDetailModeChanged(stage == ChatStage.Detail)
    }

    BackHandler(enabled = stage == ChatStage.Detail) {
        stage = ChatStage.Archive
    }

    AnimatedContent(
        targetState = stage,
        transitionSpec = {
            if (targetState == ChatStage.Detail) {
                slideInHorizontally(initialOffsetX = { it / 3 }) + fadeIn() togetherWith
                    slideOutHorizontally(targetOffsetX = { -it / 5 }) + fadeOut()
            } else {
                slideInHorizontally(initialOffsetX = { -it / 5 }) + fadeIn() togetherWith
                    slideOutHorizontally(targetOffsetX = { it / 3 }) + fadeOut()
            }
        },
        label = "chat_stage_transition",
    ) { currentStage ->
        when (currentStage) {
            ChatStage.Archive -> {
                ChatArchivePage(
                    outerPadding = outerPadding,
                    backdrop = backdrop,
                    chats = uiState.chats,
                    currentChatId = uiState.currentChat?.id,
                    searchStatus = searchStatus,
                    searchResults = searchResults,
                    onNewChat = {
                        viewModel.startDraftChat()
                        stage = ChatStage.Detail
                    },
                    onOpenChat = { chat ->
                        viewModel.selectChat(chat)
                        stage = ChatStage.Detail
                        onOpenChat(chat.id)
                    },
                    onSearchStatusChange = { next ->
                        searchStatus = next
                        when {
                            next.searchText != searchQuery -> {
                                searchViewModel.updateQuery(next.searchText)
                                searchViewModel.performSearch()
                            }
                            next.current == SearchStatus.Status.COLLAPSING -> searchViewModel.clearSearch()
                        }
                    },
                    onOpenSearchChat = { chatId ->
                        onOpenChat(chatId)
                        stage = ChatStage.Detail
                    },
                )
            }

            ChatStage.Detail -> {
                ChatDetailPage(
                    outerPadding = outerPadding,
                    backdrop = backdrop,
                    currentChatTitle = uiState.currentChat?.title ?: "新对话",
                    models = uiState.availableModels,
                    selectedModel = uiState.selectedModel,
                    activeSkill = uiState.activeSkill,
                    errorMessage = uiState.error,
                    messagesEmpty = messages.isEmpty() && !isStreaming,
                    messagesContent = {
                        items(messages, key = { it.id }) { message ->
                            MessageBubble(message = message)
                        }
                        if (isStreaming) {
                            item {
                                StreamingMessageBubble(
                                    content = streamingContent,
                                    reasoningContent = streamingReasoning,
                                )
                            }
                        }
                    },
                    listState = listState,
                    isStreaming = isStreaming,
                    isThinkingMode = isThinkingMode,
                    onThinkingModeToggle = { isThinkingMode = it },
                    onSkillToggle = { viewModel.setActiveSkill(it) },
                    onModelSelected = { viewModel.selectModel(it) },
                    onSendMessage = { message ->
                        viewModel.sendMessage(message, isThinkingMode)
                    },
                    onStopGenerating = { viewModel.stopGenerating() },
                    onDismissError = { viewModel.clearError() },
                    onNavigateBack = { stage = ChatStage.Archive },
                )
            }
        }
    }
}

@Composable
private fun ChatArchivePage(
    outerPadding: PaddingValues,
    backdrop: top.yukonga.miuix.kmp.blur.LayerBackdrop?,
    chats: List<Chat>,
    currentChatId: Long?,
    searchStatus: SearchStatus,
    searchResults: List<SearchResult>,
    onNewChat: () -> Unit,
    onOpenChat: (Chat) -> Unit,
    onSearchStatusChange: (SearchStatus) -> Unit,
    onOpenSearchChat: (Long) -> Unit,
) {
    val topAppBarScrollBehavior = MiuixScrollBehavior()
    val barColor = if (backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface
    val latestChat = chats.firstOrNull()
    val density = LocalDensity.current
    var searchOffsetY by remember { mutableStateOf(0.dp) }

    Scaffold(
        topBar = {
            HostBlurredBar(backdrop) {
                searchStatus.TopAppBarAnim(backgroundColor = barColor) {
                    HostAdaptiveTopAppBar(
                        title = "MIMO Chat",
                        largeTitle = "MIMO Chat",
                        subtitle = "会话归档",
                        scrollBehavior = topAppBarScrollBehavior,
                        color = barColor,
                        bottomContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(if (searchStatus.isCollapsed()) 1f else 0f)
                                    .onGloballyPositioned { coordinates ->
                                        with(density) {
                                            searchOffsetY = coordinates.positionInWindow().y.toDp()
                                        }
                                    }
                                    .then(
                                        if (searchStatus.isCollapsed()) {
                                            Modifier.clickable {
                                                onSearchStatusChange(searchStatus.copy(current = SearchStatus.Status.EXPANDING))
                                            }
                                        } else {
                                            Modifier
                                        },
                                    ),
                            ) {
                                SearchBarFake(label = searchStatus.label)
                            }
                        },
                    )
                }
            }
        },
        popupHost = @Composable {
            searchStatus.SearchPager(
                onSearchStatusChange = onSearchStatusChange,
                offsetY = searchOffsetY,
                defaultResult = {
                    SearchDefaultArchiveResult()
                },
            ) {
                items(searchResults, key = { "${it.chat.id}-${it.message.id}" }) { result ->
                    ArchiveSearchResultCard(
                        result = result,
                        onClick = { onOpenSearchChat(result.chat.id) },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChat,
                modifier = Modifier.padding(end = 12.dp, bottom = outerPadding.calculateBottomPadding() + 12.dp),
            ) {
                Icon(
                    imageVector = MiuixIcons.Add,
                    contentDescription = "新建对话",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
        },
        floatingActionButtonPosition = FabPosition.EndOverlay,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .hostPageScrollModifiers(true, topAppBarScrollBehavior),
            contentPadding = hostPageContentPadding(innerPadding, outerPadding, extraBottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (latestChat != null) {
                item {
                    SmallTitle("继续最近对话")
                }
                item {
                    FeaturedHistoryCard(
                        chat = latestChat,
                        onClick = { onOpenChat(latestChat) },
                    )
                }
            }
            item {
                SmallTitle(if (chats.isEmpty()) "对话" else "全部对话")
            }
            if (chats.isEmpty()) {
                item {
                    EmptyArchiveCard()
                }
            } else {
                items(chats, key = { it.id }) { chat ->
                    HistoryCard(
                        chat = chat,
                        selected = chat.id == currentChatId,
                        onClick = { onOpenChat(chat) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyArchiveCard() {
    Card(modifier = Modifier.padding(horizontal = 12.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "还没有历史会话",
                style = MiuixTheme.textStyles.title3,
                color = MiuixTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "开始第一段对话后，返回主页就会自动归档到这里。",
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
private fun ChatDetailPage(
    outerPadding: PaddingValues,
    backdrop: top.yukonga.miuix.kmp.blur.LayerBackdrop?,
    currentChatTitle: String,
    models: List<com.mroldl001.mimochat.domain.model.AIModel>,
    selectedModel: com.mroldl001.mimochat.domain.model.AIModel?,
    activeSkill: com.mroldl001.mimochat.ui.chat.viewmodel.SkillType?,
    errorMessage: String?,
    messagesEmpty: Boolean,
    messagesContent: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState,
    isStreaming: Boolean,
    isThinkingMode: Boolean,
    onThinkingModeToggle: (Boolean) -> Unit,
    onSkillToggle: (com.mroldl001.mimochat.ui.chat.viewmodel.SkillType?) -> Unit,
    onModelSelected: (com.mroldl001.mimochat.domain.model.AIModel) -> Unit,
    onSendMessage: (String) -> Unit,
    onStopGenerating: () -> Unit,
    onDismissError: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val topAppBarScrollBehavior = MiuixScrollBehavior()
    val barColor = if (backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface

    Scaffold(
        topBar = {
            HostBlurredBar(backdrop) {
                HostAdaptiveTopAppBar(
                    title = currentChatTitle,
                    largeTitle = currentChatTitle,
                    subtitle = "",
                    scrollBehavior = topAppBarScrollBehavior,
                    color = barColor,
                    navigationIcon = {
                        HostHeaderIconButton(
                            icon = MiuixIcons.Back,
                            contentDescription = "返回归档",
                            onClick = onNavigateBack,
                        )
                    },
                )
            }
        },
        bottomBar = {
            HostBlurredBar(backdrop) {
                InputBar(
                    onSendMessage = onSendMessage,
                    onStopGenerating = onStopGenerating,
                    isGenerating = isStreaming,
                    highlighted = messagesEmpty,
                    accessoryContent = {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                ModelSelector(
                                    currentModel = selectedModel,
                                    models = models,
                                    onModelSelected = onModelSelected,
                                    compact = true,
                                )
                                SkillToggleBar(
                                    isThinkingMode = isThinkingMode,
                                    activeSkill = activeSkill,
                                    isGenerating = isStreaming,
                                    onThinkingModeToggle = onThinkingModeToggle,
                                    onSkillToggle = onSkillToggle,
                                    compact = true,
                                )
                            }
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .hostPageScrollModifiers(true, topAppBarScrollBehavior),
            contentPadding = hostPageContentPadding(innerPadding, outerPadding, extraBottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!errorMessage.isNullOrBlank()) {
                item(key = "chat_error") {
                    ChatErrorCard(
                        message = errorMessage,
                        onDismiss = onDismissError,
                    )
                }
            }
            if (!messagesEmpty) {
                messagesContent()
            }
        }
    }
}

@Composable
private fun ChatErrorCard(
    message: String,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(horizontal = 12.dp),
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.errorContainer,
            contentColor = MiuixTheme.colorScheme.onErrorContainer,
        ),
    ) {
        BasicComponent(
            title = "请求失败",
            summary = message,
            endActions = {
                Text(
                    text = "关闭",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable(onClick = onDismiss),
                )
            },
        )
    }
}

@Composable
private fun FeaturedHistoryCard(
    chat: Chat,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(horizontal = 12.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            top.yukonga.miuix.kmp.basic.Icon(
                imageVector = MiuixIcons.Messages,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = chat.title,
                    style = MiuixTheme.textStyles.title3,
                    color = MiuixTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "上次更新于 ${formatArchiveTime(chat.updatedAt)}",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            Icon(
                imageVector = MiuixIcons.Basic.ArrowRight,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun HistoryCard(
    chat: Chat,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = onClick,
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = if (selected) MiuixTheme.colorScheme.surfaceContainerHighest else MiuixTheme.colorScheme.surfaceContainer,
            contentColor = MiuixTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = chat.title,
                    style = MiuixTheme.textStyles.body1,
                    color = if (selected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                )
                if (selected) {
                    top.yukonga.miuix.kmp.basic.Icon(
                        imageVector = MiuixIcons.Messages,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatArchiveTime(chat.updatedAt),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

private fun formatArchiveTime(timestamp: Long): String {
    return SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

@Composable
private fun SearchDefaultArchiveResult() {
    Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
        BasicComponent(
            title = "搜索聊天内容",
            summary = "输入关键词后，可直接定位历史对话和消息片段。",
        )
    }
}

@Composable
private fun ArchiveSearchResultCard(
    result: SearchResult,
    onClick: () -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = result.chat.title,
                style = MiuixTheme.textStyles.title3,
                color = MiuixTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = dateFormat.format(Date(result.message.timestamp)),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            top.yukonga.miuix.kmp.basic.Surface(
                color = MiuixTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = result.highlightedContent,
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                )
            }
        }
    }
}
