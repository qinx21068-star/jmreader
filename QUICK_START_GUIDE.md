# 🎯 快速开始指南 - Phase 1.1 后续工作

## 当前状态

✅ **Phase 1.1 核心实施已完成（90%）**

Hilt 依赖注入的基础设施已经就绪，所有必要的代码和文档都已创建。只需要网络连接完成构建验证，就可以进入实际的 ViewModel 迁移阶段。

---

## 📋 待完成清单

### 1. 构建验证（需要网络）

```bash
# 方案 A: 使用国内镜像源
# 编辑 build.gradle.kts，在 repositories 中添加：
repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    google()
    mavenCentral()
}

# 方案 B: 直接构建（如果网络恢复）
./gradlew assembleDebug

# 方案 C: 检查构建配置
./gradlew dependencies --configuration debugRuntimeClasspath
```

### 2. 验证 Hilt 注入

创建测试代码验证 Hilt 是否正常工作：

```kotlin
// 在 MainActivity 中测试
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    
    @Inject
    lateinit var repository: JMRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Hilt", "Repository injected: ${repository != null}")
        // 应该输出: Repository injected: true
    }
}
```

### 3. 在 UI 中使用 SettingsViewModel

```kotlin
// 在 SettingsScreen.kt 或相关的设置页面
@Composable
fun SettingsScreen() {
    val viewModel: SettingsViewModel = hiltViewModel()
    val settings by viewModel.settings.collectAsState()
    
    Column {
        Text("Theme: ${settings.themeMode}")
        Text("Dynamic Color: ${settings.dynamicColor}")
        
        Button(onClick = { 
            viewModel.viewModelScope.launch {
                viewModel.updateDynamicColor(!settings.dynamicColor)
            }
        }) {
            Text("Toggle Dynamic Color")
        }
    }
}
```

---

## 🔄 迁移 ViewModel 的步骤

### 示例：迁移 HomeViewModel

**步骤 1: 读取现有代码**
```bash
# 找到 HomeViewModel
find app/src/main/java -name "*HomeViewModel*"
```

**步骤 2: 添加 Hilt 注解**
```kotlin
// 之前
class HomeViewModel(container: AppContainer) : BaseListViewModel(container) {
    // ...
}

// 之后
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: JMRepository,
    private val settingsStore: SettingsStore,
    private val blockedTagsStore: BlockedTagsStore
) : ViewModel() {
    // ...
}
```

**步骤 3: 更新 UI 使用**
```kotlin
// 之前
val viewModel = remember { HomeViewModel(JMApp.instance.container) }

// 之后
val viewModel: HomeViewModel = hiltViewModel()
```

**步骤 4: 测试功能**
- 运行 App
- 进入首页
- 验证数据加载正常
- 验证所有交互正常

---

## 📝 迁移检查清单

每迁移一个 ViewModel，使用此清单验证：

- [ ] 添加 `@HiltViewModel` 注解
- [ ] 添加 `@Inject` 构造函数
- [ ] 移除 `container` 参数
- [ ] 直接注入所需依赖
- [ ] 更新 UI 使用 `hiltViewModel()`
- [ ] 移除手动创建代码
- [ ] 编译通过
- [ ] 运行测试
- [ ] 功能验证通过
- [ ] 提交代码

---

## 🎯 优先级列表

按以下顺序迁移 ViewModel：

### 高优先级（本周）
1. ✅ **SettingsViewModel** - 已创建示例
2. ⏳ **HomeViewModel** - 首页，用户最常用
3. ⏳ **SearchViewModel** - 搜索功能

### 中优先级（下周）
4. ⏳ **DetailViewModel** - 漫画详情页
5. ⏳ **ReaderViewModel** - 阅读器
6. ⏳ **BaseListViewModel** - 基类（需要特殊处理）

### 低优先级（Phase 1 后期）
7. 其他功能 ViewModel
8. 完全移除 AppContainer

---

## 🛠️ 常用命令

```bash
# 清理构建
./gradlew clean

# 构建 Debug 版本
./gradlew assembleDebug

# 运行测试
./gradlew test

# 检查依赖
./gradlew dependencies

# 查看 Hilt 生成的代码
ls -la app/build/generated/ksp/debug/kotlin/

# 搜索 ViewModel 文件
find app/src/main/java -name "*ViewModel.kt"

# 统计代码行数
find app/src/main/java -name "*.kt" | xargs wc -l

# 检查 TODO
grep -r "TODO" app/src/main/java
```

---

## 📚 参考文档

### 项目内部文档
- [HILT_MIGRATION_GUIDE.md](./HILT_MIGRATION_GUIDE.md) - 详细迁移指南
- [REFACTORING_PROGRESS.md](./REFACTORING_PROGRESS.md) - 实时进度追踪
- [PHASE_1_1_SUMMARY.md](./PHASE_1_1_SUMMARY.md) - Phase 1.1 总结
- [PROJECT_STATUS_REPORT.md](./PROJECT_STATUS_REPORT.md) - 完整状态报告

### 外部资源
- [Hilt 官方文档](https://dagger.dev/hilt/)
- [Android Hilt 指南](https://developer.android.com/training/dependency-injection/hilt-android)
- [Hilt ViewModel](https://developer.android.com/training/dependency-injection/hilt-jetpack#viewmodels)

---

## 🐛 常见问题排查

### 问题 1: 构建失败 - 无法找到 Hilt 生成的类

**原因**: KSP 没有运行或生成失败

**解决**:
```bash
# 清理并重新构建
./gradlew clean
./gradlew kspDebugKotlin
./gradlew assembleDebug
```

### 问题 2: 运行时错误 - 无法注入依赖

**原因**: 忘记添加 `@AndroidEntryPoint`

**解决**: 确保 Activity/Fragment 有正确的注解
```kotlin
@AndroidEntryPoint
class MainActivity : FragmentActivity()
```

### 问题 3: ViewModel 无法获取

**原因**: 忘记添加 `@HiltViewModel`

**解决**: 
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(...) : ViewModel()
```

### 问题 4: 循环依赖

**原因**: Module 之间相互依赖

**解决**: 重新设计依赖关系，或使用 `@Lazy` 延迟注入

---

## 🎓 最佳实践

### 1. 单一职责
每个 Module 只负责一类依赖：
- AppModule: Application 级别
- NetworkModule: 网络层
- DataModule: 数据层

### 2. 命名规范
```kotlin
// Module 名称
AppModule, NetworkModule, DataModule

// Provider 方法
provide + 类名
provideRepository(), provideSettingsStore()
```

### 3. 作用域使用
```kotlin
@Singleton              // Application 级别单例
@ActivityScoped         // Activity 级别
@ViewModelScoped        // ViewModel 级别（Hilt 自动管理）
```

### 4. 测试友好
```kotlin
// 为测试创建单独的 Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class]
)
object FakeDataModule {
    @Provides
    fun provideRepository(): JMRepository = FakeRepository()
}
```

---

## 🚀 启动迁移

**准备就绪！** 当网络恢复后，执行以下步骤：

1. ✅ 完成 Gradle 构建
2. ✅ 运行 App 验证启动
3. ✅ 测试 SettingsViewModel
4. ✅ 迁移 HomeViewModel
5. ✅ 逐个迁移其他 ViewModel

**每完成一个 ViewModel，更新 REFACTORING_PROGRESS.md 中的进度！**

---

## 📞 需要帮助？

遇到问题时：

1. 查看 [HILT_MIGRATION_GUIDE.md](./HILT_MIGRATION_GUIDE.md) 的常见问题部分
2. 检查 Hilt 官方文档
3. 查看已创建的示例代码（SettingsViewModel）
4. 记录问题和解决方案，更新文档

---

**让我们继续完成这个伟大的重构之旅！** 🎯
