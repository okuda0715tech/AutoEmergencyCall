package com.kurodai0715.autoemergencycall.ui.screen.contact

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun ContactListScreen(
    viewModel: ContactViewModel,
    onNavigateToEdit: (String?) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val contactList by viewModel.contacts.collectAsState()

    // ViewModel から通知権限の状態を監視
    val isNotificationPermissionGranted by viewModel.isNotificationPermissionGranted.collectAsState()

    // 権限リクエスト用のランチャー
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // ダイアログの結果を受けて ViewModel の状態を更新
        viewModel.checkNotificationPermission(context)
    }

    // 画面表示時に現在の権限状態を一度チェックする
    LaunchedEffect(Unit) {
        viewModel.checkNotificationPermission(context)
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("戻る")
                }

                Button(
                    onClick = { onNavigateToEdit(null) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("追加")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // タイトル
            Text(
                text = "連絡先の登録・管理",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // スタイルと改行を適用した説明文エリア
            Column(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "万が一の際に通知を行う連絡先です。",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "・初期設定では登録されている全員にSMSが送信されます。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "・『動作・アラート設定』から送信先を個別に指定することも可能です。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 通知権限の案内カード (Android 13以上、かつ未許可の場合のみ表示)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isNotificationPermissionGranted) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "【重要】通知の許可をお願いします",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "万が一の際、SMSが正常に送信されたことをリアルタイムに確認するために通知権限が必要です。拒否されたままだと、送信完了メッセージが届きません。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("通知を許可する")
                        }
                    }
                }
            }

            // 連絡先リスト
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (contactList.isEmpty()) {
                    item {
                        Text(
                            text = "登録されている連絡先はありません。",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    items(contactList) { contact ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToEdit(contact.id) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val displayText = if (contact.relation.isNotBlank()) {
                                    "${contact.name} (${contact.relation})"
                                } else {
                                    contact.name
                                }

                                Text(
                                    text = displayText,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "TEL: ${contact.phoneNumber}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}