package com.kurodai0715.autoemergencycall.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

// Contextの拡張プロパティとしてDataStoreを定義
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "safety_check_prefs")

class SafetyCheckStore(private val context: Context) {

    companion object {
        val KEY_LAST_BATTERY = intPreferencesKey("last_battery_level")
        val KEY_LAST_ACTIVE_TIME = longPreferencesKey("last_active_time")
        val KEY_LAST_CHECK_TIME = longPreferencesKey("last_check_time")
        val KEY_LAST_IS_INCREASED = booleanPreferencesKey("last_is_increased")
        val KEY_LAST_IS_CONNECTED = booleanPreferencesKey("last_is_connected")
        val KEY_IS_MONITORING_ENABLED = booleanPreferencesKey("is_monitoring_enabled")
        val KEY_LAST_DEFAULT_SENT_TIME = longPreferencesKey("last_default_sent_time")
    }

    suspend fun loadSafetyData(): SafetyData {
        val preferences = context.dataStore.data.first()
        return SafetyData(
            lastBatteryLevel = preferences[KEY_LAST_BATTERY],
            lastActiveTime = preferences[KEY_LAST_ACTIVE_TIME],
            lastCheckTime = preferences[KEY_LAST_CHECK_TIME],
            lastIsIncreased = preferences[KEY_LAST_IS_INCREASED],
            lastIsConnected = preferences[KEY_LAST_IS_CONNECTED],
            isMonitoringEnabled = preferences[KEY_IS_MONITORING_ENABLED] ?: true,
            lastDefaultSentTime = preferences[KEY_LAST_DEFAULT_SENT_TIME],
        )
    }

    suspend fun updateSafetyData(
        batteryLevel: Int,
        activeTime: Long?,
        checkTime: Long,
        isIncreased: Boolean,
        isConnected: Boolean,
    ) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_BATTERY] = batteryLevel
            preferences[KEY_LAST_IS_INCREASED] = isIncreased
            if (activeTime != null) {
                preferences[KEY_LAST_ACTIVE_TIME] = activeTime
            }
            preferences[KEY_LAST_CHECK_TIME] = checkTime
            preferences[KEY_LAST_IS_CONNECTED] = isConnected
        }
    }

    // デフォルトSMSを送信した「その瞬間のシステム時刻」を保存する
    suspend fun updateLastDefaultSentTime(sentTime: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_DEFAULT_SENT_TIME] = sentTime
        }
    }

    suspend fun updateMonitoringStatus(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_MONITORING_ENABLED] = enabled
        }
    }
}