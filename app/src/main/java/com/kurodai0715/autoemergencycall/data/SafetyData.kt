package com.kurodai0715.autoemergencycall.data

// 保存されているデータを一括で取得するデータクラス
data class SafetyData(
    val lastBatteryLevel: Int?,
    val lastActiveTime: Long?,
    val lastCheckTime: Long?,
    val lastIsIncreased: Boolean?,
    val lastIsConnected: Boolean?,
    val isMonitoringEnabled: Boolean = true,
    // デフォルト仕様で最後にSMSを送信した「実際のシステム時刻」
    val lastDefaultSentTime: Long? = null,
)