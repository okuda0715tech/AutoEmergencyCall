package com.kurodai0715.autoemergencycall.ui.screen.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.kurodai0715.autoemergencycall.domain.EmergencyService


@Composable
fun Screen(
    viewModel: ViewModel = hiltViewModel(),
) {

    Column {
        SmsPermissionScreen()

        NotificationPermissionScreen()

        Button(
            onClick = {
                viewModel.sendSms()
            }
        ) {
            Text(text = "送信する")
        }
    }
}

@Composable
fun SmsPermissionScreen(
) {
    val context = LocalContext.current

    // 権限要求の準備
    val permissionLauncher =
        rememberLauncherForActivityResult(
            // 権限要求の結果を受け取るための契約
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            // ユーザーがダイアログで権限を許可するかどうか選択した直後に呼ばれる

            if (granted) {
                Log.i("Permission", "SMS permission granted")
            } else {
                Log.i("Permission", "SMS permission denied")
            }
        }

    Button(
        onClick = {
            when {
                // 現在 SMS 送信権限を持っているかどうかをチェック
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.i("Permission", "SMS permission already granted")
                }

                else -> {
                    // Android システムに権限を要求し、ダイアログを表示してもらう
                    permissionLauncher.launch(
                        Manifest.permission.SEND_SMS
                    )
                }
            }
        }
    ) {
        Text("SMS送信権限を付与する")
    }
}

@Composable
fun NotificationPermissionScreen() {
    // ※ Accompanist などのパーミッション用ライブラリを使うと簡単
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 許可されたので、安心してフォアグラウンドサービスを起動できる
            val intent = Intent(context, EmergencyService::class.java)
            context.startForegroundService(intent)
        } else {
            // 拒否された場合の処理（「通知を許可しないと見守り機能が動作しません」と警告を出すなど）
        }
    }

    Button(onClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }) {
        Text("通知権限を付与する")
    }
}