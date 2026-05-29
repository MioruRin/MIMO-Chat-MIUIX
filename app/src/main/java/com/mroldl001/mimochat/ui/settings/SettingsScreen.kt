package com.mroldl001.mimochat.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.mroldl001.mimochat.data.preferences.PreferencesManager
import com.mroldl001.mimochat.ui.HostAdaptiveTopAppBar
import com.mroldl001.mimochat.ui.HostBlurredBar
import com.mroldl001.mimochat.ui.HostHeaderIconButton
import com.mroldl001.mimochat.ui.hostPageContentPadding
import com.mroldl001.mimochat.ui.hostPageScrollModifiers
import com.mroldl001.mimochat.ui.rememberHostBackdrop
import com.mroldl001.mimochat.ui.theme.MiuixAppearanceSettings
import com.mroldl001.mimochat.ui.theme.ThemeColor
import com.mroldl001.mimochat.ui.theme.ThemeMode
import com.mroldl001.mimochat.ui.theme.supportsDynamicColor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.CardDefaults as MiuixCardDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SliderDefaults
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

private enum class SettingsDestination {
    Home,
    Theme
}

data class SettingsUiState(
    val themeColor: ThemeColor = ThemeColor.WHITE,
    val themeMode: ThemeMode = ThemeMode.FOLLOW_SYSTEM,
    val apiKey: String = "",
    val apiBaseUrl: String = PreferencesManager.DEFAULT_API_BASE_URL,
    val customSystemPrompt: String = "",
    val temperature: Float = PreferencesManager.DEFAULT_TEMPERATURE,
    val topP: Float = PreferencesManager.DEFAULT_TOP_P,
    val frequencyPenalty: Float = PreferencesManager.DEFAULT_FREQUENCY_PENALTY,
    val presencePenalty: Float = PreferencesManager.DEFAULT_PRESENCE_PENALTY,
    val appearanceSettings: MiuixAppearanceSettings = MiuixAppearanceSettings()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    var uiState by mutableStateOf(
        SettingsUiState(
            themeColor = preferencesManager.getThemeColor(),
            themeMode = preferencesManager.getThemeMode(),
            apiKey = preferencesManager.getApiKey(),
            apiBaseUrl = preferencesManager.getApiBaseUrl(),
            customSystemPrompt = preferencesManager.getCustomSystemPrompt(),
            temperature = preferencesManager.getTemperature(),
            topP = preferencesManager.getTopP(),
            frequencyPenalty = preferencesManager.getFrequencyPenalty(),
            presencePenalty = preferencesManager.getPresencePenalty(),
            appearanceSettings = preferencesManager.getMiuixAppearanceSettings()
        )
    )
        private set

    fun setThemeColor(value: ThemeColor) {
        uiState = uiState.copy(themeColor = value)
        preferencesManager.saveThemeColor(value)
    }

    fun setThemeMode(value: ThemeMode) {
        uiState = uiState.copy(themeMode = value)
        preferencesManager.saveThemeMode(value)
    }

    fun setBlurEnabled(value: Boolean) {
        uiState = uiState.copy(appearanceSettings = uiState.appearanceSettings.copy(blurEnabled = value))
        preferencesManager.setBlurEnabled(value)
    }

    fun setFloatingBottomBar(value: Boolean) {
        uiState = uiState.copy(appearanceSettings = uiState.appearanceSettings.copy(useFloatingBottomBar = value))
        preferencesManager.setUseFloatingBottomBar(value)
    }

    fun setSmoothCorners(value: Boolean) {
        uiState = uiState.copy(appearanceSettings = uiState.appearanceSettings.copy(smoothCorners = value))
        preferencesManager.setSmoothCorners(value)
    }

    fun setPredictiveBackEnabled(value: Boolean) {
        uiState = uiState.copy(appearanceSettings = uiState.appearanceSettings.copy(predictiveBackEnabled = value))
        preferencesManager.setPredictiveBackEnabled(value)
    }

    fun setUiScale(value: Float) {
        val scaled = value.coerceIn(0.85f, 1.15f)
        uiState = uiState.copy(appearanceSettings = uiState.appearanceSettings.copy(uiScale = scaled))
        preferencesManager.setUiScale(scaled)
    }

    fun setApiKey(value: String) {
        uiState = uiState.copy(apiKey = value)
        preferencesManager.saveApiKey(value)
    }

    fun setApiBaseUrl(value: String) {
        uiState = uiState.copy(apiBaseUrl = value)
        preferencesManager.saveApiBaseUrl(value)
    }

    fun setCustomSystemPrompt(value: String) {
        uiState = uiState.copy(customSystemPrompt = value)
        preferencesManager.saveCustomSystemPrompt(value)
    }

    fun setParameters(
        temperature: Float,
        topP: Float,
        frequencyPenalty: Float,
        presencePenalty: Float
    ) {
        uiState = uiState.copy(
            temperature = temperature,
            topP = topP,
            frequencyPenalty = frequencyPenalty,
            presencePenalty = presencePenalty
        )
        preferencesManager.saveTemperature(temperature)
        preferencesManager.saveTopP(topP)
        preferencesManager.saveFrequencyPenalty(frequencyPenalty)
        preferencesManager.savePresencePenalty(presencePenalty)
    }

    fun resetParameters() {
        preferencesManager.resetParameters()
        uiState = uiState.copy(
            temperature = PreferencesManager.DEFAULT_TEMPERATURE,
            topP = PreferencesManager.DEFAULT_TOP_P,
            frequencyPenalty = PreferencesManager.DEFAULT_FREQUENCY_PENALTY,
            presencePenalty = PreferencesManager.DEFAULT_PRESENCE_PENALTY
        )
    }
}

@Composable
fun SettingsScreen(
    outerPadding: PaddingValues,
    onNavigateBack: () -> Unit,
    onThemeChanged: (ThemeColor, ThemeMode) -> Unit,
    appearanceSettings: MiuixAppearanceSettings = MiuixAppearanceSettings(),
    onAppearanceSettingsChanged: (MiuixAppearanceSettings) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    val backdrop = rememberHostBackdrop()
    val topAppBarScrollBehavior = MiuixScrollBehavior()
    var currentDestination by remember { mutableStateOf(SettingsDestination.Home) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showApiBaseUrlDialog by remember { mutableStateOf(false) }
    var showPromptDialog by remember { mutableStateOf(false) }
    var showParamsDialog by remember { mutableStateOf(false) }

    val pageAppearance = state.appearanceSettings

    BackHandler(enabled = currentDestination == SettingsDestination.Theme) {
        currentDestination = SettingsDestination.Home
    }

    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            HostBlurredBar(backdrop) {
                HostAdaptiveTopAppBar(
                    title = if (currentDestination == SettingsDestination.Home) "设置" else "主题设置",
                    largeTitle = if (currentDestination == SettingsDestination.Home) "设置" else "主题设置",
                    subtitle = if (currentDestination == SettingsDestination.Home) {
                        "主题、服务接入与模型参数"
                    } else {
                        "显示模式、主色与界面效果"
                    },
                    scrollBehavior = topAppBarScrollBehavior,
                    color = if (backdrop != null) androidx.compose.ui.graphics.Color.Transparent else MiuixTheme.colorScheme.surface,
                    navigationIcon = {
                        if (currentDestination == SettingsDestination.Theme) {
                            HostHeaderIconButton(
                                icon = MiuixIcons.Back,
                                contentDescription = "返回设置",
                                onClick = {
                                    currentDestination = SettingsDestination.Home
                                }
                            )
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    if (targetState == SettingsDestination.Theme) {
                        slideInHorizontally(initialOffsetX = { it / 3 }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { -it / 5 }) + fadeOut()
                    } else {
                        slideInHorizontally(initialOffsetX = { -it / 5 }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { it / 3 }) + fadeOut()
                    }
                },
                label = "settings_destination_transition",
            ) { destination ->
                when (destination) {
                    SettingsDestination.Home -> {
                        SettingsHomePage(
                            state = state,
                            appearanceSettings = appearanceSettings,
                            contentPadding = hostPageContentPadding(innerPadding, outerPadding, extraBottom = 12.dp),
                            modifier = Modifier.hostPageScrollModifiers(true, topAppBarScrollBehavior),
                            onOpenThemeSettings = { currentDestination = SettingsDestination.Theme },
                            onOpenApiKey = { showApiKeyDialog = true },
                            onOpenApiBaseUrl = { showApiBaseUrlDialog = true },
                            onOpenPrompt = { showPromptDialog = true },
                            onOpenParameters = { showParamsDialog = true },
                        )
                    }

                    SettingsDestination.Theme -> {
                        ThemeSettingsPage(
                            state = state,
                            appearanceSettings = pageAppearance,
                            contentPadding = hostPageContentPadding(innerPadding, outerPadding, extraBottom = 12.dp),
                            modifier = Modifier.hostPageScrollModifiers(true, topAppBarScrollBehavior),
                            onThemeModeChange = { mode ->
                                viewModel.setThemeMode(mode)
                                onThemeChanged(state.themeColor, mode)
                            },
                            onThemeColorChange = { color ->
                                viewModel.setThemeColor(color)
                                onThemeChanged(color, state.themeMode)
                            },
                            onBlurChange = {
                                viewModel.setBlurEnabled(it)
                                onAppearanceSettingsChanged(pageAppearance.copy(blurEnabled = it))
                            },
                            onFloatingBottomBarChange = {
                                viewModel.setFloatingBottomBar(it)
                                onAppearanceSettingsChanged(pageAppearance.copy(useFloatingBottomBar = it))
                            },
                            onSmoothCornersChange = {
                                viewModel.setSmoothCorners(it)
                                onAppearanceSettingsChanged(pageAppearance.copy(smoothCorners = it))
                            },
                            onPredictiveBackChange = {
                                viewModel.setPredictiveBackEnabled(it)
                                onAppearanceSettingsChanged(pageAppearance.copy(predictiveBackEnabled = it))
                            },
                            onUiScaleChange = {
                                viewModel.setUiScale(it)
                                onAppearanceSettingsChanged(pageAppearance.copy(uiScale = it))
                            }
                        )
                    }
                }
            }
        }

        SettingTextDialog(
            show = showApiKeyDialog,
            title = "API Key",
            value = state.apiKey,
            placeholder = "粘贴你的 API Key",
            isPassword = true,
            onDismiss = { showApiKeyDialog = false },
            onConfirm = {
                viewModel.setApiKey(it)
                showApiKeyDialog = false
            }
        )

        ApiBaseUrlDialog(
            show = showApiBaseUrlDialog,
            value = state.apiBaseUrl,
            onDismiss = { showApiBaseUrlDialog = false },
            onConfirm = {
                viewModel.setApiBaseUrl(it)
                showApiBaseUrlDialog = false
            }
        )

        SettingTextDialog(
            show = showPromptDialog,
            title = "系统提示词",
            value = state.customSystemPrompt,
            placeholder = "添加对助手长期生效的系统说明",
            singleLine = false,
            onDismiss = { showPromptDialog = false },
            onConfirm = {
                viewModel.setCustomSystemPrompt(it)
                showPromptDialog = false
            }
        )

        ParametersDialog(
            show = showParamsDialog,
            state = state,
            onTemperatureChange = { viewModel.setParameters(it, state.topP, state.frequencyPenalty, state.presencePenalty) },
            onTopPChange = { viewModel.setParameters(state.temperature, it, state.frequencyPenalty, state.presencePenalty) },
            onFrequencyPenaltyChange = { viewModel.setParameters(state.temperature, state.topP, it, state.presencePenalty) },
            onPresencePenaltyChange = { viewModel.setParameters(state.temperature, state.topP, state.frequencyPenalty, it) },
            onReset = { viewModel.resetParameters() },
            onDismiss = { showParamsDialog = false },
        )
    }
}

@Composable
private fun SettingsHomePage(
    state: SettingsUiState,
    appearanceSettings: MiuixAppearanceSettings,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onOpenThemeSettings: () -> Unit,
    onOpenApiKey: () -> Unit,
    onOpenApiBaseUrl: () -> Unit,
    onOpenPrompt: () -> Unit,
    onOpenParameters: () -> Unit,
) {
    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            SmallTitle("账号与外观")
        }
        item {
            SettingsEntryGroupCard(modifier = Modifier.padding(bottom = 12.dp)) {
                ArrowPreference(
                    title = "外观",
                    summary = themeSettingsSummary(state, appearanceSettings),
                    onClick = onOpenThemeSettings,
                )
                ArrowPreference(
                    title = "API Key",
                    summary = if (state.apiKey.isBlank()) "尚未配置" else "已配置，可直接发起会话",
                    onClick = onOpenApiKey,
                )
                ArrowPreference(
                    title = "接口地址",
                    summary = state.apiBaseUrl,
                    onClick = onOpenApiBaseUrl,
                )
                ArrowPreference(
                    title = "系统提示词",
                    summary = if (state.customSystemPrompt.isBlank()) "未设置额外系统提示词" else "已自定义系统提示词",
                    onClick = onOpenPrompt,
                )
            }
        }
        item {
            SmallTitle("模型")
        }
        item {
            SettingsEntryGroupCard(modifier = Modifier.padding(bottom = 12.dp)) {
                ArrowPreference(
                    title = "采样与惩罚项",
                    summary = parameterSummary(state),
                    onClick = onOpenParameters,
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ThemeSettingsPage(
    state: SettingsUiState,
    appearanceSettings: MiuixAppearanceSettings,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onThemeModeChange: (ThemeMode) -> Unit,
    onThemeColorChange: (ThemeColor) -> Unit,
    onBlurChange: (Boolean) -> Unit,
    onFloatingBottomBarChange: (Boolean) -> Unit,
    onSmoothCornersChange: (Boolean) -> Unit,
    onPredictiveBackChange: (Boolean) -> Unit,
    onUiScaleChange: (Float) -> Unit
) {
    val themeColorOptions = remember { buildThemeColorOptions(supportsDynamicColor()) }
    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            SmallTitle("显示模式")
        }
        item {
            SettingsEntryGroupCard(modifier = Modifier.padding(bottom = 12.dp)) {
                ThemeModeSliderPreference(
                    themeMode = state.themeMode,
                    onThemeModeChange = onThemeModeChange,
                )
            }
        }
        item {
            SmallTitle("主题色")
        }
        item {
            SettingsEntryGroupCard(modifier = Modifier.padding(bottom = 12.dp)) {
                OverlayDropdownPreference(
                    title = "主色方案",
                    summary = themeColorLabel(state.themeColor),
                    items = themeColorOptions.map { it.label },
                    selectedIndex = themeColorOptions.indexOfFirst { it.value == state.themeColor }.coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        onThemeColorChange(themeColorOptions[index].value)
                    },
                )
            }
        }
        item {
            SmallTitle("界面效果")
        }
        item {
            SettingsEntryGroupCard {
                SwitchPreference(
                    title = "模糊",
                    summary = "用于顶栏、底栏与搜索浮层",
                    checked = appearanceSettings.blurEnabled,
                    onCheckedChange = onBlurChange,
                )
                SwitchPreference(
                    title = "悬浮底栏",
                    summary = "使用 Miuix FloatingNavigationBar",
                    checked = appearanceSettings.useFloatingBottomBar,
                    onCheckedChange = onFloatingBottomBarChange,
                )
                SwitchPreference(
                    title = "平滑圆角",
                    summary = "统一卡片、面板和输入区域轮廓",
                    checked = appearanceSettings.smoothCorners,
                    onCheckedChange = onSmoothCornersChange,
                )
                SwitchPreference(
                    title = "预测性返回手势",
                    summary = "系统返回动画联动",
                    checked = appearanceSettings.predictiveBackEnabled,
                    onCheckedChange = onPredictiveBackChange,
                )
                HostSliderPreference(
                    title = "界面缩放",
                    summary = "调整整套宿主界面的显示比例",
                    value = appearanceSettings.uiScale * 100f,
                    valueText = "${(appearanceSettings.uiScale * 100).toInt()}%",
                    valueRange = 85f..115f,
                    steps = 29,
                    showKeyPoints = false,
                    keyPoints = null,
                    hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                    onValueChange = { onUiScaleChange((it / 100f).coerceIn(0.85f, 1.15f)) },
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ThemeModeSliderPreference(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    val sliderValue = when (themeMode) {
        ThemeMode.FOLLOW_SYSTEM -> 0f
        ThemeMode.LIGHT -> 1f
        ThemeMode.DARK -> 2f
    }

    BasicComponent(
        title = "显示模式",
        summary = themeModeLabel(themeMode),
        endActions = {
            MiuixText(
                text = themeModeLabel(themeMode),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                modifier = Modifier.padding(end = 8.dp),
            )
        },
        bottomAction = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ThemeModeTickLabel("跟随")
                    ThemeModeTickLabel("浅色")
                    ThemeModeTickLabel("深色")
                }
                Slider(
                    value = sliderValue,
                    onValueChange = { rawValue ->
                        val nextMode = when (rawValue.roundToInt().coerceIn(0, 2)) {
                            0 -> ThemeMode.FOLLOW_SYSTEM
                            1 -> ThemeMode.LIGHT
                            else -> ThemeMode.DARK
                        }
                        if (nextMode != themeMode) {
                            onThemeModeChange(nextMode)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    valueRange = 0f..2f,
                    steps = 1,
                    showKeyPoints = false,
                    keyPoints = null,
                    hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                    magnetThreshold = 0.12f,
                )
            }
        },
    )
}

@Composable
private fun ThemeModeTickLabel(text: String) {
    MiuixText(
        text = text,
        style = MiuixTheme.textStyles.body2,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    )
}

@Composable
private fun SettingsEntryGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    MiuixCard(
        modifier = modifier.padding(horizontal = 12.dp),
        colors = MiuixCardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer,
            contentColor = MiuixTheme.colorScheme.onSurfaceContainer,
        ),
        insideMargin = PaddingValues(vertical = 0.dp)
    ) {
        content()
    }
}

@Composable
private fun ApiBaseUrlDialog(
    show: Boolean,
    value: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember(value) { mutableStateOf(value) }

    OverlayDialog(
        show = show,
        title = "接口地址",
        summary = "请输入 API 服务器地址或选择预设",
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiuixButton(
                    modifier = Modifier.weight(1f),
                    onClick = { text = PreferencesManager.DEFAULT_API_BASE_URL },
                    colors = MiuixButtonDefaults.buttonColorsPrimary()
                ) {
                    MiuixText(
                        text = "标准接口",
                        style = MiuixTheme.textStyles.button,
                    )
                }
                MiuixButton(
                    modifier = Modifier.weight(1f),
                    onClick = { text = PreferencesManager.TOKEN_PLAN_API_BASE_URL },
                    colors = MiuixButtonDefaults.buttonColorsPrimary()
                ) {
                    MiuixText(
                        text = "订阅接口",
                        style = MiuixTheme.textStyles.button,
                    )
                }
            }
            MiuixTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                label = "API Base URL",
                useLabelAsPlaceholder = false,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Uri
                ),
                textStyle = MiuixTheme.textStyles.body1
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiuixTextButton(
                    modifier = Modifier.weight(1f),
                    text = "取消",
                    onClick = onDismiss,
                    colors = MiuixButtonDefaults.textButtonColors(
                        textColor = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                )
                Spacer(modifier = Modifier.width(20.dp))
                MiuixTextButton(
                    modifier = Modifier.weight(1f),
                    text = "保存",
                    onClick = { onConfirm(text.trim()) },
                    colors = MiuixButtonDefaults.textButtonColorsPrimary()
                )
            }
        }
    }
}

@Composable
private fun SettingTextDialog(
    show: Boolean,
    title: String,
    value: String,
    placeholder: String,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember(value) { mutableStateOf(value) }

    OverlayDialog(
        show = show,
        title = title,
        summary = placeholder,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MiuixTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                label = if (singleLine) "输入内容" else "编辑内容",
                useLabelAsPlaceholder = true,
                singleLine = singleLine,
                minLines = if (singleLine) 1 else 4,
                maxLines = if (singleLine) 1 else 8,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
                ),
                textStyle = MiuixTheme.textStyles.body1
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiuixTextButton(
                    modifier = Modifier.weight(1f),
                    text = "取消",
                    onClick = onDismiss,
                    colors = MiuixButtonDefaults.textButtonColors(
                        textColor = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                )
                Spacer(modifier = Modifier.width(20.dp))
                MiuixTextButton(
                    modifier = Modifier.weight(1f),
                    text = "保存",
                    onClick = { onConfirm(text.trim()) },
                    colors = MiuixButtonDefaults.textButtonColorsPrimary()
                )
            }
        }
    }
}

@Composable
private fun ParametersDialog(
    show: Boolean,
    state: SettingsUiState,
    onTemperatureChange: (Float) -> Unit,
    onTopPChange: (Float) -> Unit,
    onFrequencyPenaltyChange: (Float) -> Unit,
    onPresencePenaltyChange: (Float) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    OverlayDialog(
        show = show,
        title = "采样与惩罚项",
        summary = "滑动调节生成参数，修改会立即保存。",
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MiuixCard(
                modifier = Modifier.fillMaxWidth(),
                colors = MiuixCardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                    contentColor = MiuixTheme.colorScheme.onSurfaceContainer,
                ),
                insideMargin = PaddingValues(vertical = 0.dp),
            ) {
                HostSliderPreference(
                    title = "Temperature",
                    summary = "控制回答的发散程度",
                    value = state.temperature,
                    valueText = "%.2f".format(state.temperature),
                    valueRange = 0f..2f,
                    steps = 39,
                    showKeyPoints = true,
                    keyPoints = listOf(0f, 0.8f, 1f, 2f),
                    hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                    magnetThreshold = 0.025f,
                    onValueChange = onTemperatureChange,
                )
                HostSliderPreference(
                    title = "Top P",
                    summary = "控制采样概率范围",
                    value = state.topP,
                    valueText = "%.2f".format(state.topP),
                    valueRange = 0f..1f,
                    steps = 19,
                    showKeyPoints = true,
                    keyPoints = listOf(0f, 0.5f, 0.95f, 1f),
                    hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                    magnetThreshold = 0.025f,
                    onValueChange = onTopPChange,
                )
                HostSliderPreference(
                    title = "Frequency Penalty",
                    summary = "降低重复用词倾向",
                    value = state.frequencyPenalty,
                    valueText = "%.2f".format(state.frequencyPenalty),
                    valueRange = -2f..2f,
                    steps = 39,
                    showKeyPoints = true,
                    keyPoints = listOf(-2f, 0f, 2f),
                    hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                    magnetThreshold = 0.025f,
                    onValueChange = onFrequencyPenaltyChange,
                )
                HostSliderPreference(
                    title = "Presence Penalty",
                    summary = "增加新话题探索倾向",
                    value = state.presencePenalty,
                    valueText = "%.2f".format(state.presencePenalty),
                    valueRange = -2f..2f,
                    steps = 39,
                    showKeyPoints = true,
                    keyPoints = listOf(-2f, 0f, 2f),
                    hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                    magnetThreshold = 0.025f,
                    onValueChange = onPresencePenaltyChange,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MiuixTextButton(
                    modifier = Modifier.weight(1f),
                    text = "重置",
                    onClick = onReset,
                    colors = MiuixButtonDefaults.textButtonColors(
                        textColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    ),
                )
                Spacer(modifier = Modifier.width(20.dp))
                MiuixTextButton(
                    modifier = Modifier.weight(1f),
                    text = "完成",
                    onClick = onDismiss,
                    colors = MiuixButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}

@Composable
private fun HostSliderPreference(
    value: Float,
    onValueChange: (Float) -> Unit,
    title: String,
    summary: String,
    valueText: String,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    showKeyPoints: Boolean = false,
    keyPoints: List<Float>? = null,
    hapticEffect: SliderDefaults.SliderHapticEffect = SliderDefaults.DefaultHapticEffect,
    magnetThreshold: Float = 0.02f
) {
    BasicComponent(
        title = title,
        summary = summary,
        endActions = {
            Row(
                modifier = Modifier.padding(end = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                MiuixText(
                    text = valueText,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantActions
                )
            }
        },
        bottomAction = {
            Slider(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                valueRange = valueRange,
                steps = steps,
                hapticEffect = hapticEffect,
                showKeyPoints = showKeyPoints,
                keyPoints = keyPoints,
                magnetThreshold = magnetThreshold
            )
        }
    )
}

private data class ThemeColorOption(
    val label: String,
    val value: ThemeColor
)

private fun buildThemeColorOptions(dynamicSupported: Boolean): List<ThemeColorOption> {
    val options = mutableListOf(
        ThemeColorOption("默认", ThemeColor.WHITE),
        ThemeColorOption("小米橙", ThemeColor.MI_ORANGE),
        ThemeColorOption("系统绿", ThemeColor.GREEN),
        ThemeColorOption("Miku", ThemeColor.HATSUNE_MIKU),
        ThemeColorOption("紫色", ThemeColor.PURPLE)
    )
    if (dynamicSupported) {
        options.add(1, ThemeColorOption("Monet", ThemeColor.AUTO_COLOR))
    }
    return options
}

private fun themeSettingsSummary(
    state: SettingsUiState,
    appearanceSettings: MiuixAppearanceSettings
): String {
    val blurLabel = if (appearanceSettings.blurEnabled) "模糊开" else "模糊关"
    return "${themeModeLabel(state.themeMode)} · ${themeColorLabel(state.themeColor)} · $blurLabel"
}

private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.FOLLOW_SYSTEM -> "跟随系统"
    ThemeMode.LIGHT -> "浅色"
    ThemeMode.DARK -> "深色"
}

private fun themeColorLabel(color: ThemeColor): String = when (color) {
    ThemeColor.WHITE -> "默认"
    ThemeColor.AUTO_COLOR -> "Monet"
    ThemeColor.MI_ORANGE -> "小米橙"
    ThemeColor.GREEN -> "系统绿"
    ThemeColor.HATSUNE_MIKU -> "Miku"
    ThemeColor.PURPLE -> "紫色"
}

private fun parameterSummary(state: SettingsUiState): String {
    return "Temperature ${"%.2f".format(state.temperature)} · Top P ${"%.2f".format(state.topP)} · Presence ${"%.2f".format(state.presencePenalty)}"
}
