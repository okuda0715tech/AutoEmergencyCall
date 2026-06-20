package com.kurodai0715.autoemergencycall.domain.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PowerConnectionReceiver(
    private val onPowerSignalDetected: () -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        Log.i("PowerConnectionReceiver", "action=${intent.action}")

        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED -> {
                // 充電開始・終了どちらも「生存シグナル」として扱う
                onPowerSignalDetected()
            }
        }
    }
}