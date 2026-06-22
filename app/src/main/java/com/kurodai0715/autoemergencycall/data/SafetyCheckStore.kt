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
        val KEY_LAST_IS_CONNECTED = booleanPreferencesKey("last_is_connected")
    }

    // 保存されているデータを一括で取得するデータクラス
    data class SafetyData(
        val lastBatteryLevel: Int?,
        val lastActiveTime: Long?,
        val lastIsIncreased: Boolean?,
    )

    suspend fun loadSafetyData(): SafetyData {
        val preferences = context.dataStore.data.first()
        return SafetyData(
            lastBatteryLevel = preferences[KEY_LAST_BATTERY],
            lastActiveTime = preferences[KEY_LAST_ACTIVE_TIME],
            lastIsIncreased = preferences[KEY_LAST_IS_CONNECTED],
        )
    }

    suspend fun updateSafetyData(batteryLevel: Int, activeTime: Long?, isIncreased: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_BATTERY] = batteryLevel
            preferences[KEY_LAST_IS_CONNECTED] = isIncreased
            if (activeTime != null) {
                preferences[KEY_LAST_ACTIVE_TIME] = activeTime
            }
        }
    }
}