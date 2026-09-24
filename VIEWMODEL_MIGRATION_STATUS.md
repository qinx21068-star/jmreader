# ViewModel Migration Status - v28.0

## Overview
开始将 ViewModel 迁移到 Hilt 依赖注入，遵循渐进式迁移策略。

## Phase 1.1 ViewModel Migration Progress

### ✅ 已完成 (4/13)

#### 1. HomeViewModel (CategoryListViewModel + RandomListViewModel)
- **文件**: `app/src/main/java/com/jmreader/ui/screen/home/HomeViewModel.kt`
- **状态**: ✅ 完成
- **修改内容**:
  - 添加 `@HiltViewModel` 注解
  - 使用 `@Inject constructor` 注入 AppContainer
  - 保持继承 `BaseListViewModel(container)`（渐进式策略）
- **UI 更新**: `HomeScreen.kt` 已更新使用 `hiltViewModel()`
- **迁移时间**: 2026-09-24

#### 2. SearchViewModel
- **文件**: `app/src/main/java/com/jmreader/ui/screen/search/SearchViewModel.kt`
- **状态**: ✅ 完成
- **修改内容**:
  - 提取到独立文件 SearchViewModel.kt
  - 添加 `@HiltViewModel` 注解
  - 使用 `@Inject constructor` 注入 AppContainer
  - 保持继承 `BaseListViewModel(container)`
  - 保留所有业务逻辑：普通搜索、批量ID搜索、搜索历史
- **UI 更新**: `SearchScreen.kt` 已更新使用 `hiltViewModel()`
- **迁移时间**: 2026-09-24

#### 3. DetailViewModel
- **文件**: `app/src/main/java/com/jmreader/ui/screen/detail/DetailViewModel.kt`
- **状态**: ✅ 完成
- **修改内容**:
  - 提取到独立文件 DetailViewModel.kt (14 KB, 370+ 行)
  - 使用 `@HiltViewModel` + `@AssistedInject` 支持运行时参数
  - 定义 `@AssistedFactory` 接口
  - 保留所有业务逻辑：详情加载、收藏、屏蔽、相似推荐、作者作品
- **UI 更新**: `DetailScreen.kt` 已更新使用 `hiltViewModel()` + Assisted Factory
- **技术亮点**: 首个使用 Assisted Injection 的 ViewModel
- **迁移时间**: 2026-09-24

### 🚧 进行中

#### 4. ImageSearchViewModel

### 📋 待迁移列表 (11个)

继承自 BaseListViewModel 的 ViewModel：
1. ✅ CategoryListViewModel (HomeScreen) - 已完成
2. ✅ RandomListViewModel (HomeScreen) - 已完成
3. ✅ SearchViewModel - 已完成
4. ⏳ ImageSearchViewModel
5. ⏳ FavoritesViewModel
6. ⏳ HistoryViewModel
7. ⏳ DownloadsViewModel
8. ⏳ AuthorWorksViewModel
9. ⏳ TagResultsViewModel
10. ⏳ CollectionViewModel
11. ⏳ RecommendationsViewModel
12. ⏳ 其他继承 BaseListViewModel 的 ViewModel

其他 ViewModel：
1. ✅ DetailViewModel - 已完成（使用 Assisted Injection）

## Migration Strategy

### 渐进式迁移 - 三阶段策略

#### Phase 1 (当前): 保持 AppContainer
- ViewModel 添加 `@HiltViewModel` 注解
- 构造函数注入 `AppContainer`
- 继续继承 `BaseListViewModel(container)`
- UI 层使用 `hiltViewModel()` 替换手动 Factory

**优势**: 
- 零破坏性变更
- 每个 ViewModel 独立迁移
- 随时可以停止/继续

#### Phase 2: BaseListViewModel 重构
- 重构 BaseListViewModel 直接注入所需依赖
- 移除对 AppContainer 的依赖
- 更新所有已迁移的 ViewModel

#### Phase 3: 完全移除 AppContainer
- 所有 ViewModel 迁移完成后
- 移除 AppContainer 的 Hilt Module
- 清理遗留代码

## Migration Checklist

### 每个 ViewModel 迁移步骤

1. **创建/修改 ViewModel 文件**
   - [ ] 添加 `@HiltViewModel` 注解
   - [ ] 添加 `@Inject constructor(container: AppContainer)`
   - [ ] 保持继承 `BaseListViewModel(container)`

2. **更新 UI Screen**
   - [ ] 导入 `androidx.hilt.navigation.compose.hiltViewModel`
   - [ ] 替换 `viewModel(factory = ...)` 为 `hiltViewModel()`
   - [ ] 移除手动创建的 ViewModelFactory
   - [ ] 保留必要的 `key` 参数（如果有多个实例）

3. **验证**
   - [ ] 代码编译通过
   - [ ] 功能测试正常
   - [ ] 更新文档

## Current Status

- **总体进度**: 4/13 (31%)
- **Phase 1.1 状态**: 进行中
- **下一步**: 迁移 ImageSearchViewModel 或其他 BaseListViewModel

## Notes

- BaseListViewModel 依然需要 AppContainer，暂时保持这个依赖
- 所有 ViewModel 都通过 Hilt 注入，但暂时还是注入 AppContainer
- Phase 1.2 会重构 BaseListViewModel 完全移除 AppContainer

---
**最后更新**: 2026-09-24  
**更新人**: Kiro
