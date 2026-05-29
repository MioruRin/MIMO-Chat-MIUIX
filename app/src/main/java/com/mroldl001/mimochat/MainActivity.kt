package com.mroldl001.mimochat

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.mroldl001.mimochat.data.preferences.PreferencesManager
import com.mroldl001.mimochat.ui.AppNavigation
import com.mroldl001.mimochat.ui.theme.MiuixAppearanceSettings
import com.mroldl001.mimochat.ui.theme.MIMOChatTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ -> }

        requestNotificationPermissionIfNeeded()

        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)

        val widthDp = displayMetrics.widthPixels / displayMetrics.density
        val isPhone = widthDp < 600

        requestedOrientation = if (isPhone) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        }

        setContent {
            MainContent(isExpandedScreen = !isPhone)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (preferencesManager.hasRequestedNotificationPermission()) return

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        preferencesManager.setNotificationPermissionRequested(true)
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val preferencesManager: PreferencesManager by lazy {
        (application as MIMOChatApp).preferencesManager
    }
}

val LocalMiuixAppearanceSettings = staticCompositionLocalOf { MiuixAppearanceSettings() }

@Composable
private fun MainContent(
    viewModel: MainViewModel = hiltViewModel(),
    isExpandedScreen: Boolean
) {
    var themeColor by remember { mutableStateOf(viewModel.preferencesManager.getThemeColor()) }
    var themeMode by remember { mutableStateOf(viewModel.preferencesManager.getThemeMode()) }
    var appearanceSettings by remember { mutableStateOf(viewModel.preferencesManager.getMiuixAppearanceSettings()) }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var onBackToChat: (() -> Unit)? by remember { mutableStateOf(null) }

    BackHandler(enabled = isDrawerOpen) {
        isDrawerOpen = false
        onBackToChat?.invoke()
    }

    MIMOChatTheme(
        themeColor = themeColor,
        themeMode = themeMode
    ) {
        val baseDensity = LocalDensity.current
        val scaledDensity = remember(baseDensity, appearanceSettings.uiScale) {
            Density(
                density = baseDensity.density * appearanceSettings.uiScale,
                fontScale = baseDensity.fontScale * appearanceSettings.uiScale
            )
        }

        CompositionLocalProvider(
            LocalDensity provides scaledDensity,
            LocalMiuixAppearanceSettings provides appearanceSettings
        ) {
            AppNavigation(
                isExpandedScreen = isExpandedScreen,
                appearanceSettings = appearanceSettings,
                onThemeChanged = { newColor, newMode ->
                    themeColor = newColor
                    themeMode = newMode
                },
                onAppearanceSettingsChanged = { appearanceSettings = it },
                onNavigateFromDrawer = { isDrawerOpen = it },
                onBackToChat = { onBackToChat = it }
            )
        }
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    val preferencesManager: PreferencesManager
) : ViewModel()
