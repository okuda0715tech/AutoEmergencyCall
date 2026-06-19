package com.kurodai0715.autoemergencycall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kurodai0715.autoemergencycall.data.saveChargingStartedAt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PowerConnectedReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action != Intent.ACTION_POWER_CONNECTED) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            context.saveChargingStartedAt(
                timestamp = System.currentTimeMillis()
            )
        }
    }
}