package com.kurodai0715.autoemergencycall.domain

interface SmsSender {
    fun send(phoneNumber: String, message: String)
}