package com.kurodai0715.autoemergencycall.ui.screen.contact

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ContactListScreen(
    viewModel: ContactViewModel,
    onNavigateToEdit: (String?) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val contactList by viewModel.contacts.collectAsState()

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 左側：戻るボタン
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("戻る")
                }

                // 右側：新規追加ボタン
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

            // 💡 スタイルと改行を適用した説明文エリア
            Column(
                modifier = Modifier.padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp) // 行間の隙間を少し広げる
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