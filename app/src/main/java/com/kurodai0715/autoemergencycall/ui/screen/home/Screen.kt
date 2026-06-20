package com.kurodai0715.autoemergencycall.ui.screen.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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


@Composable
fun Screen(
    viewModel: ViewModel = hiltViewModel(),
) {

    Column {
        SmsPermissionScreen()

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