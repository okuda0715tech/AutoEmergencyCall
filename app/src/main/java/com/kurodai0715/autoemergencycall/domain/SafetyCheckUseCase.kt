package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.kurodai0715.autoemergencycall.data.SafetyCheckStore

class SafetyCheckUseCase(private val context: Context) {

    companion object {
        private const val SELF_CHECK_THRESHOLD = 24 * 60 * 60 * 1000L // 24時間のミリ秒
        private const val EMERGENCY_SMS_TRIGGER_THRESHOLD = 48 * 60 * 60 * 1000L // 48時間のミリ秒
    }

    // 以前の doWork 内のコアロジックをここに移植
    suspend fun executeCheck() {
        val store = SafetyCheckStore(context)
        val currentTime = System.currentTimeMillis()

        // 判定を行う「その瞬間」の最新バッテリー情報を取得
        val batteryStatus = getBatteryStatusIntent()
        val currentLevel = getBatteryLevel(batteryStatus)
        val isConnected = getIsConnected(batteryStatus)

        // DataStoreから前回保存したデータを安全に読み込む
        val safetyData = store.loadSafetyData()

        // 初回起動時は null のため現在の値で初期化
        val lastLevel = safetyData.lastBatteryLevel ?: currentLevel
        val isIncreased = currentLevel > lastLevel
        val lastIsIncreased = safetyData.lastIsIncreased ?: isIncreased
        val lastActiveTime = safetyData.lastActiveTime ?: currentTime
        val lastIsConnected = safetyData.lastIsConnected ?: isConnected

        val newActiveTime = when {
            // 充電状態が減少から増加に転じている場合
            isIncreased && !lastIsIncreased -> {
                Log.i("SafetyCheck", "The battery level changed from decreasing to increasing.")
                currentTime
            }
            // 充電装置の接続有無が変化した場合
            isConnected != lastIsConnected -> {
                Log.i("SafetyCheck", "The connection status of the charging device has changed.")
                currentTime
            }
            // 何も更新イベントが発生しなかった場合
            else -> lastActiveTime
        }

        // 最新の状態を DataStore に非同期で安全に保存
        store.updateSafetyData(
            batteryLevel = currentLevel,
            activeTime = newActiveTime,
            isIncreased = isIncreased,
            isConnected = isConnected,
        )

        // タイムリミット（24時間放置）のチェック
        if (currentTime - newActiveTime >= SELF_CHECK_THRESHOLD) {
            triggerEmergencyAlert()
        }

        if (currentTime - newActiveTime >= EMERGENCY_SMS_TRIGGER_THRESHOLD) {
            triggerEmergencySmsSend()
        }
    }

    private fun getBatteryStatusIntent(): Intent? {
        return context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }

    private fun getBatteryLevel(batteryStatus: Intent?): Int {
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            ((level / scale.toFloat()) * 100).toInt()
        } else {
            50
        }
    }

    private fun getIsConnected(batteryStatus: Intent?): Boolean {
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun triggerEmergencyAlert() {
        // TODO: ローカルでの緊急警報処理
    }

    private fun triggerEmergencySmsSend() {
        // TODO: SMS送信処理
    }
}