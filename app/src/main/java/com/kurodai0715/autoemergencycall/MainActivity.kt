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
            // 起動時にデータストアから今の状態を1回読み出す
            val safetyData = safetyCheckStore.loadSafetyData()

            if (safetyData.isMonitoringEnabled) {
                // ✅ 見守り有効なら、WorkManager登録処理を実行
                // アプリ起動時に安否確認のスケジュールを登録（または維持）します。
                // applicationContext を渡すことで、メモリリークを防ぎ安全に初期化できます。
                SafetyCheckScheduler.setupPeriodicWork(applicationContext)
            } else {
                // 🛑 一時停止中なら登録せず、既存のワークを念のためキャンセル
                WorkManager.getInstance(applicationContext).cancelUniqueWork(
                    SafetyCheckScheduler.UNIQUE_WORK_NAME
                )
            }
        }
    }
}
