package com.kurodai0715.autoemergencycall.data

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private const val DATASTORE_NAME = "heartbeat"

val Context.dataStore by preferencesDataStore(
    name = DATASTORE_NAME
)

@Singleton
class EmergencyPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private object Keys {
        val LAST_CHARGING_TIME =
            longPreferencesKey("last_charging_time")
    }

    suspend fun updateLastChargingTime(
        timestamp: Long
    ) {
        context.dataStore.edit {
            it[Keys.LAST_CHARGING_TIME] = timestamp
        }
    }

    suspend fun getLastChargingTime(): Long? {
        return context.dataStore.data.first()[Keys.LAST_CHARGING_TIME]
    }
}