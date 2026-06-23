package com.kurodai0715.autoemergencycall.ui.screen.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.kurodai0715.autoemergencycall.domain.SafetyCheckUseCase
import com.kurodai0715.directdebitmanager.ui.util.debouncedClick
import kotlinx.coroutines.launch


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

        SafetyCheckTestScreen(onClickTestButton = viewModel::onTestButtonClicked)
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
fun SafetyCheckTestScreen(onClickTestButton: () -> Unit) {
    // 1. Compose 内で Context を取得
    val context = LocalContext.current

    // 2. ボタンタップ時に suspend 関数を呼び出すためのコルーチンスコープ
    val coroutineScope = rememberCoroutineScope()

    // テスト実行ボタン
    Button(
        onClick = {
            debouncedClick {
                Log.i("SafetyCheckTestScreen", "Execute SafetyCheckUseCase")

                onClickTestButton()

                // 実行通知
                Toast.makeText(context, "安否確認を即座に開始しました", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        Text(text = "今すぐチェック処理を実行 (Force Run)")
    }
}