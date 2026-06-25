package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.kurodai0715.autoemergencycall.util.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SmsSenderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SmsSender {

    override fun sendSms(
        phoneNumber: String,
        message: String,
        showNotification: Boolean,
        targetName: String,
    ) {
        val smsManager =
            context.getSystemService(SmsManager::class.java)

        // デバッグモードの場合は SMS を送信しない
        if (DebugManager.isDebugging) {
            Log.i(
                "SmsSender",
                "SMS送信をスキップします。 phoneNumber = $phoneNumber, message = $message"
            )
        } else {
            smsManager?.sendTextMessage(
                phoneNumber,
                null,
                message,
                null,
                null
            )
        }

        if (showNotification) {
            // 送信直後に通知を表示
            // 複数人に連続で送られた場合でも、NotificationHelper側でユニークIDを
            // 生成しているため、通知が上書きされずに人数分並んで表示されます。
            NotificationHelper.showSmsSentNotification(context, targetName)
        }
    }
}