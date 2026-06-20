package com.kurodai0715.autoemergencycall.domain

interface SmsSender {
    fun sendSms(phoneNumber: String, message: String)
}