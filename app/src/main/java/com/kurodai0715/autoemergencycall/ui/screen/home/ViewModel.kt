package com.kurodai0715.autoemergencycall.ui.screen.home

import androidx.lifecycle.ViewModel
import com.kurodai0715.autoemergencycall.domain.SmsSender
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(
    private val smsSender: SmsSender,
) : ViewModel() {

    fun sendSms() {
        smsSender.sendSms(
            "09035695763",
            "テスト"
        )
    }
}