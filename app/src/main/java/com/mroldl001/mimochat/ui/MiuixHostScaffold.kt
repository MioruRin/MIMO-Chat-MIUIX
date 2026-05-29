package com.mroldl001.mimochat.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mroldl001.mimochat.LocalMiuixAppearanceSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDisplayMode
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import kotlin.math.abs

data class HostNavigationItem(
    val label: String,
    val icon: ImageVector,
)

fun Modifier.hostPageScrollModifiers(
    enableScrollEndHaptic: Boolean,
    topAppBarScrollBehavior: ScrollBehavior,
): Modifier = this
    .then(if (enableScrollEndHaptic) Modifier.scrollEndHaptic() else Modifier)
    .overScrollVertical()
    .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
    .fillMaxHeight()

@Composable
fun hostPageContentPadding(
    innerPadding: PaddingValues,
    outerPadding: PaddingValues,
    extraTop: Dp = 0.dp,
    extraStart: Dp = 0.dp,
    extraEnd: Dp = 0.dp,
    extraBottom: Dp = 0.dp,
): PaddingValues {
    val topPadding = innerPadding.calculateTopPadding() + extraTop
    val bottomPadding =
        outerPadding.calculateBottomPadding() +
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding() +
            extraBottom

    return remember(topPadding, bottomPadding, extraStart, extraEnd) {
        PaddingValues(
            top = topPadding,
            start = extraStart,
            end = extraEnd,
            bottom = bottomPadding,
        )
    }
}

@Composable
fun rememberHostBackdrop(): LayerBackdrop? {
    val appearanceSettings = LocalMiuixAppearanceSettings.current
    if (!appearanceSettings.blurEnabled || !isRuntimeShaderSupported()) return null

    val surfaceColor = MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}

@Composable
fun HostBlurredBar(
    backdrop: LayerBackdrop?,
    content: @Composable () -> Unit,
) {
    val appearanceSettings = LocalMiuixAppearanceSettings.current
    Box(
        modifier = if (appearanceSettings.blurEnabled && backdrop != null) {
            Modifier.textureBlur(
                backdrop = backdrop,
                shape = RectangleShape,
                blurRadius = 25f,
                colors = BlurDefaults.blurColors(
                    blendColors = listOf(
                        BlendColorEntry(color = MiuixTheme.colorScheme.surface.copy(0.8f)),
                    ),
                ),
            )
        } else {
            Modifier
        },
    ) {
        content()
    }
}

@Composable
fun HostHeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
fun HostAdaptiveTopAppBar(
    title: String,
    scrollBehavior: ScrollBehavior,
    subtitle: String = "",
    largeTitle: String = title,
    color: Color = MiuixTheme.colorScheme.surface,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    bottomContent: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = title,
        largeTitle = largeTitle,
        subtitle = subtitle,
        color = color,
        scrollBehavior = scrollBehavior,
        navigationIcon = navigationIcon,
        actions = actions,
        bottomContent = bottomContent,
    )
}

@Composable
fun HostBottomNavigationBar(
    navigationItems: List<HostNavigationItem>,
    pagerState: HostPagerState,
    backdrop: LayerBackdrop?,
    onItemClick: (Int) -> Unit = { pagerState.animateToPage(it) },
    modifier: Modifier = Modifier,
) {
    val appearanceSettings = LocalMiuixAppearanceSettings.current
    val blurActive = appearanceSettings.blurEnabled && backdrop != null
    val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        if (appearanceSettings.useFloatingBottomBar) {
            val floatingBarShape = RoundedCornerShape(28.dp)
            FloatingNavigationBar(
                modifier = modifier.then(
                    if (blurActive) {
                        Modifier.textureBlur(
                            backdrop = backdrop,
                            shape = floatingBarShape,
                            blurRadius = 25f,
                            colors = BlurDefaults.blurColors(
                                blendColors = listOf(
                                    BlendColorEntry(color = MiuixTheme.colorScheme.surfaceContainer.copy(0.6f)),
                                ),
                            ),
                        )
                    } else {
                        Modifier
                    }
                ),
                color = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surfaceContainer,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                navigationItems.forEachIndexed { index, item ->
                    FloatingNavigationBarItem(
                        selected = pagerState.selectedPage == index,
                        onClick = { onItemClick(index) },
                        icon = item.icon,
                        label = item.label,
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .then(
                        if (blurActive) {
                            Modifier.textureBlur(
                                backdrop = backdrop,
                                shape = RectangleShape,
                                blurRadius = 25f,
                                colors = BlurDefaults.blurColors(
                                    blendColors = listOf(
                                        BlendColorEntry(color = MiuixTheme.colorScheme.surface.copy(0.8f)),
                                    ),
                                ),
                            )
                        } else {
                            Modifier
                        }
                    )
                    .background(barColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    )
                    .then(modifier),
            ) {
                NavigationBar(
                    color = barColor,
                    mode = NavigationBarDisplayMode.IconWithSelectedLabel,
                ) {
                    navigationItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = pagerState.selectedPage == index,
                            onClick = { onItemClick(index) },
                            icon = item.icon,
                            label = item.label,
                        )
                    }
                }
            }
        }
    }
}

@Stable
class HostPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set

    private var navJob: Job? = null

    fun animateToPage(targetIndex: Int) {
        if (targetIndex == selectedPage) return

        navJob?.cancel()
        selectedPage = targetIndex
        isNavigating = true

        navJob = coroutineScope.launch {
            val myJob = coroutineContext.job
            try {
                pagerState.scroll(MutatePriority.UserInput) {
                    val distance = abs(targetIndex - pagerState.currentPage).coerceAtLeast(2)
                    val duration = 100 * distance + 100
                    val layoutInfo = pagerState.layoutInfo
                    val pageSize = layoutInfo.pageSize + layoutInfo.pageSpacing
                    val currentDistanceInPages =
                        targetIndex - pagerState.currentPage - pagerState.currentPageOffsetFraction
                    val scrollPixels = currentDistanceInPages * pageSize

                    var previousValue = 0f
                    animate(
                        initialValue = 0f,
                        targetValue = scrollPixels,
                        animationSpec = tween(easing = EaseInOut, durationMillis = duration),
                    ) { currentValue, _ ->
                        previousValue += scrollBy(currentValue - previousValue)
                    }
                }

                if (pagerState.currentPage != targetIndex) {
                    pagerState.scrollToPage(targetIndex)
                }
            } finally {
                if (navJob == myJob) {
                    isNavigating = false
                    if (pagerState.currentPage != targetIndex) {
                        selectedPage = pagerState.currentPage
                    }
                }
            }
        }
    }

    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }
}

@Composable
fun rememberHostPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): HostPagerState = remember(pagerState, coroutineScope) {
    HostPagerState(pagerState, coroutineScope)
}
