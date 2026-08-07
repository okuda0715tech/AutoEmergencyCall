package com.kurodai0715.autoemergencycall.di

import android.content.Context
import com.kurodai0715.autoemergencycall.data.ProfileStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideUserSettings(
        @ApplicationContext context: Context
    ): ProfileStore {
        return ProfileStore(context)
    }
}