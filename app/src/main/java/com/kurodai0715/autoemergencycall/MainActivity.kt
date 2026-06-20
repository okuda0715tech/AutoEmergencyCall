package com.kurodai0715.autoemergencycall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kurodai0715.autoemergencycall.domain.EmergencyCheckWorker
import com.kurodai0715.autoemergencycall.domain.PowerSignalManager
import com.kurodai0715.autoemergencycall.ui.screen.AppBaseScreen
import com.kurodai0715.autoemergencycall.ui.theme.AutoEmergencyCallTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Hilt が自動でマネージャーを生成して注入してくれます
    @Inject
    lateinit var powerSignalManager: PowerSignalManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Activity のライフサイクルにマネージャーを登録（これだけで onStart / onStop が連動します）
        lifecycle.addObserver(powerSignalManager)

        // 1 時間に 1 回見回りする WorkManager の定期ジョブを登録
        setupEmergencyWorker()

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
}
