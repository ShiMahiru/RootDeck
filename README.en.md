# RootDeck

[简体中文](README.md) | [English](README.en.md) | [日本語](README.ja.md)

RootDeck is an Android management tool designed for Root environments.  
It is mainly used for viewing installed applications, managing Root authorization, and accessing module WebUI entries.

The project is built with Kotlin and Jetpack Compose, with a UI style inspired by MIUI / HyperOS.

## Features

### App Management

- View installed applications
- Search by app name or package name
- Show or hide system apps
- Sort by app name, APK size, or installation time
- Grant or revoke Root authorization for apps

### Root Permission

- Detect Root permission on startup
- Prompt for authorization when Root access is unavailable
- Write authorization policies through Root commands

### Module Management

- Scan the `/data/adb/modules` directory
- Read module information from `module.prop`
- Display module name, author, version, and description
- Show disabled modules
- Open module WebUI when a `webroot` directory exists
- Support WebView debugging toggle

### UI Settings

- Built with Jetpack Compose
- UI framework based on [Miuix](https://github.com/compose-miuix-ui/miuix)
- Supports light mode, dark mode, and system default
- Supports multiple languages
- Supports UI scaling
- Supports floating bottom bar

## Build Environment

- Android Studio
- JDK 17
- Gradle Wrapper
- minSdk 26
- targetSdk 35

## Build

```bash
git clone https://github.com/ShiMahiru/RootDeck.git
cd RootDeck
