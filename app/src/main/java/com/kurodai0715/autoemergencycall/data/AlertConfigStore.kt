package com.kurodai0715.autoemergencycall.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.configDataStore: DataStore<Preferences> by preferencesDataStore(name = "config_prefs")

class AlertConfigStore(private val context: Context) {

    companion object {
        private val KEY_ALERT_CONFIG_LIST_JSON = stringPreferencesKey("alert_config_list_json")

        private val configuredJson = Json {
            coerceInputValues = true
            ignoreUnknownKeys = true
        }
    }

    /**
     * すべての動作設定リストを取得（未設定なら空のリストを返す）
     */
    suspend fun loadAlertConfigs(): List<AlertConfig> {
        val preferences = context.configDataStore.data.first()
        val jsonString = preferences[KEY_ALERT_CONFIG_LIST_JSON]
        if (jsonString.isNullOrEmpty()) return emptyList()

        return try {
            configuredJson.decodeFromString<List<AlertConfig>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 動作設定リストを丸ごと保存
     */
    suspend fun saveAlertConfigs(configs: List<AlertConfig>) {
        val jsonString = configuredJson.encodeToString(configs)
        context.configDataStore.edit { preferences ->
            preferences[KEY_ALERT_CONFIG_LIST_JSON] = jsonString
        }
    }
}