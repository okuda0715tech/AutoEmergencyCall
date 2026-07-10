package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SafetyCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val safetyCheckUseCase: SafetyCheckUseCase,
) : CoroutineWorker(context, workerParams) { // CoroutineWorkerに変更

    companion object {
        private const val TAG = "SafetyCheckWorker"
    }

    override suspend fun doWork(): Result {

        Log.i(TAG, "SafetyCheckWorker is running")

        safetyCheckUseCase.executeCheck()
        return Result.success()
    }
}