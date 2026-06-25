package com.kurodai0715.autoemergencycall.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val CHANNEL_ID = "safety_sms_channel"

    // アプリ起動時などに一度呼び出してチャネルを作成しておく
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "安否確認通知"
            val descriptionText = "SMSの送信状態などを通知します"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * SMS送信時に呼び出す関数（送信ごとに通知を独立して残す）
     * @param context コンテキスト
     * @param targetName 送信先の相手の名前
     */
    fun showSmsSentNotification(context: Context, targetName: String) {
        val notificationManagerCompat = NotificationManagerCompat.from(context)

        // 💡 修正ポイント：Android StudioのLint警告を回避するための確実な権限チェック
        // areNotificationsEnabled() は全OSバージョンで使え、Android 13以降のPOST_NOTIFICATIONSの拒否状態も検知します
        if (!notificationManagerCompat.areNotificationsEnabled()) {
            return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // 必要に応じてアプリアイコンに差し替えてください
            .setContentTitle("安否確認SMSを送信しました")
            .setContentText("${targetName}さん宛てに緊急SMSを送信しました。")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // 呼び出しごとのタイムスタンプからユニークなIDを生成
        val uniqueNotificationId = (System.currentTimeMillis() and 0xfffffff).toInt()

        try {
            // Lint警告が消え、安全に通知が発行できるようになります
            notificationManagerCompat.notify(uniqueNotificationId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}