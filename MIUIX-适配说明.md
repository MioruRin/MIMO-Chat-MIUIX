# MIMO-Chat MIUIX 适配说明

## 概述

本项目将 MIMO-Chat 从 Material3 (Compose Material3) 迁移至 **MIUIX UI** (v0.9.1)，使用 `top.yukonga.miuix.kmp` 组件库，实现类 MIUI 的系统化 UI 风格。

原始仓库：`MRoldL001/MIMO-Chat`

---

## 主要改动

### 1. 依赖变更 (`app/build.gradle.kts`)

**新增 MIUIX 依赖：**
```kotlin
implementation("top.yukonga.miuix.kmp:miuix-ui-android:0.9.1")
implementation("top.yukonga.miuix.kmp:miuix-preference-android:0.9.1")
implementation("top.yukonga.miuix.kmp:miuix-icons-android:0.9.1")
implementation("top.yukonga.miuix.kmp:miuix-blur-android:0.9.1")
```

**保留的 Material3 依赖（部分仍在使用）：**
```kotlin
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended") // 部分图标
```

---

### 2. 主题系统重写 (`ui/theme/Theme.kt`)

| 原实现 (Material3) | 新实现 (MIUIX) |
|---|---|
| `MaterialTheme()` | `MiuixTheme(controller = ...)` |
| `MaterialTheme.colorScheme` | `MiuixTheme.colorScheme` |
| `lightColorScheme(...)` / `darkColorScheme(...)` | `miuixLightColorScheme(primary=...)` / `miuixDarkColorScheme(primary=...)` |
| 无动态取色 | 支持 `ColorSchemeMode.MonetSystem` / `MonetLight` / `MonetDark` |
| `isSystemInDarkTheme()` | 封装在 `isDarkTheme(themeMode)` 中 |

**新增 `ThemeController` 配置：**
- `paletteStyle = ThemePaletteStyle.TonalSpot`
- `colorSpec = ThemeColorSpec.Spec2025`
- 支持 `ThemeColor.AUTO_COLOR` 自动取色（Android 12+ Monet）

**系统栏适配：**
- `applySystemBars()` 使用 `MiuixTheme.colorScheme.background` 动态设置状态栏/导航栏颜色
- `insetsController.isAppearanceLightStatusBars` 根据暗色模式自动切换

---

### 3. 颜色定义 (`ui/theme/Color.kt`)

新增多套主题色（用于 `ThemeColor` 枚举切换）：

| ThemeColor 枚举 | Light Primary | Dark Primary |
|---|---|---|
| `WHITE` | `#000000` | `#FFFFFF` |
| `MI_ORANGE` | XiaoMiOrange `#FF7E00` | XiaoMiOrangeDark `#E86F00` |
| `GREEN` | `#006E2A` | `#5CDB78` |
| `PURPLE` | `#6650A4` | `#D0BCFF` |
| `HATSUNE_MIKU` | `#39C5BB` | `#26A69A` |
| `AUTO_COLOR` | null (Monet) | MiOrangeDarkPrimary |

---

### 4. 新增文件

#### `ui/MiuixAppHost.kt`
- 应用主容器，使用 `HorizontalPager` 实现「对话 / 设置」双页切换
- 使用 MIUIX `Scaffold` + `FloatingNavigationBar`（手机）/ `NavigationBar`（大屏）
- 依赖 `rememberHostPagerState()` / `rememberHostBackdrop()` 管理页面状态和模糊效果

#### `ui/MiuixHostScaffold.kt`
- 抽取 `HostBottomNavigationBar`、`HostHeaderIconButton`、`HostBlurredBar` 等可复用组件
- 实现 MIUIX 模糊特效：`textureBlur(backdrop, shape, blurRadius=25f, blendColors=...)`
- 处理 `WindowInsets.navigationBars` / `captionBar` 适配全面屏

#### `ui/MiuixChatPage.kt`
- 对话列表页，使用 `LazyColumn` + `MiuixTheme.textStyles`
- 支持 `ChatStage.Archive`（会话列表）/ `ChatStage.Detail`（对话详情）双状态
- 集成 `SearchViewModel` 实现搜索状态机 (`SearchStatus`)

#### `ui/chat/components/MiuixChatShell.kt`
- 对话详情页顶部 Header（`MiuixConversationHeader`）
- 使用 `MiuixCard(cornerRadius=28.dp)` + `MiuixTheme.colorScheme.surfaceContainer`

#### `ui/chat/components/InputBar.kt`
- 输入框改造为 MIUIX `MiuixTextField` + `MiuixButton`
- 使用 `MiuixTheme.colorScheme.surfaceContainerHigh` 作为背景

#### `ui/chat/components/MessageBubble.kt`
- 消息气泡使用 `MiuixCard(colors = CardDefaults.defaultColors(...))`
- 用户消息：`color = MiuixTheme.colorScheme.primary`
- AI 消息：`color = MiuixTheme.colorScheme.surfaceContainer`

---

### 5. 设置页面重写 (`ui/settings/SettingsScreen.kt`)

| 原实现 | 新实现 (MIUIX Preference) |
|---|---|
| 手动 Compose 布局 | `ArrowPreference`、`SwitchPreference`、`SliderPreference` |
| 无标准化Preference | `OverlayDropdownPreference`（对话框下拉） |
| 手动存储 | 集成 `PreferencesManager.getMiuixAppearanceSettings()` |

**新增 `MiuixAppearanceSettings` 可配置项：**
- `blurEnabled: Boolean` — 是否启用模糊效果
- `uiScale: Float` — UI 缩放比例（通过 `LocalDensity` 注入）

---

### 6. 导航简化 (`ui/AppNavigation.kt`)

原实现包含 Drawer + NavRail + NavHost 多分支逻辑，现简化为：

```kotlin
@Composable
fun AppNavigation(...) {
    MiuixAppHost(
        appearanceSettings = appearanceSettings,
        onThemeChanged = onThemeChanged,
        onAppearanceSettingsChanged = onAppearanceSettingsChanged,
        onBackToChat = onBackToChat
    )
}
```

所有导航逻辑封装在 `MiuixAppHost` + `HorizontalPager` 内部。

---

### 7. MainActivity 调整 (`MainActivity.kt`)

- 新增 `MainViewModel`（Hilt 注入 `PreferencesManager`）
- 新增 `LocalMiuixAppearanceSettings` CompositionLocal
- 通过 `CompositionLocalProvider` 注入 `appearanceSettings` 和自定义 `Density`（支持 uiScale）
- 通知权限请求逻辑保持不变

---

### 8. 模糊效果 (`miuix-blur`)

- 使用 `rememberLayerBackdrop { drawRect(surfaceColor); drawContent() }` 创建模糊图层
- 条件启用：`appearanceSettings.blurEnabled && isRuntimeShaderSupported()`
- 回落方案：不支持 RuntimeShader 的设备使用纯色 `surface` 背景

---

## 未改动部分

以下模块保持原样（未涉及 UI 层）：
- `data/api/` — API 服务层
- `data/local/` — Room 数据库
- `data/repository/` — 数据仓库
- `data/preferences/` — DataStore Preferences
- `di/` — Hilt 依赖注入
- `domain/model/` — 数据模型
- `service/` — 后台服务

---

## 构建说明

```bash
# 要求
# - Android Studio Panda / Koala 或更高
# - AGP 8.8+ / Kotlin 2.0+
# - compileSdk 35 / minSdk 26

./gradlew assembleDebug
```

---

## 已知问题 / TODO

- [ ] 部分 Material3 依赖尚未完全移除（`material-icons-extended` 仍用于个别图标）
- [ ] 平板 / 折叠屏适配：`MiuixAppHost` 目前主要针对手机优化
- [ ] 模糊效果在部分设备上可能出现性能问题，建议增加 FPS 监控

---

*最后更新：2026-05-30*
