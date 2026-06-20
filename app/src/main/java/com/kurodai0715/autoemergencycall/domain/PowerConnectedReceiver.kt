package com.kurodai0715.autoemergencycall.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PowerConnectedReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        if (
            intent.action !=
            Intent.ACTION_POWER_CONNECTED
        ) {
            return
        }

        val entryPoint =
            EntryPointAccessors.fromApplication(
                context,
                ReceiverEntryPoint::class.java
            )

        val preferences =
            entryPoint.preferences()

        val scheduler =
            entryPoint.scheduler()

        CoroutineScope(Dispatchers.IO).launch {

            preferences.updateLastChargingTime(
                System.currentTimeMillis()
            )

            scheduler.schedule()
        }
    }
}