package com.kurodai0715.autoemergencycall.domain

interface SmsSender {
    fun requestSendSms(
        phoneNumber: String,
        message: String,
        showNotification: Boolean,
        receiverName: String,
    )
}