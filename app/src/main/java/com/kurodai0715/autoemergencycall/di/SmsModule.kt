package com.kurodai0715.autoemergencycall.di

import com.kurodai0715.autoemergencycall.domain.SmsSender
import com.kurodai0715.autoemergencycall.domain.SmsSenderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SmsModule {

    @Binds
    @Singleton
    abstract fun bindSmsSender(
        smsSenderImpl: SmsSenderImpl
    ): SmsSender
}
