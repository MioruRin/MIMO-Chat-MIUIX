package com.mroldl001.mimochat.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mroldl001.mimochat.ui.search.SearchScreen
import com.mroldl001.mimochat.ui.settings.SettingsScreen
import com.mroldl001.mimochat.ui.theme.MiuixAppearanceSettings
import com.mroldl001.mimochat.ui.theme.ThemeColor
import com.mroldl001.mimochat.ui.theme.ThemeMode
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Messages
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class HostTab {
    Chat,
    Settings,
}

@Composable
fun MiuixAppHost(
    appearanceSettings: MiuixAppearanceSettings = MiuixAppearanceSettings(),
    onThemeChanged: (ThemeColor, ThemeMode) -> Unit = { _, _ -> },
    onAppearanceSettingsChanged: (MiuixAppearanceSettings) -> Unit = {},
    onBackToChat: ((() -> Unit) -> Unit)? = null,
    initialChatId: Long? = null,
) {
    val pagerState = rememberPagerState(pageCount = { HostTab.entries.size })
    val hostPagerState = rememberHostPagerState(pagerState)
    val backdrop = rememberHostBackdrop()
    var selectedChatId by remember { mutableStateOf<Long?>(initialChatId) }
    var isChatDetailMode by remember { mutableStateOf(false) }

    fun navigateToTab(index: Int) {
        if (index == HostTab.Chat.ordinal) {
            selectedChatId = null
            isChatDetailMode = false
        }
        hostPagerState.animateToPage(index)
    }

    LaunchedEffect(hostPagerState.pagerState.currentPage) {
        hostPagerState.syncPage()
    }

    LaunchedEffect(Unit) {
        onBackToChat?.invoke {
            hostPagerState.animateToPage(HostTab.Chat.ordinal)
        }
    }

    BackHandler(enabled = hostPagerState.selectedPage != HostTab.Chat.ordinal) {
        navigateToTab(HostTab.Chat.ordinal)
    }

    val navigationItems = remember {
        listOf(
            HostNavigationItem(label = "对话", icon = MiuixIcons.Messages),
            HostNavigationItem(label = "设置", icon = MiuixIcons.Settings),
        )
    }

    Scaffold(
        containerColor = MiuixTheme.colorScheme.background,
        bottomBar = {
            if (!(hostPagerState.selectedPage == HostTab.Chat.ordinal && isChatDetailMode)) {
                HostBottomNavigationBar(
                    navigationItems = navigationItems,
                    pagerState = hostPagerState,
                    backdrop = backdrop,
                    onItemClick = { index -> navigateToTab(index) },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = androidx.compose.ui.Alignment.Top,
            ) { page ->
                when (HostTab.entries[page]) {
                    HostTab.Chat -> MiuixChatPage(
                        outerPadding = innerPadding,
                        selectedChatId = selectedChatId,
                        onDetailModeChanged = { isChatDetailMode = it },
                        onNavigateToSettings = { navigateToTab(HostTab.Settings.ordinal) },
                        onOpenChat = { chatId ->
                            selectedChatId = chatId
                            hostPagerState.animateToPage(HostTab.Chat.ordinal)
                        },
                    )

                    HostTab.Settings -> SettingsScreen(
                        outerPadding = innerPadding,
                        onNavigateBack = { navigateToTab(HostTab.Chat.ordinal) },
                        onThemeChanged = onThemeChanged,
                        appearanceSettings = appearanceSettings,
                        onAppearanceSettingsChanged = onAppearanceSettingsChanged,
                    )
                }
            }
        }
    }
}
