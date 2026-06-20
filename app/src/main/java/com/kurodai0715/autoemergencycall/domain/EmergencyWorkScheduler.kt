package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyWorkScheduler @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) {

    companion object {
        private const val WORK_NAME = "emergency_check"

        private const val LIMIT_HOURS = 48L
    }

    /**
     * 指定した時間経過後に実行される Worker を登録する。
     */
    fun schedule(
        delayMillis: Long = TimeUnit.HOURS.toMillis(LIMIT_HOURS)
    ) {

        val request =
            OneTimeWorkRequestBuilder<EmergencyCheckWorker>()
                .setInitialDelay(
                    delayMillis,
                    TimeUnit.MILLISECONDS
                )
                .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE, // 既存の Worker を上書き
                request
            )
    }
}