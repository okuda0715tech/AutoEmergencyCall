package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.kurodai0715.autoemergencycall.data.AlertConfigStore
import com.kurodai0715.autoemergencycall.data.Contact
import com.kurodai0715.autoemergencycall.data.ContactStore
import com.kurodai0715.autoemergencycall.data.SafetyCheckStore
import com.kurodai0715.autoemergencycall.data.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SafetyCheckUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val store: SafetyCheckStore,
    private val contactStore: ContactStore,
    private val alertConfigStore: AlertConfigStore,
    private val smsSender: SmsSender,
) {

    companion object {
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

        val latestActiveTime = when {
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
            activeTime = latestActiveTime,
            checkTime = currentTime,
            isIncreased = isIncreased,
            isConnected = isConnected,
        )

        val elapsedTime = currentTime - latestActiveTime

        // 連絡先データと動作設定データのロード
        val allContacts = contactStore.loadContacts()
        val alertConfigs = alertConfigStore.loadAlertConfigs()

        if (allContacts.isEmpty()) {
            Log.w(
                "SafetyCheck",
                "連絡先が0件のためSMSを送信できる状態ではありません。"
            )
            return
        }

        // SMS送信ロジック（複数設定 vs デフォルト仕様の判定）
        if (alertConfigs.isEmpty()) {
            // デフォルト仕様：動作設定が空なら、すべての連絡先に48時間後に送る
            if (elapsedTime >= DEFAULT_SMS_THRESHOLD) {
                allContacts.forEach { contact ->
                    triggerSendSms(contact, 48)
                }
            }
        } else {
            // ユーザー設定仕様：登録された複数の動作設定をループ処理
            alertConfigs.forEach { config ->
                if (elapsedTime >= config.thresholdHours * 60 * 60 * 1000L) {
                    // この設定の対象になっている連絡先を抽出
                    val targets = allContacts.filter { contact ->
                        config.targetContactIds.contains(contact.id)
                    }

                    targets.forEach { contact ->
                        triggerSendSms(contact, config.thresholdHours)
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
        val plugStatus = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1

        // ACコンセント、USB、またはワイヤレス充電のいずれかに接続されているか。
        // 充電器だけでなく、USBメモリやワイヤレス機器の物理的な接続変化をすべてキャッチする。
        return plugStatus == BatteryManager.BATTERY_PLUGGED_AC ||
                plugStatus == BatteryManager.BATTERY_PLUGGED_USB ||
                plugStatus == BatteryManager.BATTERY_PLUGGED_WIRELESS
    }

    private fun triggerSendSms(contact: Contact, hours: Int) {
        smsSender.requestSendSms(
            contact.phoneNumber,
            message = "${contact.name}さんへの安否確認SMS：端末の活動が${hours}時間検知できませんでした。",
            showNotification = true,
            targetName = contact.name,
        )
    }

    private fun getUserName(): String {
        val userSettings = UserSettings(context)
        val userName = userSettings.getUserName()

        return if (userName.isNotBlank()) {
            "${userName}さん"
        } else {
            "名前未登録のユーザー"
        }
    }
}