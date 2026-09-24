package com.jmreader.di

import com.jmreader.data.AppContainer
import com.jmreader.data.api.direct.JmDirectClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 网络层 Hilt 模块。
 *
 * v28.0: 第一阶段从 AppContainer 委托，后续会重构为直接创建依赖。
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * 提供直连客户端（JM 加密解密 + 域名轮换）。
     */
    @Provides
    @Singleton
    fun provideJmDirectClient(container: AppContainer): JmDirectClient {
        return container.directClient
    }
}
