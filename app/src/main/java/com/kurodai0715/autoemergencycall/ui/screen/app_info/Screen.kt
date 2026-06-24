package com.kurodai0715.autoemergencycall.ui.screen.app_info

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.kurodai0715.autoemergencycall.domain.DebugManager

@Composable
fun AppInfoScreen() {
    val context = LocalContext.current

    val version = getAppVersion(context)

    // タップ回数の記録は、この画面内（ローカル）だけで保持できればいいので remember を使用
    var tapCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // アプリタイトル
        Text(
            text = "自動安否確認アプリ 守るくん",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        // バージョン表示
        Text(
            text = "バージョン: $version",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // デバッグモードがONになったら自動で出現するエリア
        AnimatedVisibility(
            visible = DebugManager.isDebugging,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "● 審査用デバッグモード有効化中",
                    color = Color.Red,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // コピープリント（5回タップするごとにデバッグモードのONとOFFを切り替え）
        Text(
            text = "© 2026 守るくん Project",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .clickable {
                    tapCount++
                    if (tapCount % 5 == 0) {
                        // グローバルな mutableState を更新
                        DebugManager.isDebugging = !DebugManager.isDebugging

                        val displayText = if (DebugManager.isDebugging)
                            "デバッグモードが有効になりました（1分/1秒判定）"
                        else
                            "デバッグモードが無効になりました"
                        Toast.makeText(context, displayText, Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(8.dp) // タップしやすいように少し判定を広げる
        )
    }
}