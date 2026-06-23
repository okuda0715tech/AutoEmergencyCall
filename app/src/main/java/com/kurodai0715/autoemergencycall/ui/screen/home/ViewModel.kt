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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _isSmsPermissionGranted = MutableStateFlow(false)
    val isSmsPermissionGranted: StateFlow<Boolean> = _isSmsPermissionGranted.asStateFlow()

    private val _isAutoRevokeDisabled = MutableStateFlow(true)
    val isAutoRevokeDisabled: StateFlow<Boolean> = _isAutoRevokeDisabled.asStateFlow()

    /**
     * ON_RESUME などのタイミングでアプリの状態を一括でリフレッシュする
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
            // 1: 機能なし(古いOS), 2: DISABLED(自動削除オフで安全) の場合は警告不要 (true)
            _isAutoRevokeDisabled.value = (status == 2 || status == 1)
        }
    }

    /**
     * アプリ詳細設定（パーミッション変更等）を開くインテントの生成
     */
    fun createApplicationDetailsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }

    /**
     * 自動リセット解除のための制限設定画面を開くインテントの生成
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
}