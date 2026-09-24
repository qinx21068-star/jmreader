# 配置 GitHub Actions Release 构建指南

本文档说明如何配置 GitHub Actions 来构建签名的 Release APK。

## 步骤 1：生成签名密钥

在你的**本地电脑**上（不是 Alpine 环境）运行以下命令：

```bash
keytool -genkey -v -keystore release.keystore -alias jmreader \
  -keyalg RSA -keysize 2048 -validity 10000
```

会提示你输入：
- **Keystore 密码**（输入两次）- 记住这个密码
- **密钥密码**（输入两次）- 记住这个密码
- 姓名、组织等信息（可以随意填写）

完成后会生成 `release.keystore` 文件。

⚠️ **重要**：
- 这个文件包含你的应用签名密钥
- 务必妥善保管，丢失后无法更新已发布的应用
- **不要提交到 Git 仓库**
- 建议备份到安全的地方

## 步骤 2：将 keystore 转换为 base64

```bash
# Linux/macOS
base64 release.keystore > keystore.txt

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore")) > keystore.txt
```

`keystore.txt` 文件中的内容就是你需要的 base64 编码。

## 步骤 3：在 GitHub 仓库中配置 Secrets

1. 进入你的 GitHub 仓库
2. 点击 **Settings** → **Secrets and variables** → **Actions**
3. 点击 **New repository secret**，添加以下 4 个 secret：

| Secret 名称 | 值 | 说明 |
|------------|---|------|
| `KEYSTORE_BASE64` | `keystore.txt` 的内容 | base64 编码的 keystore 文件 |
| `JM_KEYSTORE_PW` | 你设置的 keystore 密码 | 步骤 1 中输入的 keystore 密码 |
| `JM_KEY_PW` | 你设置的密钥密码 | 步骤 1 中输入的密钥密码 |
| `JM_KEY_ALIAS` | `jmreader` | 密钥别名（与生成时的 -alias 参数一致）|

⚠️ **注意**：
- Secret 名称必须完全一致（区分大小写）
- 粘贴 `KEYSTORE_BASE64` 时，复制整个 `keystore.txt` 的内容，包括开头和结尾
- 不要在值的前后添加引号或空格

## 步骤 4：推送代码触发构建

```bash
cd /workspace/jmtt.apk
git add .github/workflows/build.yml
git commit -m "Configure Release build with signing"
git push
```

或者在 GitHub Actions 页面手动触发 workflow。

## 步骤 5：下载 APK

构建完成后：

1. 进入仓库的 **Actions** 标签
2. 点击最新的 workflow run
3. 在 **Artifacts** 部分下载：
   - **app-debug**：Debug 版本 APK（总是会生成）
   - **app-release**：Release 版本 APK（配置签名后才会生成）

## 验证签名

下载 Release APK 后，可以验证签名信息：

```bash
# 查看签名信息
keytool -printcert -jarfile app-release.apk

# 应该能看到：
# - Owner: CN=你填写的信息
# - Issuer: CN=你填写的信息
# - 有效期: 10000 天
```

## 故障排查

### 构建失败：Keystore file not found

- 检查 `KEYSTORE_BASE64` secret 是否正确配置
- 确保 base64 内容完整（没有截断）

### 构建失败：Keystore was tampered with, or password was incorrect

- 检查 `JM_KEYSTORE_PW` 密码是否正确
- 确保密码中没有多余的空格

### 构建失败：Alias not found

- 检查 `JM_KEY_ALIAS` 是否为 `jmreader`
- 如果生成时用了不同的 alias，修改这个 secret

### 只生成了 Debug APK，没有 Release APK

- 说明 Secrets 没有配置，或配置不正确
- 检查所有 4 个 secret 是否都已添加

## 安全建议

✅ **推荐做法**：
- 将 `release.keystore` 备份到安全的地方（加密存储、密码管理器等）
- 定期更换密码（需要重新生成 keystore）
- 不要在多个项目中重复使用同一个 keystore

❌ **不要**：
- 将 keystore 文件提交到 Git 仓库
- 在公开的地方分享 keystore 或密码
- 使用弱密码（建议至少 12 位，包含字母数字符号）

## 本地测试 Release 构建

如果想在本地测试 Release 构建：

```bash
# 将 release.keystore 放到项目根目录
cp /path/to/release.keystore /workspace/jmtt.apk/

# 构建 Release APK
cd /workspace/jmtt.apk
./gradlew assembleRelease
```

⚠️ 但在 Alpine 环境中仍然无法构建，这个只能在有完整 Android SDK 的环境中使用。
