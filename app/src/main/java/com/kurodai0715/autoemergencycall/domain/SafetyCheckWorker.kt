package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kurodai0715.autoemergencycall.data.SafetyCheckStore

class SafetyCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) { // CoroutineWorkerに変更

    companion object {
        private const val SELF_CHECK_THRESHOLD = 24 * 60 * 60 * 1000L // 24時間のミリ秒表現
        private const val EMERGENCY_SMS_TRIGGER_THRESHOLD = 48 * 60 * 60 * 1000L // 48時間のミリ秒表現

    }

    // バッテリーの現在の状態を取得
    val batteryStatus: Intent? = context.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )

    override suspend fun doWork(): Result {
        val store = SafetyCheckStore(context)
        val currentTime = System.currentTimeMillis()

        // 現在のバッテリー残量を取得
        val currentLevel = getBatteryLevel()

        // DataStoreから前回保存したデータを安全に読み込む
        val safetyData = store.loadSafetyData()

        // 初回起動時は null のため現在の値で初期化
        val lastLevel = safetyData.lastBatteryLevel ?: currentLevel
        val isCharging = currentLevel >= lastLevel
        val lastIsCharging = safetyData.lastIsCharging ?: isCharging
        val lastActiveTime = safetyData.lastActiveTime ?: currentTime

        // 充電状態に変化があれば安全確認時刻を更新
        val newActiveTime = if (lastIsCharging != isCharging) currentTime else lastActiveTime

        // 最新の状態を DataStore に非同期で安全に保存
        store.updateSafetyData(
            batteryLevel = currentLevel,
            activeTime = newActiveTime,
            isConnected = isCharging
        )

        // タイムリミット（24時間放置）のチェック
        if (currentTime - newActiveTime >= SELF_CHECK_THRESHOLD) {
            triggerEmergencyAlert()
        }

        if (currentTime - newActiveTime >= EMERGENCY_SMS_TRIGGER_THRESHOLD) {
            triggerEmergencySmsSend()
        }

        return Result.success()
    }

    /**
     * 現在のバッテリー残量を取得する。単位は % とする.
     */
    private fun getBatteryLevel(): Int {
        // 現在のバッテリー残量を取得
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        // バッテリーの最大値を取得
        // デバイスによっては最大値が 100 ではない場合があるため
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            // バッテリー残量を % に変換する
            ((level / scale.toFloat()) * 100).toInt()
        } else {
            // バッテリーのステータスが取得できなかった場合のセーフティー
            50
        }
    }

    /**
     * 充電装置が接続されているかどうか.
     *
     * ワイヤレス充電、 USB 充電、どれでも充電装置が接続されていれば true 。そうでなければ false .
     */
    private fun getIsConnected(): Boolean {
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

        // CHARGING（充電中）と FULL（満充電）は充電ケーブルが刺さっている状態。
        // 満充電でケーブルが抜かれると即座に DISCHARGING に変わる。
        // 満充電でケーブルが刺さっている状態で、どれだけデバイスが処理を行っても
        // DISCHARGING にはならず FULL のままとなる。
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun triggerEmergencyAlert() {
        // TODO: ローカルでの緊急警報処理
    }

    private fun triggerEmergencySmsSend() {
        TODO()
    }
}