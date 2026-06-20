package com.kurodai0715.autoemergencycall

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kurodai0715.autoemergencycall.domain.EmergencyCheckWorker
import com.kurodai0715.autoemergencycall.domain.EmergencyService
import com.kurodai0715.autoemergencycall.ui.screen.AppBaseScreen
import com.kurodai0715.autoemergencycall.ui.theme.AutoEmergencyCallTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1 時間に 1 回見回りする WorkManager の定期ジョブを登録
        setupEmergencyWorker()

        // 通知の表示権限を持っていれば、フォアグラウンドサービスを開始する。
        // 何度起動しても、二重起動にはならないため心配不要。
        if (hasNotificationPermission()) {
            startEmergencyService()
        }

        enableEdgeToEdge()
        setContent {
            AutoEmergencyCallTheme {
                AppBaseScreen()
            }
        }
    }

    private fun setupEmergencyWorker() {
        // 1時間に1回実行する定期リクエストを作成
        // （※Androidの仕様上、定期実行の間隔は最短15分まで指定可能です）
        val workRequest = PeriodicWorkRequestBuilder<EmergencyCheckWorker>(
            1, TimeUnit.HOURS
        ).build()

        // ユニークワークとして登録（既存のスケジュールがあればそれを維持し、二重登録を防ぐ）
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            EmergencyCheckWorker.NAME,
            ExistingPeriodicWorkPolicy.KEEP, // すでに登録済みなら何もしない（タスクを上書きしない）
            workRequest
        )
    }

    /**
     * 通知権限がすでに付与されているかチェックする
     */
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12以前は通知の個別許可が不要（デフォルトで許可）なため常にtrue
            true
        }
    }

    /**
     * 安全にフォアグラウンドサービスを起動する
     */
    private fun startEmergencyService() {
        val serviceIntent = Intent(this, EmergencyService::class.java)
        // 念のため、すでにサービスが起動していても二重起動にならないようにOSが制御してくれます
        startForegroundService(serviceIntent)
    }
}
