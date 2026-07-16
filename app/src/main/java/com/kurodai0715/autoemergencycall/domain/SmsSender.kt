package com.kurodai0715.autoemergencycall.domain

interface SmsSender {
    fun requestSendSms(
        phoneNumber: String,
        showNotification: Boolean,
        isTest: Boolean = false,
        receiverName: String,
        elapsedTime: String,
    )
}