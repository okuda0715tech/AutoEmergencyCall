package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kurodai0715.autoemergencycall.data.EmergencyPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class EmergencyCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val preferences: EmergencyPreferences,
    private val smsSender: SmsSender,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        Log.i("EmergencyCheckWorker", "Worker started")

        val lastChargingTime =
            preferences.getLastChargingTime()
                ?: return Result.success()

        val elapsed =
            System.currentTimeMillis() -
                    lastChargingTime

        val limit =
            TimeUnit.HOURS.toMillis(48)

        if (elapsed < limit) {
            return Result.success()
        }

        smsSender.sendSms(
            phoneNumber = "09012345678",
            message = "48時間以上充電が行われていません。"
        )

        return Result.success()
    }
}