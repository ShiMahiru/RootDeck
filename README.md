# RootDeck
[简体中文](README.md) | [English](README.en.md) | [日本語](README.ja.md)
RootDeck 是一个面向 Root 环境的 Android 管理工具，主要用于应用列表查看、Root 授权管理，以及支持 WebUI 的模块入口管理。

项目使用 Kotlin 与 Jetpack Compose 编写，界面风格基于 MIUI / HyperOS 视觉风格。

## 功能特性

### 应用管理

- 查看已安装应用
- 搜索应用名称和包名
- 显示或隐藏系统应用
- 按应用名称、安装包大小、安装时间排序
- 对应用执行 Root 授权或撤销操作

### Root 权限

- 启动时检测 Root 权限
- 未获取 Root 权限时提示授权
- 支持通过 Root 命令写入授权策略

### 模块管理

- 扫描 `/data/adb/modules` 目录
- 读取模块 `module.prop` 信息
- 显示模块名称、作者、版本和描述
- 支持显示禁用模块
- 支持打开带 `webroot` 的模块 WebUI
- 支持 WebView 调试开关

### 界面设置

- Jetpack Compose 构建
- UI 框架基于 [Miuix](https://github.com/compose-miuix-ui/miuix)
- 支持浅色、深色、跟随系统
- 支持多语言
- 支持 UI 缩放
- 支持悬浮底栏

## 构建环境

- Android Studio
- JDK 17
- Gradle Wrapper
- minSdk 26
- targetSdk 35

## 构建方式

```bash
git clone https://github.com/ShiMahiru/RootDeck.git
cd RootDeck
```

构建 Debug APK：

```bash
./gradlew assembleDebug
```

构建 Release APK：

```bash
./gradlew assembleRelease
```

生成的 APK 位于：

```txt
app/build/outputs/apk/
```

## Root 权限说明

RootDeck 需要 Root 权限才能使用完整功能。

部分功能依赖：

- `su`
- Magisk Root 环境
- `/data/adb/modules` 目录访问权限

没有 Root 权限时，模块读取、授权管理等功能可能无法正常使用。

## 模块 WebUI 说明

RootDeck 会扫描：

```txt
/data/adb/modules
```

当模块目录中存在 `webroot` 文件夹时，会识别为支持 WebUI 的模块。

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX
- libsu
- WebView
- Gradle Kotlin DSL
- GitHub Actions
- [KsuWebUIStandalone](https://github.com/5ec1cff/KsuWebUIStandalone)
- [Miuix](https://github.com/compose-miuix-ui/miuix)

## 鸣谢

感谢 [5ec1cff](https://github.com/5ec1cff) 提供的 [KsuWebUIStandalone](https://github.com/5ec1cff/KsuWebUIStandalone)。

感谢 [compose-miuix-ui/miuix](https://github.com/compose-miuix-ui/miuix) 提供的 Miuix UI 框架。

## 注意事项

- 本项目需要 Android 8.0 及以上系统。
- Root 功能需要设备已正确配置 Root 环境。
