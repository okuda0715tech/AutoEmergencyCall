package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.telephony.SmsManager
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kurodai0715.autoemergencycall.data.EmergencyPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class EmergencyCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val preferences: EmergencyPreferences
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val NAME = "EmergencyCheckWorker"
    }

    override suspend fun doWork(): Result {
        Log.d("EmergencyWorker", "見守り番兵が起動しました")

        // 1. 【最優先】今まさに充電中（または満充電）かチェック
        if (isCurrentlyCharging()) {
            val now = System.currentTimeMillis()
            preferences.updateLastChargingTime(now)
            Log.d(
                "EmergencyWorker",
                "現在充電中のため、最終充電時刻を更新して終了します: ${formatTime(now)}"
            )
            return Result.success()
        }

        // 2. 充電中でなければ、保存されている「最後の充電時刻」を取得
        // 過去に一度も保存されていない（初回起動など）場合は、現在時刻を仮保存して終了
        val lastChargingTime = preferences.getLastChargingTime()
        if (lastChargingTime == null) {
            preferences.updateLastChargingTime(System.currentTimeMillis())
            return Result.success()
        }

        // 3. 48時間以上経過しているか判定
        val currentTime = System.currentTimeMillis()
        val fortyEightHoursInMillis = 48L * 60 * 60 * 1000 // 48時間をミリ秒に換算

        if (currentTime - lastChargingTime > fortyEightHoursInMillis) {
            Log.e("EmergencyWorker", "⚠️ 48時間以上充電が検知されません！緊急SMSを送信します。")

            // 4. 【緊急事態】SMS送信処理を実行
            sendEmergencySms()
        } else {
            val remainingHours =
                (fortyEightHoursInMillis - (currentTime - lastChargingTime)) / (1000 * 60 * 60)
            Log.d(
                "EmergencyWorker",
                "安全確認完了（最終充電: ${formatTime(lastChargingTime)}、残り約 ${remainingHours} 時間）"
            )
        }

        return Result.success()
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
     * 緊急SMSの送信ロジック
     */
    private fun sendEmergencySms() {
        try {
            // TODO: 本来は preferences などからユーザーが設定した連絡先とメッセージを取得する
            val phoneNumber = "090XXXXXXXX"
            val message =
                "【緊急通報】対象のスマートフォンで48時間以上充電（生存シグナル）が確認できません。安否のご確認をお願いします。"

            // Android 12 (API 31) 以降を考慮した SmsManager の取得方法
            val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)

            // SMS送信（長文に対応する場合は sendMultipartTextMessage を検討してください）
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)

            Log.d("EmergencyWorker", "SMSの送信リクエストが完了しました")
        } catch (e: Exception) {
            Log.e("EmergencyWorker", "SMS送信中にエラーが発生しました", e)
        }
    }

    // デバッグ用の日時フォーマット関数
    private fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}