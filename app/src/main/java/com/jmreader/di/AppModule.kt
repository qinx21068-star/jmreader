package com.jmreader.di

import android.content.Context
import com.jmreader.data.AppContainer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 应用级模块：提供全局单例依赖。
 *
 * v28.0 重构策略：
 * - 第一阶段：保留 AppContainer，通过 Hilt 提供它（兼容现有代码）
 * - 后续阶段：逐步拆分 AppContainer 内部依赖到独立的 Module
 *
 * @InstallIn(SingletonComponent::class) 表示这些依赖的生命周期与 Application 一致
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * 提供 AppContainer 单例。
     * 
     * 这是过渡方案：现有代码通过 JMApp.instance.container 访问依赖，
     * 这里先通过 Hilt 管理 AppContainer 的创建，后续逐步迁移内部依赖。
     */
    @Provides
    @Singleton
    fun provideAppContainer(
        @ApplicationContext context: Context
    ): AppContainer {
        return AppContainer(context)
    }
}
