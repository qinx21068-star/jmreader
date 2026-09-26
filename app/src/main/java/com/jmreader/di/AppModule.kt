package com.jmreader.di

import android.content.Context
import com.jmreader.data.AppContainer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppContainer(
        @ApplicationContext context: Context
    ): AppContainer = AppContainer(context)
}
