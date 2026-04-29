---

# RootDeck

[简体中文](README.md) | [English](README.en.md) | [日本語](README.ja.md)

RootDeck は、Root 環境向けの Android 管理ツールです。  
主にインストール済みアプリの確認、Root 権限管理、WebUI 対応モジュールの入口管理に使用します。

本プロジェクトは Kotlin と Jetpack Compose で開発されており、UI は MIUI / HyperOS のビジュアルスタイルをベースにしています。

## 機能

### アプリ管理

- インストール済みアプリの表示
- アプリ名やパッケージ名による検索
- システムアプリの表示 / 非表示
- アプリ名、APK サイズ、インストール日時による並び替え
- アプリへの Root 権限付与または取り消し

### Root 権限

- 起動時に Root 権限を検出
- Root 権限が取得できない場合に認証を提示
- Root コマンドによる認証ポリシーの書き込みに対応

### モジュール管理

- `/data/adb/modules` ディレクトリをスキャン
- `module.prop` からモジュール情報を読み取り
- モジュール名、作者、バージョン、説明を表示
- 無効化されたモジュールの表示に対応
- `webroot` を持つモジュールの WebUI を開く
- WebView デバッグ切り替えに対応

### UI 設定

- Jetpack Compose で構築
- UI フレームワークは [Miuix](https://github.com/compose-miuix-ui/miuix) ベース
- ライトモード、ダークモード、システム設定に対応
- 多言語対応
- UI スケーリング対応
- フローティングボトムバー対応

## ビルド環境

- Android Studio
- JDK 17
- Gradle Wrapper
- minSdk 26
- targetSdk 35

## ビルド方法

```bash
git clone https://github.com/ShiMahiru/RootDeck.git
cd RootDeck
