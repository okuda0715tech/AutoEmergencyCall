package com.kurodai0715.autoemergencycall.ui.screen.developer_only

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DeveloperScreen(
    viewModel: DeveloperViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // 成功・失敗時のトースト通知管理
    LaunchedEffect(uiState) {
        when (uiState) {
            is DeveloperUiState.Success -> {
                Toast.makeText(context, (uiState as DeveloperUiState.Success).message, Toast.LENGTH_SHORT).show()
                viewModel.resetUiState()
            }
            is DeveloperUiState.Error -> {
                Toast.makeText(context, "エラーが発生しました: ${(uiState as DeveloperUiState.Error).exception.localizedMessage}", Toast.LENGTH_LONG).show()
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    // 💡 Scaffoldを撤廃し、スクロール可能な通常のColumnとして構築
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 注意喚起セクション
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚠️ 開発者警告",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "本画面の機能はデバッグ専用です。強制実行によってDataStoreのステータスが書き換わったり、条件を満たしている場合は登録された連絡先へ「実際にSMSが送信」されます。パケット代や送信先に十分注意してください。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Text(
            text = "定期ジョブのシミュレート",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // 即時実行用カード
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "安否確認ロジックの強制実行",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1時間に1回 WorkManager から呼び出される executeCheck() を、バックグラウンドのタイマーを待たずに今すぐ実行します。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 状態インジケータ
                if (uiState is DeveloperUiState.Running) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Text(text = "executeCheck() を実行中...", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Button(
                    onClick = { viewModel.runSafetyCheckImmediately() },
                    enabled = uiState !is DeveloperUiState.Running,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("executeCheck() を即時実行")
                }
            }
        }
    }
}