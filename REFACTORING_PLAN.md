# JM Reader 全面重构升级计划

## 📊 项目现状分析

### 当前技术栈
- **语言**: Kotlin 2.1.0
- **UI框架**: Jetpack Compose + Material 3
- **构建工具**: Gradle 8.10.2 + AGP 8.5.0
- **目标平台**: Android 16 (API 36) / 最低 Android 7.0 (API 24)
- **架构**: 部分 MVVM（有 Repository + ViewModel，但不完整）
- **网络**: Retrofit 2.11.0 + OkHttp 4.12.0 + Moshi
- **图片加载**: Coil 2.7.0 + Telephoto（缩放）
- **存储**: DataStore 1.1.1 (Preferences)
- **导航**: Navigation Compose 2.8.5

### 项目结构
```
app/src/main/java/com/jmreader/
├── core/                    # 核心工具（日志、崩溃处理、Coil配置）
├── data/
│   ├── api/                 # 网络层（Retrofit + 直连客户端）
│   │   ├── direct/          # 直连模式（JM加密解密）
│   │   └── saucenao/        # 以图搜图
│   ├── download/            # 下载管理
│   ├── dto/                 # 数据传输对象
│   ├── local/               # 本地存储（DataStore封装）
│   └── repository/          # 数据仓库（JMRepository）
├── notification/            # 通知服务（稍后再看）
├── ui/
│   ├── components/          # UI组件
│   ├── nav/                 # 导航
│   ├── screen/              # 各功能屏幕（13个子包）
│   │   ├── author/          # 作者页
│   │   ├── comment/         # 评论区
│   │   ├── detail/          # 详情页
│   │   ├── downloads/       # 下载管理
│   │   ├── favorites/       # 收藏（本地+云端）
│   │   ├── forum/           # 讨论区
│   │   ├── history/         # 浏览历史
│   │   ├── home/            # 首页（最新/排行/随机）
│   │   ├── imagesearch/     # 以图搜图
│   │   ├── lock/            # 应用锁
│   │   ├── logs/            # 日志查看
│   │   ├── reader/          # 阅读器
│   │   ├── search/          # 搜索
│   │   └── settings/        # 设置
│   ├── theme/               # 主题系统
│   └── viewmodel/           # ViewModel（只有BaseListViewModel）
├── JMApp.kt                 # Application类
└── MainActivity.kt          # 主Activity
```

### 代码规模
- **Kotlin文件**: 56个
- **代码行数**: 约15000+ LOC
- **核心模块**: 已有基础架构，但有很大优化空间

### 现有问题
1. **架构不完整**: 只有少数 ViewModel，大量状态在 Composable 中管理
2. **没有依赖注入**: 全局 AppContainer 手动管理依赖
3. **缺少测试**: 无单元测试/UI测试
4. **错误处理**: Resource 封装良好，但 UI 层处理不统一
5. **性能问题**: 代码注释显示已优化过多轮卡顿（derivedStateOf、graphicsLayer等）
6. **代码重复**: BaseListViewModel 复用，但其他部分有重复逻辑
7. **直连模式复杂**: JM加密解密逻辑硬编码，难以维护

---

## 🎯 重构目标

### 1. 架构升级 (Architecture)
- ✅ 现有: 部分 MVVM + Repository
- 🎯 目标: 完整的 Clean Architecture + MVI
- 📦 方案:
  - 引入 **Hilt** 依赖注入
  - 重构为 **三层架构**: Presentation (UI + ViewModel) → Domain (UseCases) → Data (Repository + DataSource)
  - 实现 **MVI 模式**: UiState + UiEvent + UiEffect，单向数据流
  - 每个功能模块独立 ViewModel

### 2. 依赖更新 (Dependencies)
- 🎯 升级到最新稳定版本
- 📦 新增依赖:
  - **Hilt** (依赖注入)
  - **Room** (本地数据库，替换部分 DataStore)
  - **Paging 3** (分页加载)
  - **Ktor Client** (替代 Retrofit，更 Kotlin-native)
  - **Kotlinx Serialization** (替代 Moshi，性能更好)
  - **Turbine** (Flow 测试)
  - **MockK** (Mock 框架)

### 3. 模块化 (Modularization)
- 🎯 拆分为多个 Gradle 模块
- 📦 模块结构:
  ```
  :app                      # 主应用模块
  :core:common              # 通用工具类
  :core:network             # 网络层（Ktor + 直连客户端）
  :core:database            # 数据库（Room）
  :core:datastore           # DataStore封装
  :core:ui                  # UI组件库
  :feature:home             # 首页功能
  :feature:search           # 搜索功能
  :feature:detail           # 详情页功能
  :feature:reader           # 阅读器功能
  :feature:favorites        # 收藏功能
  :feature:settings         # 设置功能
  ```

### 4. 性能优化 (Performance)
- ✅ 已优化: derivedStateOf、graphicsLayer、去重 effect
- 🎯 进一步优化:
  - **图片加载**: 实现多级缓存（内存 + 磁盘 + HTTP）
  - **列表性能**: LazyList key 优化、Paging 3 集成
  - **启动速度**: App Startup、ProGuard/R8 优化
  - **内存优化**: LeakCanary 检测、图片压缩
  - **网络优化**: HTTP/3、连接池复用、预加载

### 5. 代码质量 (Code Quality)
- 🎯 测试覆盖率 > 70%
- 📦 方案:
  - **单元测试**: ViewModel + UseCase + Repository (JUnit5 + Turbine + MockK)
  - **UI测试**: Compose UI Test
  - **集成测试**: Room + Network (Fake/Mock)
  - **静态分析**: Detekt + ktlint
  - **Code Review**: GitHub Actions自动检查

### 6. 功能增强 (Features)
- 🎯 新增功能:
  - **离线模式**: 完整的离线浏览（Room缓存）
  - **智能推荐**: 基于浏览历史的个性化推荐
  - **多账号支持**: 切换不同账号
  - **深色主题增强**: AMOLED黑、自动切换
  - **手势操作**: 侧滑返回、下拉刷新统一
  - **无障碍支持**: TalkBack、字体缩放
  - **Widget**: 桌面小组件
  - **快捷方式**: App Shortcuts

### 7. UI/UX优化 (Design)
- 🎯 Material 3 全面应用
- 📦 方案:
  - **动画**: Shared Element Transition（详情进入/退出）
  - **加载态**: Shimmer/Skeleton 效果
  - **空状态**: 精美空状态插图
  - **错误处理**: 友好的错误提示 + 重试
  - **手势反馈**: 触摸涟漪、震动反馈
  - **字体**: 自定义字体支持

---

## 📋 详细执行计划

### Phase 1: 基础设施重构 (第1-2周)

#### 1.1 依赖注入 - Hilt
- [ ] 添加 Hilt 依赖和插件
- [ ] 创建 Application 模块 (@HiltAndroidApp)
- [ ] 重构 AppContainer → Hilt Modules
- [ ] 迁移所有依赖到 Hilt (Repository, DataStore, NetworkClient)
- [ ] ViewModel 改用 @HiltViewModel

#### 1.2 数据层重构 - Room
- [ ] 设计数据库 Schema
  - ComicEntity (漫画信息)
  - ChapterEntity (章节)
  - FavoriteEntity (本地收藏)
  - HistoryEntity (浏览历史)
  - DownloadEntity (下载记录)
  - TagEntity (标签/分类)
- [ ] 创建 DAO 接口
- [ ] 实现数据库迁移策略
- [ ] 替换现有 DataStore 实现（保留 Settings 用 DataStore）

#### 1.3 网络层重构 - Ktor
- [ ] 引入 Ktor Client
- [ ] 重构 NetworkFactory → Ktor HttpClient
- [ ] 迁移 Retrofit 接口到 Ktor Resources
- [ ] 实现请求拦截器（日志、鉴权、重试）
- [ ] Kotlinx Serialization 替代 Moshi

#### 1.4 模块化初步
- [ ] 创建 :core:common 模块（工具类）
- [ ] 创建 :core:network 模块（网络层）
- [ ] 创建 :core:database 模块（Room）
- [ ] 创建 :core:datastore 模块（Settings）
- [ ] 创建 :core:ui 模块（共享 UI 组件）

### Phase 2: 架构升级 - Domain Layer (第3-4周)

#### 2.1 定义领域模型
- [ ] ComicModel (替代 ComicBriefDto/ComicDetailDto)
- [ ] ChapterModel
- [ ] UserModel
- [ ] CommentModel
- [ ] 创建 Mapper (Dto ↔ Entity ↔ Model)

#### 2.2 实现 UseCases
- [ ] GetLatestComicsUseCase
- [ ] SearchComicsUseCase
- [ ] GetComicDetailUseCase
- [ ] GetChapterImagesUseCase
- [ ] AddToFavoritesUseCase
- [ ] RemoveFromFavoritesUseCase
- [ ] GetFavoritesUseCase
- [ ] LoginUseCase / LogoutUseCase
- [ ] GetCommentsUseCase
- [ ] DownloadComicUseCase

#### 2.3 Repository 重构
- [ ] ComicRepository (网络 + 本地)
- [ ] UserRepository (登录/账号)
- [ ] FavoriteRepository (收藏同步)
- [ ] HistoryRepository (浏览历史)
- [ ] DownloadRepository (下载管理)
- [ ] 实现数据同步策略（网络优先/缓存优先）

### Phase 3: 表现层重构 - ViewModels (第5-6周)

#### 3.1 MVI 架构实现
- [ ] 定义 BaseViewModel (UiState + UiEvent + UiEffect)
- [ ] 实现 StateFlow + Channel 模式
- [ ] 错误处理统一封装

#### 3.2 各功能 ViewModel
- [ ] HomeViewModel (最新/排行/随机)
- [ ] SearchViewModel (搜索 + Paging)
- [ ] DetailViewModel (详情 + 收藏 + 下载)
- [ ] ReaderViewModel (阅读器 + 进度)
- [ ] FavoritesViewModel (本地+云端收藏)
- [ ] HistoryViewModel (浏览历史)
- [ ] SettingsViewModel (设置)
- [ ] CommentViewModel (评论区)
- [ ] DownloadViewModel (下载管理)
- [ ] LoginViewModel (登录/注册)

#### 3.3 Paging 3 集成
- [ ] 实现 PagingSource (Latest/Search/Ranking)
- [ ] RemoteMediator (网络 + 本地缓存)
- [ ] UI 层使用 collectAsLazyPagingItems

### Phase 4: UI/UX 优化 (第7-8周)

#### 4.1 组件库完善
- [ ] ComicCard (统一封装)
- [ ] LoadingState (Shimmer)
- [ ] EmptyState (空状态)
- [ ] ErrorState (错误提示 + 重试)
- [ ] ImageViewer (图片查看器)
- [ ] ChipGroup (标签组)
- [ ] FilterBar (筛选栏)

#### 4.2 动画效果
- [ ] Shared Element Transition (列表→详情)
- [ ] 页面切换动画
- [ ] 下拉刷新动画
- [ ] 加载动画统一

#### 4.3 Material 3 深度应用
- [ ] Dynamic Color 优化
- [ ] 自定义 ColorScheme
- [ ] Typography Scale
- [ ] Shape System
- [ ] Motion System

#### 4.4 无障碍支持
- [ ] Semantics 添加
- [ ] TalkBack 测试
- [ ] 字体缩放适配
- [ ] 对比度检查

### Phase 5: 功能增强 (第9-10周)

#### 5.1 离线模式
- [ ] 完整离线浏览支持
- [ ] 离线标记 UI
- [ ] 离线数据同步策略

#### 5.2 智能推荐
- [ ] 浏览历史分析
- [ ] 相似内容推荐算法
- [ ] 推荐页面

#### 5.3 多账号支持
- [ ] 账号切换 UI
- [ ] 多账号数据隔离
- [ ] 账号同步

#### 5.4 Widget & Shortcuts
- [ ] 首页 Widget
- [ ] 收藏 Widget
- [ ] App Shortcuts (搜索/收藏/历史)

### Phase 6: 测试 & 质量 (第11-12周)

#### 6.1 单元测试
- [ ] ViewModel 测试 (70%+ 覆盖率)
- [ ] UseCase 测试
- [ ] Repository 测试 (Fake实现)
- [ ] Mapper 测试
- [ ] Util 测试

#### 6.2 UI 测试
- [ ] Compose UI Test
- [ ] Navigation 测试
- [ ] 截图测试 (Paparazzi)

#### 6.3 集成测试
- [ ] Room 测试
- [ ] Network 测试 (MockWebServer)
- [ ] End-to-End 测试

#### 6.4 代码质量
- [ ] Detekt 集成
- [ ] ktlint 集成
- [ ] Baseline 建立
- [ ] CI/CD 集成

### Phase 7: 性能优化 & 发布 (第13-14周)

#### 7.1 性能优化
- [ ] App Startup 优化
- [ ] ProGuard/R8 配置
- [ ] Baseline Profile
- [ ] LeakCanary 泄漏检测
- [ ] Macrobenchmark 性能测试

#### 7.2 监控 & 分析
- [ ] Crashlytics (可选)
- [ ] Analytics (可选)
- [ ] Performance Monitoring

#### 7.3 文档 & 发布
- [ ] API 文档
- [ ] 架构文档
- [ ] 贡献指南
- [ ] CHANGELOG
- [ ] Release APK

---

## 📦 依赖升级清单

### 当前版本 → 目标版本

```toml
[versions]
# 构建工具
agp = "8.5.0" → "8.7.3"
kotlin = "2.1.0" → "2.1.0" (最新)

# AndroidX 核心
coreKtx = "1.15.0" → "1.15.0" (最新)
lifecycle = "2.8.7" → "2.8.7" (最新)
activityCompose = "1.9.3" → "1.9.3" (最新)
composeBom = "2024.12.01" → "2024.12.01" (最新)

# 导航
navigation = "2.8.5" → "2.8.5" (最新)

# 图片加载
coil = "2.7.0" → "3.0.4" (Coil 3 - 重大升级)

# 网络层 (新方案)
ktor = "3.0.2" (新增 - 替代 Retrofit)
kotlinxSerialization = "1.7.3" (新增 - 替代 Moshi)

# 或继续使用 Retrofit (升级)
retrofit = "2.11.0" → "2.11.0" (最新)
okhttp = "4.12.0" → "5.0.0-alpha.14" (OkHttp 5)
moshi = "1.15.1" → "1.15.1" (最新)

# 依赖注入 (新增)
hilt = "2.54" (新增)
hiltNavigationCompose = "1.2.0" (新增)

# 数据库 (新增)
room = "2.7.0-alpha12" (新增)

# 分页 (新增)
paging = "3.3.5" (新增)
pagingCompose = "3.3.5" (新增)

# 测试 (新增)
junit5 = "5.11.4"
turbine = "1.2.0"
mockk = "1.13.14"
composeUiTest = composeBom
robolectric = "4.14.1"
mockWebServer = "5.0.0-alpha.14"

# 代码质量 (新增)
detekt = "1.23.7"
ktlint = "12.1.2"

# 性能 (新增)
leakcanary = "3.0-beta-1"
```

### 新增库说明

1. **Hilt** - 依赖注入，Google官方推荐，比手动管理更安全
2. **Room** - 类型安全的SQL数据库，替代部分DataStore，支持复杂查询
3. **Ktor** - Kotlin-native网络库，性能更好，API更简洁（可选，也可保留Retrofit）
4. **Paging 3** - 官方分页库，自动处理加载状态、错误重试
5. **Kotlinx Serialization** - Kotlin官方序列化，编译时生成代码，性能优于Moshi
6. **Coil 3** - 最新版本，性能提升，API更简洁
7. **OkHttp 5** - 支持HTTP/3、性能优化
8. **Turbine** - Flow测试利器，简化异步测试
9. **MockK** - Kotlin专用Mock框架，比Mockito更好用

---

## 🔄 迁移策略

### 渐进式迁移原则
1. **向后兼容**: 旧代码继续工作，逐步迁移
2. **功能独立**: 每个功能模块独立迁移，互不影响
3. **测试先行**: 迁移前写测试，保证行为一致
4. **文档同步**: 迁移时更新文档

### 迁移顺序
1. **Settings** (最简单) → 验证架构可行性
2. **Home** (核心功能) → 建立完整范例
3. **Search** → 集成Paging 3
4. **Detail** → 复杂状态管理
5. **Reader** → 性能优化重点
6. **其他功能** → 批量迁移

### 风险控制
- 每个 Phase 结束构建可运行的 APK
- 关键功能保留降级方案
- 性能回归测试
- 用户反馈收集

---

## ✅ 成功指标

### 代码质量
- [ ] 测试覆盖率 > 70%
- [ ] 0 Detekt 严重问题
- [ ] 0 Memory Leak
- [ ] CI/CD 全绿

### 性能
- [ ] 冷启动时间 < 2s
- [ ] 列表滚动 60fps
- [ ] 图片加载时间 < 1s
- [ ] APK 大小 < 20MB

### 架构
- [ ] 100% 依赖注入覆盖
- [ ] 所有功能模块化
- [ ] 单向数据流（MVI）
- [ ] 完整的 Clean Architecture

### 用户体验
- [ ] Material 3 Design
- [ ] 无障碍得分 > 90%
- [ ] 0 崩溃
- [ ] 流畅的动画和过渡

---

## 📝 备注

### 保留的优秀设计
- ✅ Resource 封装（Success/Error/Loading）
- ✅ 直连模式的加密解密逻辑（核心竞争力）
- ✅ 性能优化（derivedStateOf、graphicsLayer等）
- ✅ 主题系统（动态颜色、自定义配色）
- ✅ 应用锁（生物识别 + PIN）
- ✅ 稍后再看通知栏

### 需要特别注意
- ⚠️ JM加密逻辑（JMCrypto、JmImageDecoder）需完整测试
- ⚠️ 网络切换（直连/后端模式）逻辑保持稳定
- ⚠️ 下载管理器（DownloadManager）迁移需谨慎
- ⚠️ 用户数据迁移（DataStore → Room）需无损

### 可选增强
- 🎯 Jetpack Compose Multiplatform (未来支持 Desktop/iOS)
- 🎯 WebView 集成（内嵌浏览器）
- 🎯 分享功能增强（生成海报）
- 🎯 更多主题（节日主题、IP联动）

---

**预计总时长**: 14周（约3.5个月）
**每周投入**: 20-30小时
**最终成果**: 企业级 Android 应用架构，可作为开源项目范例

现在准备开始执行！🚀
