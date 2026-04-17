# RootDeck 发布说明（GitHub Actions）

当前仓库包含两个工作流：

- `.github/workflows/release.yml`（工作流名：`构建并草稿签名发布版`）
- `.github/workflows/debug.yml`（工作流名：`编译 Debug 版 APK`）

> 你提到的 `.github/workflows/main.yml` 可以删除。该文件如果为空或不再使用，删除后不会再产生新的运行记录；但历史运行记录仍会在 Actions 页面保留，需手动清理。

---

## 1) 发布版工作流（构建并草稿签名发布版）

### 触发方式

仅支持：

- `workflow_dispatch`（在 GitHub Actions 页面手动点击 **Run workflow**）

### 使用步骤

1. 打开仓库主页，点击顶部 **Actions**。
2. 在左侧选择 **构建并草稿签名发布版**。
3. 点击 **Run workflow**。
4. 在 `version` 输入框里填写版本号（例如：`v1.0.4`）。
5. 点击绿色 **Run workflow** 开始执行。

### 运行后会做什么

1. 检出代码。
2. 配置 JDK 17。
3. 校验签名 Secrets。
4. 编译 `assembleRelease`。
5. 使用 Secrets 自动签名 APK。
6. 产物命名为 `RootDeck-<version>-signed.apk`。
7. 创建 GitHub Release 草稿并上传 APK。

### 需要提前配置的 Secrets

在仓库 **Settings → Secrets and variables → Actions** 中配置：

- `KEYSTORE_BASE64`
- `KEY_ALIAS`
- `KEYSTORE_PASSWORD`
- `KEY_PASSWORD`

---

## 2) Debug 工作流（编译 Debug 版 APK）

### 触发方式

仅支持：

- `workflow_dispatch`（手动触发）

### 运行后会做什么

1. 检出代码。
2. 配置 JDK 17。
3. 执行 `./gradlew assembleDebug`。
4. 上传 `app/build/outputs/apk/debug/*.apk` 为 Actions Artifact（`rootdeck-debug-apk`）。

---

## 常见问题

### Q1：为什么会自动执行 `.github/workflows/main.yml`？

通常是该文件过去配置过 `push` / `pull_request` 等触发器，或者历史上曾有效执行过。你现在删除它后，不会再有新触发。

### Q2：为什么 Actions 里还看到 `main.yml` 相关记录？

因为那是历史运行记录，不会因删除文件自动消失。可在 Actions 页面逐条删除（或等待保留期到期自动清理）。
