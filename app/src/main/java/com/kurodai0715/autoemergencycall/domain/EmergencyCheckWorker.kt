package com.kurodai0715.autoemergencycall.domain

import android.content.Context
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
        Log.d("EmergencyWorker", "見守り番兵（定期チェック）が起動しました")

        // 1. 保存されている「最後の生存確認時刻（充電の抜き差し時など）」を取得
        val lastChargingTime = preferences.getLastChargingTime()

        // 過去に一度も保存されていない（アプリ初回起動時など）場合は、
        // 安全のため現在時刻を初期値として保存し、次回から生存確認を開始する。
        if (lastChargingTime == null) {
            val now = System.currentTimeMillis()
            preferences.updateLastChargingTime(now)
            Log.d("EmergencyWorker", "初回起動：基準時刻を初期化しました: ${formatTime(now)}")
            return Result.success()
        }

        // 2. 最終生存確認時刻から48時間以上経過しているか判定
        val currentTime = System.currentTimeMillis()
        // 経過時間を計算
        val timeDiff = currentTime - lastChargingTime

        val fortyEightHoursInMillis = 48L * 60 * 60 * 1000 // 48時間をミリ秒に換算

        if (timeDiff > fortyEightHoursInMillis) {
            // 【緊急事態】充電しっぱなし、または放置されて48時間が経過
            Log.e(
                "EmergencyWorker",
                "48時間以上ユーザーのアクション（生存シグナル）が確認できません！"
            )

            // SMS送信処理を実行
            sendEmergencySms()
        } else {
            // 【正常】まだ48時間以内
            val remainingMillis = fortyEightHoursInMillis - timeDiff
            val remainingHours = remainingMillis / (1000 * 60 * 60)
            val remainingMinutes = (remainingMillis % (1000 * 60 * 60)) / (1000 * 60)

            Log.d(
                "EmergencyWorker",
                "安全確認完了（最終シグナル: ${formatTime(lastChargingTime)}、SMS送信まで残り約 ${remainingHours}時間${remainingMinutes}分）"
            )
        }

        return Result.success()
    }

    /**
     * 緊急SMSの送信ロジック
     */
    private fun sendEmergencySms() {
        try {
            // TODO: 本来は preferences などからユーザーが設定した連絡先とメッセージを取得する
            val phoneNumber = "090XXXXXXXX"
            val message =
                "【緊急通報】対象のスマートフォンで48時間以上充電の抜き差し（生存確認）が検知できません。至急安否のご確認をお願いします。"

            val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)

            Log.d("EmergencyWorker", "緊急SMSを送信しました")
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