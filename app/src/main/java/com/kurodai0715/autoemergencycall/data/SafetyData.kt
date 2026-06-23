package com.kurodai0715.autoemergencycall.data

import kotlinx.serialization.Serializable

// 保存されているデータを一括で取得するデータクラス
data class SafetyData(
    val lastBatteryLevel: Int?,
    val lastActiveTime: Long?,
    val lastCheckTime: Long?,
    val lastIsIncreased: Boolean?,
    val lastIsConnected: Boolean?,
    val isMonitoringEnabled: Boolean = true,
)