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
import com.kurodai0715.autoemergencycall.data.SafetyCheckStore // 💡 追加
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val safetyCheckStore: SafetyCheckStore // 💡 コンストラクタでStoreを受け取るように変更
) : ViewModel() {

    private val _isSmsPermissionGranted = MutableStateFlow(false)
    val isSmsPermissionGranted: StateFlow<Boolean> = _isSmsPermissionGranted.asStateFlow()

    private val _isAutoRevokeDisabled = MutableStateFlow(true)
    val isAutoRevokeDisabled: StateFlow<Boolean> = _isAutoRevokeDisabled.asStateFlow()

    private val _lastActiveTimeText = MutableStateFlow("--:--")
    val lastActiveTimeText: StateFlow<String> = _lastActiveTimeText.asStateFlow()

    private val _lastCheckTimeText = MutableStateFlow("--:--")
    val lastCheckTimeText: StateFlow<String> = _lastCheckTimeText.asStateFlow()

    private val timeFormatter = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

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
            _isAutoRevokeDisabled.value = (status == 2 || status == 1)
        }

        // 3. 💡 DataStoreから実際の最終時刻データを読み込む
        viewModelScope.launch {
            try {
                val safetyData = safetyCheckStore.loadSafetyData()

                // 生存確認できた最終時刻の反映
                _lastActiveTimeText.value = safetyData.lastActiveTime?.let {
                    timeFormatter.format(Date(it))
                } ?: "未検知"

                // システムが最後にチェックした時刻（今回は現在の生存チェックデータ上の最新記録として反映）
                // ※もし定期ジョブ側の実行ログ用Timeを別で保存している場合は、そちらの変数をバインドしてください
                _lastCheckTimeText.value = safetyData.lastActiveTime?.let {
                    timeFormatter.format(Date(it))
                } ?: "--:--"

            } catch (e: Exception) {
                _lastActiveTimeText.value = "エラー"
                _lastCheckTimeText.value = "エラー"
            }
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
}