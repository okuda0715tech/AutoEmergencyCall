package com.kurodai0715.autoemergencycall.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kurodai0715.autoemergencycall.data.EmergencyPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        if (
            intent.action != Intent.ACTION_BOOT_COMPLETED
        ) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {

            val prefs =
                EmergencyPreferences(context)

            val lastChargingTime = prefs.getLastChargingTime() ?: return@launch

            val limit = TimeUnit.HOURS.toMillis(48)

            val elapsed = System.currentTimeMillis() - lastChargingTime

            val remaining = limit - elapsed

            val scheduler = EmergencyWorkScheduler(context)

            if (remaining <= 0) {

                scheduler.schedule(0)

            } else {

                scheduler.schedule(remaining)
            }
        }
    }
}