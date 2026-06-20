package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kurodai0715.autoemergencycall.data.EmergencyPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class PowerSignalManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferences: EmergencyPreferences
) : DefaultLifecycleObserver {

    // コルーチンスコープの用意（時刻保存用）
    private val scope = CoroutineScope(Dispatchers.IO)

    // レシーバーの定義
    private val receiver = PowerConnectionReceiver {
        saveCurrentTime()
    }

    // Activity の onStart() に連動して自動で呼ばれる
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        // 1. 動的レシーバーの登録
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        context.registerReceiver(receiver, filter)

        // 2. 起動時チェック（今すでに充電中なら、その時刻を保存する）
        if (isCurrentlyCharging()) {
            saveCurrentTime()
        }
    }

    // Activity の onStop() に連動して自動で呼ばれる
    override fun onStop(owner: LifecycleOwner) {
        // メモリリーク防止のため、レシーバーを確実に解除
        context.unregisterReceiver(receiver)
        super.onStop(owner)
    }

    /**
     * 現在端末が充電中かどうかを判定
     */
    private fun isCurrentlyCharging(): Boolean {
        val batteryStatus = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    /**
     * 不揮発領域に現在時刻を保存
     */
    private fun saveCurrentTime() {
        scope.launch {
            val currentTime = System.currentTimeMillis()
            preferences.updateLastChargingTime(currentTime)
        }
    }
}