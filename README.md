# RootDeck 发布说明（GitHub Actions）

本项目已配置发布工作流：**支持打标签自动触发，也支持网页端手动触发**。

## 工作流文件位置

- `.github/workflows/release.yml`
- 工作流名称：`Build and Draft Signed Release`

## 触发方式

当前支持两种触发方式：

- `push.tags: v*`（例如推送 `v1.0.3` 时自动触发）
- `workflow_dispatch`（在 GitHub 页面手动点击运行）

### 方式一：打标签自动触发

1. 本地创建并推送标签，例如：
   ```bash
   git tag v1.0.3
   git push origin v1.0.3
   ```
2. 推送完成后，GitHub Actions 会自动执行该工作流。
3. 工作流会使用标签名作为版本号（`VERSION=v1.0.3`）。

### 方式二：网页端手动触发

1. 打开仓库主页，点击顶部 **Actions**。
2. 在左侧选择 **Build and Draft Signed Release**。
3. 点击右上区域的 **Run workflow**。
4. 在 `version` 输入框里填写版本号（例如：`v1.0.4`）。
5. 点击绿色 **Run workflow** 开始执行。

手动触发时，工作流会使用你输入的 `version` 作为版本号。

## 运行后会发生什么

工作流会自动执行以下步骤：

1. 检出代码。
2. 配置 JDK 17。
3. 编译 `assembleRelease`。
4. 使用仓库 Secrets 自动签名 APK。
5. 生成并重命名产物为：
   - `RootDeck-<version>-signed.apk`
6. 创建一个 GitHub Release 草稿，并上传该 APK。

## 需要提前配置的 Secrets

在仓库 **Settings → Secrets and variables → Actions** 中配置：

- `KEYSTORE_BASE64`
- `KEY_ALIAS`
- `KEYSTORE_PASSWORD`
- `KEY_PASSWORD`

## 版本号建议

- 建议统一使用 `v` 前缀，例如：`v1.0.2`、`v1.0.3`。
- 手动触发和标签触发都建议保持语义化版本格式，便于追踪。
- 每次发布请使用新版本号，避免产物和 Release 混淆。
