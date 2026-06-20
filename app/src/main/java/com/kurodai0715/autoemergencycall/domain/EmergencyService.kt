package com.kurodai0715.autoemergencycall.domain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.kurodai0715.autoemergencycall.R
import com.kurodai0715.autoemergencycall.data.EmergencyPreferences
import com.kurodai0715.autoemergencycall.domain.broadcast_receiver.PowerConnectionReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EmergencyService : Service() {

    @Inject
    lateinit var preferences: EmergencyPreferences

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // 💡 24時間監視し続けるためのレシーバー
    private val receiver = PowerConnectionReceiver {
        saveCurrentTime()
    }

    override fun onCreate() {
        super.onCreate()

        // 1. フォアグラウンドサービスに必要な「常時表示の通知」を作成
        startForeground(1, createNotification())

        // 2. ここで動的レシーバーを登録！サービスが生きている間、ずっと登録しっぱなしになります
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_USER_PRESENT) // 画面ロック解除もついでに監視
        }
        registerReceiver(receiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY を返すことで、万が一OSに一時的に落とされても自動復活するようにします
        return START_STICKY
    }

    override fun onDestroy() {
        // サービス終了時にしっかりと解除
        unregisterReceiver(receiver)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun saveCurrentTime() {
        serviceScope.launch {
            preferences.updateLastChargingTime(System.currentTimeMillis())

            // 抜き差しを検知したついでに、通知を再発行して画面に復活させる
            // ただし、常に非表示にしたいユーザーもいると思うので設定で復活させないことも検討
            startForeground(1, createNotification())
        }
    }

    // 通知チャネルと通知の作成（Android 8.0以降必須）
    private fun createNotification(): Notification {
        val channelId = "emergency_service_channel"
        val channelName = "見守りサービス"
        val manager = getSystemService(NotificationManager::class.java)

        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("あんしん見守り中")
            .setContentText("48時間の生存確認センサーが稼働しています。")
            .setSmallIcon(R.drawable.ic_launcher_background)
            // ユーザーがスワイプでこの通知を消去できないように固定する
            // 一部、消せてしまうデバイスもある
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}