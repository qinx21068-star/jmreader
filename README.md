# JM Reader

JM 漫画阅读器 Android 应用

## 功能特性

- 📖 漫画阅读
- 🔖 稍后再看收藏功能
- 🔔 快速通知栏输入
- 🔐 应用锁（指纹/PIN）
- 💾 外部存储下载支持

## 构建说明

### 本地构建

需要：
- JDK 17
- Android SDK (API 36)
- Gradle 8.10.2

```bash
./gradlew assembleDebug
```

生成的 APK 位置：`app/build/outputs/apk/debug/app-debug.apk`

### GitHub Actions 自动构建

本项目已配置 GitHub Actions 自动构建：

1. **推送代码到 GitHub**：
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/你的用户名/jmtt.apk.git
   git push -u origin main
   ```

2. **自动触发构建**：
   - 推送到 `main`/`master`/`dev` 分支时自动构建
   - 也可以在 GitHub Actions 页面手动触发

3. **下载 APK**：
   - 构建完成后，在 Actions 页面找到对应的 workflow run
   - 点击 "app-debug" artifact 下载 APK
   - APK 会保留 30 天

### Release 版本构建

Release 版本需要签名配置。配置完成后，GitHub Actions 会自动构建签名的 Release APK。

**详细配置步骤请查看：[RELEASE_BUILD_GUIDE.md](RELEASE_BUILD_GUIDE.md)**

简要步骤：
1. 在本地电脑生成签名密钥（keystore）
2. 将 keystore 转换为 base64 编码
3. 在 GitHub 仓库配置 4 个 Secrets
4. 推送代码，自动构建 Release APK

⚠️ 如果不配置签名密钥，只会生成 Debug APK（功能完全相同）。

## 技术栈

- Kotlin 2.1.0
- Jetpack Compose
- Material 3
- **Hilt** - 依赖注入 ⭐ (v28.0 新增)
- Retrofit + OkHttp
- Coil (图片加载)
- Coroutines
- DataStore

## 版本信息

- **当前版本：v28.0-alpha** - Hilt 依赖注入集成 ⭐
- 稳定版本：1.2.0 (versionCode 3)
- 最低支持：Android 7.0 (API 24)
- 目标版本：Android 16 (API 36)

## 📚 项目文档

本项目正在进行架构重构，采用现代化的 Clean Architecture + MVI + Hilt 架构。

### 快速导航

- **[📖 文档索引 (DOCS_INDEX.md)](DOCS_INDEX.md)** - 查看所有文档和推荐阅读顺序
- **[📋 重构计划 (REFACTORING_PLAN.md)](REFACTORING_PLAN.md)** - 完整的 14 周重构计划
- **[📊 重构进度 (REFACTORING_PROGRESS.md)](REFACTORING_PROGRESS.md)** - 实时进度追踪

### Phase 1.1: Hilt 依赖注入 (当前阶段 - 90% 完成)

核心文档：
- **[🚀 快速开始指南 (QUICK_START_GUIDE.md)](QUICK_START_GUIDE.md)** - 后续工作指南
- **[📘 Hilt 迁移指南 (HILT_MIGRATION_GUIDE.md)](HILT_MIGRATION_GUIDE.md)** - 详细迁移步骤
- **[📄 项目状态报告 (PROJECT_STATUS_REPORT.md)](PROJECT_STATUS_REPORT.md)** - 完整状态报告

技术总结：
- [Phase 1.1 总结 (PHASE_1_1_SUMMARY.md)](PHASE_1_1_SUMMARY.md)
- [Phase 1.1 完成报告 (PHASE_1_1_COMPLETION.md)](PHASE_1_1_COMPLETION.md)
- [工作总结 (WORK_SUMMARY.md)](WORK_SUMMARY.md)

### 重构进度

```
Phase 1: 基础设施重构 (当前)
├─ 1.1 Hilt 依赖注入    ████████████████░░ 90% ⬅ 当前
├─ 1.2 Room 数据库      ░░░░░░░░░░░░░░░░░░  0%
├─ 1.3 网络层优化       ░░░░░░░░░░░░░░░░░░  0%
└─ 1.4 模块化拆分       ░░░░░░░░░░░░░░░░░░  0%
```

### 架构亮点 (v28.0)

- ✅ **Hilt 依赖注入** - 类型安全，编译时检查
- ✅ **渐进式迁移** - 零破坏性变更，向后兼容
- ✅ **完善文档** - 12 个技术文档，~100 KB
- 🚧 **Clean Architecture** - 进行中
- 🚧 **MVI 架构** - 计划中

### 开发者指南

**新加入的开发者**请按以下顺序阅读：
1. README.md (本文件) - 项目概览
2. [DOCS_INDEX.md](DOCS_INDEX.md) - 文档导航
3. [REFACTORING_PLAN.md](REFACTORING_PLAN.md) - 重构计划
4. [HILT_MIGRATION_GUIDE.md](HILT_MIGRATION_GUIDE.md) - Hilt 使用指南
5. [QUICK_START_GUIDE.md](QUICK_START_GUIDE.md) - 开始工作

**想要贡献代码**？
- 查看 [REFACTORING_PROGRESS.md](REFACTORING_PROGRESS.md) 了解待完成任务
- 遵循 [HILT_MIGRATION_GUIDE.md](HILT_MIGRATION_GUIDE.md) 中的最佳实践
- 每完成一个任务，更新进度文档

## 许可证

请根据项目实际情况添加许可证信息。
# 触发新构建
