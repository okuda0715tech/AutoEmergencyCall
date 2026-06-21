package com.kurodai0715.autoemergencycall.ui.screen.home

import android.app.ActivityManager
import android.content.Context
import androidx.lifecycle.ViewModel
import com.kurodai0715.autoemergencycall.domain.SmsSender
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val smsSender: SmsSender,
) : ViewModel() {

    fun sendSms() {
        smsSender.sendSms(
            "09035695763",
            "テスト"
        )
    }
}