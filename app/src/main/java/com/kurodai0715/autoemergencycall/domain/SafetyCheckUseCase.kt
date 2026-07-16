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

    suspend fun executeCheck() {
        val currentTime = System.currentTimeMillis()

        // バッテリー変化を元に最新の「アクティブ時刻」を計算・更新する
        val latestActiveTime = checkAndSaveBatteryStatus(currentTime)

        // 最終アクティブからの経過時間を算出
        val elapsedTime = currentTime - latestActiveTime

        // 経過時間に基づき、必要であれば安否確認のSMSを送信する
        evaluateAndTriggerSms(elapsedTime)
    }

    /**
     * 現在のバッテリー状態を検知し、前回のデータと照合して
     * 変化があった場合は最新アクティブ時刻を更新し、永続化ストアに保存する。
     */
    private suspend fun checkAndSaveBatteryStatus(currentTime: Long): Long {
        val batteryStatus = getBatteryStatusIntent()
        val currentLevel = getBatteryLevel(batteryStatus)
        val isConnected = getIsConnected(batteryStatus)

        val safetyData = store.loadSafetyData()

        // 初期値（初回起動時等）のフォールバック
        val lastLevel = safetyData.lastBatteryLevel ?: currentLevel
        val isIncreased = currentLevel > lastLevel
        val lastIsIncreased = safetyData.lastIsIncreased ?: isIncreased
        val lastActiveTime = safetyData.lastActiveTime ?: currentTime
        val lastIsConnected = safetyData.lastIsConnected ?: isConnected

        // アクティブ時刻を更新するトリガー条件を判定
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

        store.updateSafetyData(
            batteryLevel = currentLevel,
            activeTime = latestActiveTime,
            checkTime = currentTime,
            isIncreased = isIncreased,
            isConnected = isConnected,
        )

        return latestActiveTime
    }

    /**
     * 経過時間と設定値を評価し、適切な連絡先にSMS送信を要求する。
     */
    private suspend fun evaluateAndTriggerSms(elapsedTime: Long) {
        val allContacts = contactStore.loadContacts()
        if (allContacts.isEmpty()) {
            Log.w("SafetyCheck", "連絡先が0件のためSMSを送信できる状態ではありません。")
            return
        }

        val alertConfigs = alertConfigStore.loadAlertConfigs()

        if (alertConfigs.isEmpty()) {
            // A. デフォルト仕様：動作設定が空なら、すべての連絡先に48時間経過後に送る
            if (elapsedTime >= DEFAULT_SMS_THRESHOLD) {
                allContacts.forEach { contact ->
                    triggerSendSms(contact, 48)
                }
            }
        } else {
            // B. ユーザー設定仕様：登録された設定ごとに判定して送る
            alertConfigs.forEach { config ->
                val thresholdMillis = config.thresholdHours * 60 * 60 * 1000L
                if (elapsedTime >= thresholdMillis) {
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
            phoneNumber = contact.phoneNumber,
            showNotification = true,
            receiverName = contact.name,
            elapsedTime = hours.toString(),
        )
    }
}