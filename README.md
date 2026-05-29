<div align="center" style="display: flex; justify-content: center; align-items: center; gap: 20px;">
  <img src="mimo-title.jpg" alt="LOGO" width="325">
</div>

<br/>

# MIMO Chat - MIUIX 版

> 基于 [MRoldL001/MIMO-Chat](https://github.com/MRoldL001/MIMO-Chat) 的 **MIUIX UI 适配分支**
>
> 第三方小米 MiMo 大模型 Android 客户端 · 全面迁移至 MIUIX 设计语言

[![MiMo](https://img.shields.io/badge/MiMo-console-FF7E00?style=for-the-badge&logo=xiaomi&logoColor=white)](https://platform.xiaomimimo.com/console/balance)
[![MIUIX](https://img.shields.io/badge/MIUIX-v0.9.1-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/miuix-kotlin/miuix)

[![GitHub Release](https://img.shields.io/github/v/release/MioruRin/MIMO-Chat-MIUIX?style=for-the-badge)](https://github.com/MioruRin/MIMO-Chat-MIUIX/releases)
[![Download](https://img.shields.io/github/downloads/MioruRin/MIMO-Chat-MIUIX/total?style=for-the-badge)](https://github.com/MioruRin/MIMO-Chat-MIUIX/releases)

---

## 🎨 MIUIX 适配说明

本项目是 MIMO-Chat 的 **MIUIX 下游分支**，将原有 Material3 (Compose) UI 全面迁移至 **MIUIX v0.9.1**（`top.yukonga.miuix.kmp`）设计体系。

### 主要变更

| 变更项 | 原版 (Material3) | MIUIX 版 |
|--------|-------------------|-----------|
| UI 框架 | Jetpack Compose Material3 | MIUIX KMP Components |
| 主题系统 | MaterialTheme | MiuixTheme + ThemeController |
| 导航结构 | NavHost + BottomNav | MiuixAppHost + HorizontalPager |
| 设置页 | Material3 Settings | MiuixAppearanceSettings |
| 模糊效果 | 不支持 | `miuix-blur` 原生支持 |
| 圆角风格 | Material 大圆角 | MIUIX 小圆角 (6dp) |

---

## ✨ 功能

- ✅ 多模型切换支持
- ✅ 代码块渲染
- ✅ 较完整的 LaTeX 公式渲染（包含化学公式等额外扩展）
- ✅ 思考过程展示
- ✅ 对话历史管理
- ✅ **MIUIX 模糊特效（可开关）**
- ✅ **UI 缩放调节（Small / Medium / Large）**
- ✅ 自定义系统提示词
- ✅ 手机端与 Pad 端自适应布局
- ✅ 通知栏保活

---

## 📱 快速开始

### 安装

#### 方式一：从 GitHub Releases 下载（推荐）

1. 访问本项目的 [Releases](https://github.com/MioruRin/MIMO-Chat-MIUIX/releases)
2. 下载最新的 `MIMO-Chat-v*-miuix.apk` 文件
3. 在手机上安装（可能需要允许未知来源应用）

#### 方式二：从源码构建

> ⚠️ **注意：MIUIX 版依赖 `top.yukonga.miuix.kmp:miuix:0.9.1`，需确认 Maven Central 可访问**

1. 使用 `git clone` 将 Repo 克隆到本地
   ```bash
   git clone https://github.com/MioruRin/MIMO-Chat-MIUIX.git
   cd MIMO-Chat-MIUIX
   git checkout miuix
   ```
2. 使用 Android Studio 打开项目
3. 同步 Gradle（自动下载 MIUIX 依赖）
4. 构建 APK

---

## ⚙️ 配置

1. 首次打开应用，点击右上角设置图标
2. 设置你的小米 MiMo API Key
3. （可选）配置 API Base URL
4. （可选）在设置中开启/关闭模糊特效、调节 UI 缩放

---

## 🔄 与上游同步

本分支会尽量跟进上游 `MRoldL001/MIMO-Chat` 的功能更新，但 UI 层变更不会合并（保持 MIUIX 风格）。

---

## 📄 授权

基于上游项目授权协议，本项目同样开源。具体授权信息请查看上游项目页面。

> 原始项目：[MRoldL001/MIMO-Chat](https://github.com/MRoldL001/MIMO-Chat)
> MIUIX 组件库：[miuix-kotlin/miuix](https://github.com/miuix-kotlin/miuix)
