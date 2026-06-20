package com.kurodai0715.autoemergencycall.ui.screen.home

import android.app.ActivityManager
import android.content.Context
import androidx.lifecycle.ViewModel
import com.kurodai0715.autoemergencycall.domain.EmergencyService
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

    // サービスの稼働状態を管理するStateFlow
    private val _isServiceRunning = MutableStateFlow(false)
    // サービスの通知が消されても、裏できちんと稼働していることをユーザーに知らせるために使用する予定
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    init {
        // 初期化時に現在の状態をチェック
        checkServiceStatus()
    }

    /**
     * サービスが起動中かどうかをチェックして状態を更新する
     */
    fun checkServiceStatus() {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // 自分のサービスの生存状況を確認する唯一の手段であり、Googleが他の手段を用意してくれないまま
        // 9年も使われ続けている関数のため、非推奨だが使い続けて問題ない。
        @Suppress("DEPRECATION")
        val runningServices = activityManager.getRunningServices(100)

        val isRunning = runningServices.any {
            EmergencyService::class.java.name == it.service.className
        }

        _isServiceRunning.value = isRunning
    }

    fun sendSms() {
        smsSender.sendSms(
            "09035695763",
            "テスト"
        )
    }
}