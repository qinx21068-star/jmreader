package com.jmreader.di

import com.jmreader.data.AppContainer
import com.jmreader.data.local.*
import com.jmreader.data.repository.JMRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据层 Hilt 模块：Repository 和 DataStore。
 *
 * v28.0: 第一阶段从 AppContainer 委托，后续会迁移到 Room + 独立创建。
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideJMRepository(container: AppContainer): JMRepository {
        return container.repository
    }

    @Provides
    @Singleton
    fun provideSettingsStore(container: AppContainer): SettingsStore {
        return container.settingsStore
    }

    @Provides
    @Singleton
    fun provideFavoritesStore(container: AppContainer): FavoritesStore {
        return container.favoritesStore
    }

    @Provides
    @Singleton
    fun provideHistoryStore(container: AppContainer): HistoryStore {
        return container.historyStore
    }

    @Provides
    @Singleton
    fun provideBrowseHistoryStore(container: AppContainer): BrowseHistoryStore {
        return container.browseHistoryStore
    }

    @Provides
    @Singleton
    fun provideBlockedTagsStore(container: AppContainer): BlockedTagsStore {
        return container.blockedTagsStore
    }

    @Provides
    @Singleton
    fun provideSearchHistoryStore(container: AppContainer): SearchHistoryStore {
        return container.searchHistoryStore
    }

    @Provides
    @Singleton
    fun provideComicTagsCache(container: AppContainer): ComicTagsCache {
        return container.comicTagsCache
    }

    @Provides
    @Singleton
    fun provideDownloadManager(container: AppContainer): com.jmreader.data.download.DownloadManager {
        return container.downloadManager
    }
}
