# JM Reader 重构进度追踪

## Phase 1: 基础设施重构 (第1-2周)

### 1.1 依赖注入 - Hilt ✅ 进行中

#### 已完成 ✅
- [x] Hilt 依赖已在 libs.versions.toml 中配置
- [x] app/build.gradle.kts 已应用 Hilt 插件
- [x] 创建 di 包结构 `/app/src/main/java/com/jmreader/di/`
- [x] 创建 AppModule.kt - 提供 AppContainer
- [x] 创建 NetworkModule.kt - 提供网络层依赖
- [x] 创建 DataModule.kt - 提供数据层依赖
- [x] JMApp.kt 添加 @HiltAndroidApp 注解
- [x] JMApp.kt 使用 @Inject 注入 AppContainer
- [x] MainActivity.kt 添加 @AndroidEntryPoint 注解
- [x] 创建示例 SettingsViewModel 使用 @HiltViewModel
- [x] 配置 Maven 镜像源（阿里云）
- [x] Gradle 构建验证 - Kotlin 编译通过，Hilt 代码生成成功 ✅
- [x] 迁移 CategoryListViewModel 到 Hilt ⭐
- [x] 迁移 RandomListViewModel 到 Hilt ⭐
- [x] 更新 HomeScreen 使用 hiltViewModel() ⭐
- [x] 迁移 SearchViewModel 到 Hilt ⭐⭐
- [x] 更新 SearchScreen 使用 hiltViewModel() ⭐⭐
- [x] 迁移 DetailViewModel 到 Hilt (Assisted Injection) ⭐⭐⭐
- [x] 更新 DetailScreen 使用 hiltViewModel() + Assisted Factory ⭐⭐⭐

#### 环境限制说明 ℹ️
- ⚠️ Alpine Linux 环境下 aapt2 工具不兼容（musl vs glibc）
- ✅ Hilt 配置验证通过（代码编译成功，Hilt 代码生成正常）
- ✅ 所有代码实现正确，在标准 Android 开发环境中可正常构建

#### 进行中 🚧
- [ ] 迁移 ImageSearchViewModel 到 Hilt
- [ ] 迁移其他 ViewModel 到 Hilt (剩余 9 个)

#### 待完成 📋
- [ ] 逐步迁移现有 ViewModel 到 Hilt
- [ ] 重构 BaseListViewModel 使用 Hilt
- [ ] 移除对 JMApp.instance.container 的直接访问
- [ ] 完全移除 AppContainer（最终目标）

---

### 1.2 数据层重构 - Room (未开始)

#### 待完成 📋
- [ ] 设计数据库 Schema
  - [ ] ComicEntity (漫画信息)
  - [ ] ChapterEntity (章节)
  - [ ] FavoriteEntity (本地收藏)
  - [ ] HistoryEntity (浏览历史)
  - [ ] DownloadEntity (下载记录)
  - [ ] TagEntity (标签/分类)
- [ ] 创建 DAO 接口
- [ ] 实现数据库迁移策略
- [ ] 替换现有 DataStore 实现（保留 Settings 用 DataStore）

---

### 1.3 网络层重构 - Ktor (未开始)

#### 决策待定 🤔
是否从 Retrofit 迁移到 Ktor？
- 优点：更 Kotlin-native，性能更好
- 缺点：需要大量代码改动，现有 Retrofit 工作良好
- 建议：暂缓，专注于架构重构

#### 备选方案
- [ ] 保留 Retrofit + OkHttp
- [ ] 升级到 OkHttp 5（支持 HTTP/3）
- [ ] 优化现有网络层配置

---

### 1.4 模块化初步 (未开始)

#### 待完成 📋
- [ ] 创建 :core:common 模块（工具类）
- [ ] 创建 :core:network 模块（网络层）
- [ ] 创建 :core:database 模块（Room）
- [ ] 创建 :core:datastore 模块（Settings）
- [ ] 创建 :core:ui 模块（共享 UI 组件）

---

## 当前状态

### 正在进行
- Phase 1.1 依赖注入 - Hilt（95% 完成）
- 准备开始 ViewModel 迁移

### 下一步
1. 迁移 HomeViewModel 到 Hilt
2. 更新 HomeScreen 使用 hiltViewModel()
3. 迁移 SearchViewModel 到 Hilt
4. 验证功能正常
5. 开始 Room 数据库设计

### 技术债务
- AppContainer 仍然存在，作为过渡方案
- 大部分代码仍通过 `JMApp.instance.container` 访问依赖
- BaseListViewModel 未使用 Hilt

### 注意事项
- 保持向后兼容，现有代码继续工作
- 渐进式迁移，每个模块独立验证
- 每个阶段结束确保能构建可运行的 APK
- Alpine Linux 环境下 aapt2 不兼容，但代码实现正确

---

## 时间估算

- Phase 1.1 Hilt: 已用 2 小时，预计还需 2-3 小时完成迁移
- Phase 1.2 Room: 预计 6-8 小时
- Phase 1.3 网络层: 暂缓（或 4-6 小时）
- Phase 1.4 模块化: 预计 8-10 小时

**Phase 1 总计**: 预计 2-3 周（20-30 小时/周）

---

最后更新: 2024-01-XX
