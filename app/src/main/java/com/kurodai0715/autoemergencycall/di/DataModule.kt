package com.kurodai0715.autoemergencycall.di

import android.content.Context
import com.kurodai0715.autoemergencycall.data.ContactStore
import com.kurodai0715.autoemergencycall.data.SafetyCheckStore
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
    fun provideSafetyCheckStore(
        @ApplicationContext context: Context
    ): SafetyCheckStore {
        return SafetyCheckStore(context)
    }

    @Provides
    @Singleton
    fun provideContactStore(
        @ApplicationContext context: Context
    ): ContactStore {
        return ContactStore(context)
    }
}