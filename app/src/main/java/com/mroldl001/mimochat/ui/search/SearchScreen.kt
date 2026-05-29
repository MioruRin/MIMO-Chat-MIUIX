package com.mroldl001.mimochat.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mroldl001.mimochat.domain.model.SearchResult
import com.mroldl001.mimochat.ui.HostAdaptiveTopAppBar
import com.mroldl001.mimochat.ui.HostBlurredBar
import com.mroldl001.mimochat.ui.HostHeaderIconButton
import com.mroldl001.mimochat.ui.hostPageContentPadding
import com.mroldl001.mimochat.ui.hostPageScrollModifiers
import com.mroldl001.mimochat.ui.rememberHostBackdrop
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SearchScreen(
    outerPadding: PaddingValues,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (Long) -> Unit,
    onNavigateFromSearch: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val searchQuery by viewModel.searchQuery
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching
    val hasSearched by viewModel.hasSearched
    val topAppBarScrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberHostBackdrop()
    val barColor = if (backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface
    val density = LocalDensity.current
    val lazyListState = rememberLazyListState()
    val dynamicTopPadding by remember(topAppBarScrollBehavior) {
        derivedStateOf { 12.dp * (1f - topAppBarScrollBehavior.state.collapsedFraction) }
    }

    var searchStatus by remember { mutableStateOf(SearchStatus(label = "搜索会话、消息与上下文")) }
    var searchOffsetY by remember { mutableStateOf(0.dp) }

    DisposableEffect(Unit) {
        onDispose { onNavigateFromSearch() }
    }

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

    Scaffold(
        topBar = {
            HostBlurredBar(backdrop) {
                searchStatus.TopAppBarAnim(backgroundColor = barColor) {
                    HostAdaptiveTopAppBar(
                        title = "搜索",
                        largeTitle = "搜索",
                        subtitle = "按关键词检索历史会话、标题和消息片段",
                        scrollBehavior = topAppBarScrollBehavior,
                        color = barColor,
                        navigationIcon = {
                            HostHeaderIconButton(
                                icon = MiuixIcons.Search,
                                contentDescription = "返回对话",
                                onClick = onNavigateBack,
                            )
                        },
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
                                            Modifier.pointerInput(Unit) {
                                                detectTapGestures {
                                                    searchStatus = searchStatus.copy(current = SearchStatus.Status.EXPANDING)
                                                }
                                            }
                                        } else {
                                            Modifier
                                        },
                                    ),
                            ) {
                                SearchBarFake(
                                    label = searchStatus.label,
                                    searchBarTopPadding = dynamicTopPadding,
                                )
                            }
                        },
                    )
                }
            }
        },
        popupHost = {
            searchStatus.SearchPager(
                onSearchStatusChange = { next ->
                    searchStatus = next
                    when {
                        next.searchText != searchQuery -> viewModel.updateQuery(next.searchText)
                        next.current == SearchStatus.Status.COLLAPSING -> viewModel.clearSearch()
                    }
                },
                offsetY = searchOffsetY,
                defaultResult = {
                    SearchDefaultLayer(outerPadding = outerPadding)
                },
            ) {
                items(searchResults, key = { "${it.chat.id}-${it.message.id}" }) { result ->
                    SearchResultCard(
                        result = result,
                        onClick = { onNavigateToChat(result.chat.id) },
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(outerPadding.calculateBottomPadding() + 20.dp))
                }
            }
        },
    ) { innerPadding ->
        val contentPadding = hostPageContentPadding(innerPadding, outerPadding, extraBottom = 12.dp)
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .hostPageScrollModifiers(
                    enableScrollEndHaptic = true,
                    topAppBarScrollBehavior = topAppBarScrollBehavior,
                ),
            contentPadding = contentPadding,
        ) {
            item {
                SmallTitle("快速入口")
                Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                    BasicComponent(
                        title = "按会话标题搜索",
                        summary = "适合查找你记得主题或标题的大致会话",
                        onClick = { searchStatus = searchStatus.copy(current = SearchStatus.Status.EXPANDING) },
                    )
                    BasicComponent(
                        title = "按消息内容搜索",
                        summary = "检索用户提问、助手回答与上下文片段",
                        onClick = { searchStatus = searchStatus.copy(current = SearchStatus.Status.EXPANDING) },
                    )
                }
            }
            item {
                SmallTitle("搜索说明")
                Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                    BasicComponent(
                        title = "搜索结果会回到原对话",
                        summary = "点按任意结果会直接打开对应会话",
                    )
                    BasicComponent(
                        title = "建议使用更短的关键词",
                        summary = "Miuix 风格搜索更强调直接、快速定位",
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SearchDefaultLayer(
    outerPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 82.dp,
            start = 12.dp,
            end = 12.dp,
            bottom = outerPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card {
                BasicComponent(
                    title = "最近搜索",
                    summary = "输入关键词后，可在此直接查看命中的历史消息",
                )
                BasicComponent(
                    title = "搜索建议",
                    summary = "例如：接口、模型、系统提示词、联网、代码、公式",
                )
            }
        }
    }
}

@Composable
private fun SearchResultCard(
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = dateFormat.format(Date(result.message.timestamp)),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Surface(
                color = MiuixTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = result.highlightedContent,
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
