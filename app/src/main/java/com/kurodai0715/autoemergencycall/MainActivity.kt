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
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kurodai0715.autoemergencycall.data.SafetyCheckStore
import com.kurodai0715.autoemergencycall.domain.SafetyCheckScheduler
import com.kurodai0715.autoemergencycall.ui.screen.AppBaseScreen
import com.kurodai0715.autoemergencycall.ui.theme.AutoEmergencyCallTheme
import com.kurodai0715.autoemergencycall.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var safetyCheckStore: SafetyCheckStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 通知を出す前に、必ず最優先でチャンネルを作成しておく
        NotificationHelper.createNotificationChannel(this)

        manageSafetyCheckExecution()

        enableEdgeToEdge()
        setContent {
            AutoEmergencyCallTheme {
                AppBaseScreen()
            }
        }
    }

    private fun manageSafetyCheckExecution() {
        lifecycleScope.launch {
            // スケジューラー側が内部で有効/無効を判定して適切に処理するため、呼び出すだけで安全です。
            SafetyCheckScheduler.setupPeriodicWork(applicationContext, safetyCheckStore)
        }
    }
}
