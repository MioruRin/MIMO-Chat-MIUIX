package com.mroldl001.mimochat.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.offset
import androidx.compose.ui.zIndex
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.basic.SearchCleanup
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Stable
data class SearchStatus(
    val label: String,
    val searchText: String = "",
    val current: Status = Status.COLLAPSED,
    val resultStatus: ResultStatus = ResultStatus.DEFAULT,
) {
    fun isExpand() = current == Status.EXPANDED
    fun isCollapsed() = current == Status.COLLAPSED
    fun shouldExpand() = current == Status.EXPANDED || current == Status.EXPANDING
    fun shouldCollapsed() = current == Status.COLLAPSED || current == Status.COLLAPSING
    fun isAnimatingExpand() = current == Status.EXPANDING

    fun onAnimationComplete(): SearchStatus = when (current) {
        Status.EXPANDING -> copy(current = Status.EXPANDED)
        Status.COLLAPSING -> copy(searchText = "", current = Status.COLLAPSED)
        else -> this
    }

    @Composable
    fun TopAppBarAnim(
        modifier: Modifier = Modifier,
        visible: Boolean = shouldCollapsed(),
        backgroundColor: Color = MiuixTheme.colorScheme.surface,
        content: @Composable () -> Unit,
    ) {
        Box(modifier = modifier) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(backgroundColor),
            )
            Box(
                modifier = Modifier
                    .graphicsLayer { this.alpha = if (visible) 1f else 0f },
            ) { content() }
        }
    }

    enum class Status { EXPANDED, EXPANDING, COLLAPSED, COLLAPSING }
    enum class ResultStatus { DEFAULT, EMPTY, LOAD, SHOW }
}

@Composable
fun SearchStatus.SearchPager(
    onSearchStatusChange: (SearchStatus) -> Unit,
    offsetY: Dp,
    defaultResult: @Composable () -> Unit,
    expandBar: @Composable (SearchStatus, (SearchStatus) -> Unit) -> Unit = { searchStatus, onStatusChange ->
        SearchBar(searchStatus, onStatusChange)
    },
    result: LazyListScope.() -> Unit,
) {
    val searchStatus = this
    val onSearchStatusChangeUpdated = rememberUpdatedState(onSearchStatusChange)
    val searchStatusUpdated = rememberUpdatedState(searchStatus)
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val topPadding by animateDpAsState(
        targetValue = if (searchStatus.shouldExpand()) {
            systemBarsPadding + 12.dp
        } else {
            max(offsetY, 0.dp)
        },
        animationSpec = tween(300, easing = LinearOutSlowInEasing),
        label = "SearchPagerTopPadding",
    ) {
        onSearchStatusChange(searchStatus.onAnimationComplete())
    }
    val surfaceAlpha by animateFloatAsState(
        if (searchStatus.shouldExpand()) 1f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "SearchPagerSurfaceAlpha",
    )

    val surfaceColor = MiuixTheme.colorScheme.surface
    Column(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(5f)
            .drawBehind { drawRect(surfaceColor.copy(alpha = surfaceAlpha)) }
            .semantics { onClick { false } }
            .then(
                if (!searchStatus.isCollapsed()) Modifier else Modifier,
            ),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val topPaddingPx = topPadding.roundToPx()
                    val placeable = measurable.measure(constraints.offset(vertical = -topPaddingPx))
                    layout(placeable.width, placeable.height + topPaddingPx) {
                        placeable.placeRelative(0, topPaddingPx)
                    }
                }
                .then(
                    if (!searchStatus.isCollapsed()) {
                        Modifier.background(MiuixTheme.colorScheme.surface)
                    } else {
                        Modifier
                    },
                ),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!searchStatus.isCollapsed()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(MiuixTheme.colorScheme.surface),
                ) {
                    expandBar(searchStatus, onSearchStatusChange)
                }
            }
            AnimatedVisibility(
                visible = searchStatus.isExpand() || searchStatus.isAnimatingExpand(),
                enter = expandHorizontally() + slideInHorizontally(initialOffsetX = { it }),
                exit = shrinkHorizontally() + slideOutHorizontally(targetOffsetX = { it }),
            ) {
                Text(
                    text = "取消",
                    fontWeight = FontWeight.Bold,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 6.dp)
                        .clickable(
                            interactionSource = null,
                            enabled = searchStatus.isExpand(),
                            indication = null,
                            onClick = {
                                onSearchStatusChangeUpdated.value(
                                    searchStatusUpdated.value.copy(
                                        searchText = "",
                                        current = SearchStatus.Status.COLLAPSING,
                                    ),
                                )
                            },
                        ),
                )
            }
        }
        AnimatedVisibility(
            visible = searchStatus.isExpand(),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            when (searchStatus.resultStatus) {
                SearchStatus.ResultStatus.DEFAULT -> defaultResult()
                SearchStatus.ResultStatus.EMPTY -> {}
                SearchStatus.ResultStatus.LOAD -> {}
                SearchStatus.ResultStatus.SHOW -> LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .overScrollVertical(),
                ) {
                    result()
                }
            }
        }
    }

    BackHandler(enabled = searchStatus.shouldExpand()) {
        onSearchStatusChange(
            searchStatus.copy(
                searchText = "",
                current = SearchStatus.Status.COLLAPSING,
            ),
        )
    }
}

@Composable
fun SearchBar(
    searchStatus: SearchStatus,
    onSearchStatusChange: (SearchStatus) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val onSearchStatusChangeUpdated = rememberUpdatedState(onSearchStatusChange)
    val searchStatusUpdated = rememberUpdatedState(searchStatus)

    InputField(
        query = searchStatus.searchText,
        onQueryChange = { onSearchStatusChange(searchStatus.copy(searchText = it)) },
        label = searchStatus.label,
        leadingIcon = {
            Icon(
                imageVector = MiuixIcons.Basic.Search,
                contentDescription = "search",
                modifier = Modifier
                    .size(44.dp)
                    .padding(start = 16.dp, end = 8.dp),
                tint = MiuixTheme.colorScheme.onSurfaceContainerHigh,
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                searchStatus.searchText.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                Icon(
                    imageVector = MiuixIcons.Basic.SearchCleanup,
                    tint = MiuixTheme.colorScheme.onSurface,
                    contentDescription = "clear",
                    modifier = Modifier
                        .size(44.dp)
                        .padding(start = 8.dp, end = 16.dp)
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            onClick = {
                                onSearchStatusChangeUpdated.value(
                                    searchStatusUpdated.value.copy(searchText = ""),
                                )
                            },
                        ),
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 6.dp)
            .focusRequester(focusRequester),
        onSearch = {},
        expanded = searchStatus.shouldExpand(),
        onExpandedChange = {
            onSearchStatusChange(
                searchStatus.copy(
                    current = if (it) SearchStatus.Status.EXPANDED else SearchStatus.Status.COLLAPSED,
                ),
            )
        },
    )
    LaunchedEffect(Unit) {
        if (!expanded && searchStatus.shouldExpand()) {
            focusRequester.requestFocus()
            expanded = true
        }
    }
}

@Composable
fun SearchBarFake(
    label: String,
    searchBarTopPadding: Dp = 12.dp,
) {
    InputField(
        query = "",
        onQueryChange = { },
        label = label,
        leadingIcon = {
            Icon(
                imageVector = MiuixIcons.Basic.Search,
                contentDescription = "Search",
                modifier = Modifier
                    .size(44.dp)
                    .padding(start = 16.dp, end = 8.dp),
                tint = MiuixTheme.colorScheme.onSurfaceContainerHigh,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = searchBarTopPadding, bottom = 6.dp),
        onSearch = { },
        enabled = false,
        expanded = false,
        onExpandedChange = { },
    )
}
