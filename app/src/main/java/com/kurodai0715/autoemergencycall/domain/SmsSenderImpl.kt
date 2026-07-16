package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.kurodai0715.autoemergencycall.R
import com.kurodai0715.autoemergencycall.data.ProfileStore
import com.kurodai0715.autoemergencycall.util.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SmsSenderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val profileStore: ProfileStore,
) : SmsSender {

    override fun requestSendSms(
        phoneNumber: String,
        showNotification: Boolean,
        isTest: Boolean,
        receiverName: String,
        elapsedTime: String,
    ) {
        val smsManager =
            context.getSystemService(SmsManager::class.java)

        val senderName = getSenderName()

        // デバッグモードの場合は SMS を送信しない
        if (DebugManager.isDebugging) {
            Log.i(
                "SmsSender",
                "Skipping SMS transmission due to debug mode. " +
                        "phoneNumber = $phoneNumber, " +
                        "receiverName = $receiverName, " +
                        "senderName = $senderName, " +
                        "elapsedTime = $elapsedTime"
            )
        } else {
            val testLabel =
                if (isTest)
                    context.getString(R.string.test_label)
                else
                    ""
            val message = context.getString(
                R.string.sms_message,
                testLabel, receiverName, senderName, elapsedTime
            )

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
            NotificationHelper.showSmsSentNotification(context, receiverName)
        }
    }

    private fun getSenderName(): String {
        return profileStore.getUserName().ifBlank {
            context.getString(R.string.no_name_user)
        }
    }
}