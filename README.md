# RootDeck

RootDeck 是一个面向 Root 环境的 Android 管理工具，主要用于应用列表查看、Root 授权管理，以及支持 WebUI 的模块入口管理。

项目使用 Kotlin 与 Jetpack Compose 编写，界面风格偏向 MIUI / HyperOS，适合 Root 工具、模块管理器、超级用户授权面板等场景继续扩展。

## 功能特性

### 应用管理

- 查看设备中已安装的应用
- 支持搜索应用名称和包名
- 支持显示或隐藏系统应用
- 支持按应用名称、安装包大小、安装时间排序
- 点击应用后可执行 Root 授权或撤销操作

### Root 权限

- 启动时检测 Root 权限
- 未获取 Root 权限时提示用户授权
- 支持通过 Root 命令写入授权策略
- 适合 Magisk / KernelSU / APatch 等 Root 环境使用

### 模块管理

- 扫描 `/data/adb/modules` 目录
- 读取模块的 `module.prop` 信息
- 显示模块名称、作者、版本和描述
- 支持显示禁用模块
- 支持打开带 `webroot` 的模块 WebUI
- 支持 WebView 调试开关

### 界面与设置

- 使用 Jetpack Compose 构建
- 使用 Material 3 组件
- MIUI / HyperOS 风格界面
- 支持浅色、深色、跟随系统
- 支持多语言
- 支持 UI 缩放
- 支持悬浮底栏

## 构建环境

- Android Studio
- JDK 17
- Gradle Wrapper
- Kotlin
- Jetpack Compose
- Android Gradle Plugin
- minSdk 26
- targetSdk 35

## 构建方式

克隆项目：

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

```bash
app/build/outputs/apk/
```

## 版本号说明

RootDeck 的版本号统一在源码中维护。

位置：

```txt
app/build.gradle.kts
```

示例：

```kotlin
versionCode = 2
versionName = "1.1.0"
```

发布新版本时，只需要修改这里：

```kotlin
versionCode = 3
versionName = "1.2.0"
```

GitHub Actions 会自动读取源码中的 `versionName` 和 `versionCode`，并同步到 Release 名称、Tag 校验和 APK 文件名。

示例生成文件：

```txt
RootDeck-v1.2.0-3-signed.apk
```

## GitHub Actions 自动发布

项目支持通过 GitHub Actions 自动构建签名版 APK，并创建 GitHub Release 草稿。

工作流文件位置：

```txt
.github/workflows/release.yml
```

触发方式：

### 方式一：推送 Tag

```bash
git tag v1.2.0
git push origin v1.2.0
```

Tag 必须和源码中的 `versionName` 对应。

例如源码中是：

```kotlin
versionName = "1.2.0"
```

那么 Tag 必须是：

```txt
v1.2.0
```

如果 Tag 和源码版本不一致，工作流会自动停止，避免 Release 版本号和源码版本号不一致。

### 方式二：手动运行

也可以在 GitHub 仓库页面中进入：

```txt
Actions -> Build and Draft Signed Release -> Run workflow
```

手动运行时，版本号同样会从 `app/build.gradle.kts` 自动读取。

## 签名配置

自动签名需要在 GitHub 仓库中配置以下 Secrets：

| Secret 名称 | 说明 |
|---|---|
| `KEYSTORE_BASE64` | keystore 文件转成 Base64 后的内容 |
| `KEY_ALIAS` | 签名别名 |
| `KEYSTORE_PASSWORD` | keystore 密码 |
| `KEY_PASSWORD` | key 密码 |

将 keystore 转成 Base64：

```bash
base64 -w 0 your-release-key.jks
```

然后把输出内容填入 GitHub Secrets 的 `KEYSTORE_BASE64`。

## Root 权限说明

RootDeck 需要 Root 权限才能使用完整功能。

部分功能依赖：

- `su`
- Magisk / KernelSU / APatch 等 Root 环境
- `/data/adb/modules` 目录访问权限

没有 Root 权限时，模块读取、授权管理等功能可能无法正常使用。

## 模块 WebUI 说明

RootDeck 会扫描：

```txt
/data/adb/modules
```

当模块目录中存在 `webroot` 文件夹时，会识别为支持 WebUI 的模块。

示例结构：

```txt
/data/adb/modules/example_module/
├── module.prop
└── webroot/
```

`module.prop` 示例：

```properties
id=example_module
name=Example Module
version=1.0.0
versionCode=1
author=Author
description=Example module with WebUI
```

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX
- libsu
- WebView
- Gradle Kotlin DSL
- GitHub Actions

## 项目结构

```txt
RootDeck
├── app
│   ├── src
│   │   └── main
│   │       ├── kotlin
│   │       │   └── com/rtools/superapp
│   │       ├── res
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle
├── .github
│   └── workflows
│       └── release.yml
├── settings.gradle.kts
└── README.md
```

## 注意事项

- 本项目需要 Android 8.0 及以上系统。
- Root 功能需要设备已正确配置 Root 环境。
- Release 构建需要配置签名 Secrets。
- Tag 发布时，Tag 版本号需要和源码中的 `versionName` 保持一致。
- 当前 Release 工作流会创建草稿版本，确认无误后需要手动发布。

## License

暂无 License。

开源发布时可以根据需求添加许可证，例如：

- MIT License
- Apache License 2.0
- GPL-3.0 License
