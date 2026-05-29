package com.mroldl001.mimochat.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle
import top.yukonga.miuix.kmp.theme.darkColorScheme as miuixDarkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme as miuixLightColorScheme

enum class ThemeColor {
    WHITE,
    HATSUNE_MIKU,
    AUTO_COLOR,
    MI_ORANGE,
    GREEN,
    PURPLE
}

enum class ThemeMode {
    LIGHT,
    DARK,
    FOLLOW_SYSTEM
}

fun supportsDynamicColor(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

private fun colorSeed(themeColor: ThemeColor): Color? = when (themeColor) {
    ThemeColor.WHITE -> WhiteLightPrimary
    ThemeColor.HATSUNE_MIKU -> HatsuneMikuLightPrimary
    ThemeColor.MI_ORANGE -> MiOrangeLightPrimary
    ThemeColor.GREEN -> GreenLightPrimary
    ThemeColor.PURPLE -> PurpleLightPrimary
    ThemeColor.AUTO_COLOR -> null
}

@Composable
private fun isDarkTheme(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
}

private fun mapToMiuixMode(themeMode: ThemeMode, themeColor: ThemeColor): ColorSchemeMode {
    return when {
        themeColor == ThemeColor.AUTO_COLOR && themeMode == ThemeMode.FOLLOW_SYSTEM -> ColorSchemeMode.MonetSystem
        themeColor == ThemeColor.AUTO_COLOR && themeMode == ThemeMode.LIGHT -> ColorSchemeMode.MonetLight
        themeColor == ThemeColor.AUTO_COLOR && themeMode == ThemeMode.DARK -> ColorSchemeMode.MonetDark
        themeMode == ThemeMode.LIGHT -> ColorSchemeMode.Light
        themeMode == ThemeMode.DARK -> ColorSchemeMode.Dark
        else -> ColorSchemeMode.System
    }
}

private fun darkPrimary(themeColor: ThemeColor): Color = when (themeColor) {
    ThemeColor.WHITE -> WhiteDarkPrimary
    ThemeColor.HATSUNE_MIKU -> HatsuneMikuDarkPrimary
    ThemeColor.MI_ORANGE -> MiOrangeDarkPrimary
    ThemeColor.GREEN -> GreenDarkPrimary
    ThemeColor.PURPLE -> PurpleDarkPrimary
    ThemeColor.AUTO_COLOR -> MiOrangeDarkPrimary
}

private fun applySystemBars(view: android.view.View, background: Color, darkTheme: Boolean) {
    val activity = view.context as? Activity ?: return
    val window = activity.window
    window.statusBarColor = background.toArgb()
    window.navigationBarColor = background.toArgb()

    val insetsController = WindowCompat.getInsetsController(window, view)
    insetsController.isAppearanceLightStatusBars = !darkTheme
    insetsController.isAppearanceLightNavigationBars = !darkTheme
}

@Composable
fun MIMOChatTheme(
    themeColor: ThemeColor = ThemeColor.AUTO_COLOR,
    themeMode: ThemeMode = ThemeMode.FOLLOW_SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = isDarkTheme(themeMode)
    val view = LocalView.current
    val controller = remember(themeColor, themeMode, darkTheme) {
        ThemeController(
            colorSchemeMode = mapToMiuixMode(themeMode, themeColor),
            lightColors = miuixLightColorScheme(
                primary = colorSeed(themeColor) ?: MiOrangeLightPrimary
            ),
            darkColors = miuixDarkColorScheme(
                primary = darkPrimary(themeColor)
            ),
            keyColor = colorSeed(themeColor),
            paletteStyle = ThemePaletteStyle.TonalSpot,
            colorSpec = ThemeColorSpec.Spec2025,
            isDark = darkTheme
        )
    }

    MiuixTheme(controller = controller) {
        val background = MiuixTheme.colorScheme.background
        if (!view.isInEditMode) {
            LaunchedEffect(background, darkTheme) {
                applySystemBars(view, background, darkTheme)
            }
        }
        content()
    }
}
