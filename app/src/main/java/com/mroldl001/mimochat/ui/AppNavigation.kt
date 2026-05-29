package com.mroldl001.mimochat.ui

import androidx.compose.runtime.Composable
import com.mroldl001.mimochat.ui.theme.MiuixAppearanceSettings
import com.mroldl001.mimochat.ui.theme.ThemeColor
import com.mroldl001.mimochat.ui.theme.ThemeMode

@Composable
fun AppNavigation(
    isExpandedScreen: Boolean = false,
    appearanceSettings: MiuixAppearanceSettings = MiuixAppearanceSettings(),
    onThemeChanged: (ThemeColor, ThemeMode) -> Unit = { _, _ -> },
    onAppearanceSettingsChanged: (MiuixAppearanceSettings) -> Unit = {},
    onNavigateFromDrawer: (Boolean) -> Unit = {},
    onBackToChat: ((() -> Unit) -> Unit)? = null
) {
    MiuixAppHost(
        appearanceSettings = appearanceSettings,
        onThemeChanged = onThemeChanged,
        onAppearanceSettingsChanged = onAppearanceSettingsChanged,
        onBackToChat = onBackToChat
    )
}
