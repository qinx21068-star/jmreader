# Hilt 依赖注入迁移指南

## 概述

本文档记录了将 JM Reader 从手动依赖管理（AppContainer）迁移到 Hilt 依赖注入的过程。

## 架构变化

### 迁移前
```kotlin
// 手动创建依赖
class JMApp : Application() {
    lateinit var container: AppContainer
    
    override fun onCreate() {
        container = AppContainer(this)
    }
}

// ViewModel 手动获取依赖
class MyViewModel(private val container: AppContainer) : ViewModel() {
    private val repository = container.repository
}

// UI 层手动创建 ViewModel
val viewModel = remember { MyViewModel(JMApp.instance.container) }
```

### 迁移后
```kotlin
// Hilt 自动管理依赖
@HiltAndroidApp
class JMApp : Application() {
    @Inject
    lateinit var container: AppContainer  // 过渡期保留
}

// ViewModel 通过构造函数注入
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: JMRepository,
    private val settingsStore: SettingsStore
) : ViewModel()

// UI 层使用 Hilt
@AndroidEntryPoint
class MainActivity : FragmentActivity()

@Composable
fun MyScreen() {
    val viewModel: MyViewModel = hiltViewModel()
}
```

## 迁移步骤

### Step 1: 添加 Hilt 依赖 ✅

已在 `libs.versions.toml` 和 `app/build.gradle.kts` 中配置。

### Step 2: 创建 Hilt 模块 ✅

创建了三个模块：
- `AppModule.kt` - 提供 AppContainer（过渡方案）
- `NetworkModule.kt` - 提供网络层依赖
- `DataModule.kt` - 提供数据层依赖（Repository、DataStore 等）

### Step 3: 标注 Application 和 Activity ✅

- `JMApp` 添加 `@HiltAndroidApp`
- `MainActivity` 添加 `@AndroidEntryPoint`

### Step 4: 迁移 ViewModel

#### 示例：SettingsViewModel

**迁移前**（假设存在）：
```kotlin
class SettingsViewModel(container: AppContainer) : ViewModel() {
    private val settingsStore = container.settingsStore
    val settings = settingsStore.settings.stateIn(...)
}

// 使用
val viewModel = remember { SettingsViewModel(JMApp.instance.container) }
```

**迁移后**：
```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsStore: SettingsStore
) : ViewModel() {
    val settings = settingsStore.settings.stateIn(...)
}

// 使用
val viewModel: SettingsViewModel = hiltViewModel()
```

### Step 5: 迁移 BaseListViewModel

BaseListViewModel 是所有列表页的基类，需要特殊处理：

**当前实现**：
```kotlin
abstract class BaseListViewModel(protected val container: AppContainer) : ViewModel()

// 子类
class HomeViewModel(container: AppContainer) : BaseListViewModel(container)
```

**迁移方案 A - 直接注入依赖**（推荐）：
```kotlin
abstract class BaseListViewModel(
    protected val repository: JMRepository,
    protected val blockedTagsStore: BlockedTagsStore,
    protected val comicTagsCache: ComicTagsCache,
    protected val settingsStore: SettingsStore
) : ViewModel()

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: JMRepository,
    blockedTagsStore: BlockedTagsStore,
    comicTagsCache: ComicTagsCache,
    settingsStore: SettingsStore
) : BaseListViewModel(repository, blockedTagsStore, comicTagsCache, settingsStore)
```

**迁移方案 B - 保留 AppContainer**（过渡方案）：
```kotlin
abstract class BaseListViewModel(protected val container: AppContainer) : ViewModel()

@HiltViewModel
class HomeViewModel @Inject constructor(
    container: AppContainer
) : BaseListViewModel(container)
```

### Step 6: 移除静态访问

逐步移除所有 `JMApp.instance.container` 的直接访问：

**迁移前**：
```kotlin
@Composable
fun MyScreen() {
    val container = JMApp.instance.container
    val settings = container.settingsStore.settings.collectAsState()
}
```

**迁移后**：
```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel()
) {
    val settings = viewModel.settings.collectAsState()
}
```

## 迁移优先级

### 高优先级（立即迁移）
1. ✅ SettingsViewModel - 已创建示例
2. 新创建的 ViewModel - 直接使用 Hilt

### 中优先级（Phase 1 完成前）
3. HomeViewModel
4. SearchViewModel
5. DetailViewModel
6. ReaderViewModel

### 低优先级（Phase 2+）
7. 其他功能 ViewModel
8. 完全移除 AppContainer

## 常见问题

### Q1: 如何在 Composable 中获取注入的依赖？

**A**: 使用 `hiltViewModel()` 获取 ViewModel，所有依赖通过 ViewModel 提供：

```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel()
) {
    val data = viewModel.data.collectAsState()
}
```

### Q2: 非 ViewModel 的类如何使用 Hilt？

**A**: 使用 `@Inject` 标注构造函数：

```kotlin
class MyRepository @Inject constructor(
    private val api: JMApi,
    private val dataStore: SettingsStore
) {
    // ...
}
```

### Q3: 如何注入 Context？

**A**: 使用 `@ApplicationContext` 注解：

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel()
```

### Q4: 为什么保留 AppContainer？

**A**: 过渡方案。现有代码依赖 AppContainer，一次性移除风险太大。通过 Hilt 管理 AppContainer 的生命周期，然后逐步迁移内部依赖。

### Q5: Hilt 生成的代码在哪里？

**A**: `app/build/generated/hilt/` 目录下。Hilt 使用 KSP（Kotlin Symbol Processing）在编译时生成依赖注入代码。

## 验证清单

构建成功后，验证以下功能：

- [ ] App 启动正常
- [ ] MainActivity 显示正常
- [ ] SettingsViewModel 可以通过 hiltViewModel() 获取
- [ ] 设置页面功能正常
- [ ] 网络请求正常（通过 container.repository）
- [ ] 现有功能未受影响

## 性能考虑

Hilt 的性能影响：
- **编译时间**：首次构建增加 10-20 秒（KSP 生成代码）
- **运行时性能**：几乎无影响（生成的是普通 Kotlin 代码）
- **APK 大小**：增加约 50-100 KB（Hilt 运行时库）
- **启动速度**：略微提升（依赖图在编译时确定，无需运行时反射）

## 下一步

1. 验证构建成功
2. 在 SettingsScreen 中使用 hiltViewModel() 测试
3. 迁移 HomeViewModel
4. 迁移 BaseListViewModel
5. 逐步移除 AppContainer 内部依赖

## 参考资源

- [Hilt 官方文档](https://dagger.dev/hilt/)
- [Android Hilt 指南](https://developer.android.com/training/dependency-injection/hilt-android)
- [Hilt 与 ViewModel](https://developer.android.com/training/dependency-injection/hilt-jetpack)

---

最后更新: v28.0 - Hilt 初始集成
