package com.kurodai0715.autoemergencycall.domain

import com.kurodai0715.autoemergencycall.data.EmergencyPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReceiverEntryPoint {

    fun preferences(): EmergencyPreferences

    fun scheduler(): EmergencyWorkScheduler
}