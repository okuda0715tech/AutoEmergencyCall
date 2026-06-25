package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.kurodai0715.autoemergencycall.data.ConfigStore
import com.kurodai0715.autoemergencycall.data.Contact
import com.kurodai0715.autoemergencycall.data.ContactStore
import com.kurodai0715.autoemergencycall.data.SafetyCheckStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SafetyCheckUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val store: SafetyCheckStore,
    private val contactStore: ContactStore,
    private val configStore: ConfigStore, // TODO AlertConfigStore という名前に後で変更する
    private val smsSender: SmsSender,
) {

    companion object {
        private const val SELF_CHECK_THRESHOLD = 24 * 60 * 60 * 1000L // 24時間のミリ秒
        private const val DEFAULT_SMS_THRESHOLD = 48 * 60 * 60 * 1000L // 48時間のミリ秒
    }

    // 以前の doWork 内のコアロジックをここに移植
    suspend fun executeCheck() {
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
            checkTime = currentTime,
            isIncreased = isIncreased,
            isConnected = isConnected,
        )

        val elapsedTime = currentTime - newActiveTime

        // タイムリミット（24時間放置）のチェック
        if (elapsedTime >= SELF_CHECK_THRESHOLD) {
            triggerEmergencyAlert()
        }

        // 連絡先データと動作設定データのロード
        val allContacts = contactStore.loadContacts()
        val alertConfigs = configStore.loadAlertConfigs()

        if (allContacts.isEmpty()) {
            Log.w(
                "SafetyCheck",
                "SMS送信しきい値を超えましたが、連絡先が0件のためSMSを送信できません。"
            )
            return
        }

        // SMS送信ロジック（複数設定 vs デフォルト仕様の判定）
        if (alertConfigs.isEmpty()) {
            // デフォルト仕様：動作設定が空なら、すべての連絡先に48時間後に送る
            if (elapsedTime >= DEFAULT_SMS_THRESHOLD) {
                allContacts.forEach { contact ->
                    triggerEmergencySmsSend(contact, 48)
                }
            }
        } else {
            // ユーザー設定仕様：登録された複数の動作設定をループ処理
            alertConfigs.forEach { config ->
                val thresholdMillis = config.thresholdHours * 60 * 60 * 1000L
                if (elapsedTime >= thresholdMillis) {
                    // この設定の対象になっている連絡先を抽出
                    val targets = allContacts.filter { contact ->
                        config.targetContactIds.contains(contact.id)
                    }

                    targets.forEach { contact ->
                        triggerEmergencySmsSend(contact, config.thresholdHours)
                    }
                }
            }
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

    private fun triggerEmergencySmsSend(contact: Contact, hours: Int) {
        smsSender.sendSms(
            contact.phoneNumber,
            message = "${contact.name}さんへの安否確認SMS：端末の活動が${hours}時間検知できませんでした。",
            showNotification = true,
            targetName = contact.name,
        )
    }
}