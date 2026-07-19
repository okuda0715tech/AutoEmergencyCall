package com.kurodai0715.autoemergencycall.ui.screen.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurodai0715.autoemergencycall.data.SafetyCheckStore
import com.kurodai0715.autoemergencycall.domain.SafetyCheckScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val safetyCheckStore: SafetyCheckStore
) : ViewModel() {

    private val _isSmsPermissionGranted = MutableStateFlow(false)
    val isSmsPermissionGranted: StateFlow<Boolean> = _isSmsPermissionGranted.asStateFlow()

    private val _isAutoRevokeDisabled = MutableStateFlow(true)
    val isAutoRevokeDisabled: StateFlow<Boolean> = _isAutoRevokeDisabled.asStateFlow()

    private val timeFormatter = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    val isMonitoringEnabled: StateFlow<Boolean> = safetyCheckStore.safetyDataFlow
        .map { it.isMonitoringEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val lastActiveTimeText: StateFlow<String> = safetyCheckStore.safetyDataFlow
        .map { data ->
            data.lastActiveTime?.let { timeFormatter.format(Date(it)) } ?: "未検知"
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "--:--"
        )

    val lastCheckTimeText: StateFlow<String> = safetyCheckStore.safetyDataFlow
        .map { data ->
            data.lastCheckTime?.let { timeFormatter.format(Date(it)) } ?: "--:--"
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "--:--"
        )

    /**
     * ON_RESUME などのタイミングでアプリの状態（権限等）を一括でリフレッシュする
     */
    fun refreshStatuses(context: Context) {
        // 1. SMS権限のチェック
        _isSmsPermissionGranted.value = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        // 2. 自動停止制限ステータスの非同期チェック
        viewModelScope.launch {
            val status = try {
                PackageManagerCompat.getUnusedAppRestrictionsStatus(context).get()
            } catch (e: Exception) {
                0
            }
            _isAutoRevokeDisabled.value = (status == 2 || status == 1)
        }
    }

    /**
     * アプリ詳細設定（永久拒否時などのフォールバック用）
     */
    fun createApplicationDetailsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }

    /**
     * 自動リセット解除設定画面へのインテント
     */
    fun createUnusedAppRestrictionsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent("android.settings.UNUSED_APP_RESTRICTIONS").apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
    }

    /**
     * 監視状態の切り替え.
     */
    fun toggleMonitoringStatus(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            safetyCheckStore.updateMonitoringStatus(enabled)

            // 内部で「有効なら登録、無効なら既存ワークをキャンセル」が自動実行されます
            SafetyCheckScheduler.setupPeriodicWork(context, safetyCheckStore)
        }
    }
}