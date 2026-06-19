package com.kurodai0715.autoemergencycall.data

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore

private const val DATASTORE_NAME = "heartbeat"

val Context.dataStore by preferencesDataStore(
    name = DATASTORE_NAME
)

object PreferenceKeys {
    val LAST_CHARGING_STARTED_AT =
        longPreferencesKey("last_charging_started_at")
}

suspend fun Context.saveChargingStartedAt(
    timestamp: Long
) {
    dataStore.edit { preferences ->
        preferences[PreferenceKeys.LAST_CHARGING_STARTED_AT] =
            timestamp
    }
}