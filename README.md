# RootDeck 手动发布说明（GitHub Actions）

本项目已配置为：**只允许通过 GitHub 网页上的 `Run workflow` 按钮触发打包发布**。

## 工作流文件位置

- `.github/workflows/release.yml`
- 工作流名称：`Build and Draft Signed Release`

## 触发方式（仅手动）

当前触发条件只有：

- `workflow_dispatch`

这表示：

- 不会因为 `push` 或打 `tag` 自动触发。
- 只有你在 GitHub 页面手动点击，才会执行打包流程。

## 如何手动运行

1. 打开仓库主页，点击顶部 **Actions**。
2. 在左侧选择 **Build and Draft Signed Release**。
3. 点击右上区域的 **Run workflow**。
4. 在 `version` 输入框里填写版本号（例如：`v1.0.3`）。
5. 点击绿色 **Run workflow** 开始执行。

## 运行后会发生什么

工作流会自动执行以下步骤：

1. 检出代码。
2. 配置 JDK 17。
3. 校验签名 Secrets（缺失、Base64 无效、密码不匹配、Alias 不存在会提前失败）。
4. 编译 `assembleRelease`。
5. 使用仓库 Secrets 自动签名 APK。
6. 将输出重命名为：
   - `RootDeck-<version>-signed.apk`
7. 创建一个 GitHub Release 草稿，并上传该 APK。

## 需要提前配置的 Secrets

在仓库 **Settings → Secrets and variables → Actions** 中配置：

- `KEYSTORE_BASE64`
- `KEY_ALIAS`
- `KEYSTORE_PASSWORD`
- `KEY_PASSWORD`

## 常见问题排查

### 1) `apksigner` 报错：`Tag number over 30 is not supported`

通常是签名材料有问题，重点检查：

- `KEYSTORE_BASE64` 是否真的是 **keystore 文件本体** 的 Base64（不是证书文本，也不是路径）。
- `KEYSTORE_PASSWORD` 是否与该 keystore 匹配。
- `KEY_ALIAS` 是否存在于 keystore 中。
- `KEY_PASSWORD` 是否与该 alias 的私钥密码匹配。

本工作流已增加预校验步骤，会在编译前给出更明确的错误提示。

### 2) Node.js 20 弃用警告

工作流已设置 `FORCE_JAVASCRIPT_ACTIONS_TO_NODE24=true`，提前切换到 Node.js 24 运行 JS Actions，以规避 Node.js 20 弃用告警。

## 版本号建议

- 建议统一使用 `v` 前缀，例如：`v1.0.2`、`v1.0.3`。
- 每次手动发布请使用新版本号，避免产物和 Release 混淆。
