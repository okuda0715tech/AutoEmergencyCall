package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kurodai0715.autoemergencycall.data.SafetyCheckStore

class SafetyCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) { // CoroutineWorkerに変更

    companion object{
        private const val TAG = "SafetyCheckWorker"
    }

    override suspend fun doWork(): Result {

        Log.i(TAG, "SafetyCheckWorker is running")

        SafetyCheckUseCase(context).executeCheck()
        return Result.success()
    }
}