package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kurodai0715.autoemergencycall.BuildConfig
import java.util.concurrent.TimeUnit

object SafetyCheckScheduler {

    // WorkManager内でこのジョブを一意に識別するためのユニーク名
    const val UNIQUE_WORK_NAME = "BatterySafetyCheckWork"

    fun setupPeriodicWork(context: Context) {
        val interval: Long = if (BuildConfig.DEBUG) 15 else 60

        // 1. 定期実行するワークのリクエストを作成
        // ※第一引数で実行間隔（今回は60分＝1時間）を指定します。
        // ※WorkManagerの仕様上、15分未満の間隔は指定できません。
        val safetyCheckRequest = PeriodicWorkRequestBuilder<SafetyCheckWorker>(
            interval, TimeUnit.MINUTES
        ).build()

        // 2. ジョブをWorkManagerに登録
        // 【重要】ポリシーに「ExistingPeriodicWorkPolicy.KEEP」を指定しています。
        // これにより、すでに1時間タイマーが動いている場合は、アプリを開き直しても
        // ジョブが二重登録されたり、タイマーが最初からリセットされたりするのを防ぎます。
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,

            ExistingPeriodicWorkPolicy.KEEP,
            safetyCheckRequest
        )
    }
}