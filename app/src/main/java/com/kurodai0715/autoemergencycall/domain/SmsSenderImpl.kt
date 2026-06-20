package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import android.telephony.SmsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SmsSenderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SmsSender {

    override fun sendSms(
        phoneNumber: String,
        message: String,
    ) {
        val smsManager =
            context.getSystemService(SmsManager::class.java)

        smsManager?.sendTextMessage(
            phoneNumber,
            null,
            message,
            null,
            null
        )
    }
}