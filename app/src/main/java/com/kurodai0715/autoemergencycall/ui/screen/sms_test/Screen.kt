package com.kurodai0715.autoemergencycall.ui.screen.sms_test

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kurodai0715.autoemergencycall.data.Contact

@Composable
fun TestSmsScreen(
    viewModel: TestSmsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val contacts by viewModel.contacts.collectAsState()
    var selectedContact by remember { mutableStateOf<Contact?>(null) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("結果") },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("OK") }
            }
        )
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
                    onClick = {
                        selectedContact?.let { contact ->
                            viewModel.sendTestSms(contact) { success ->
                                dialogMessage = if (success) {
                                    "${contact.name} さんへテストSMSを送信しました。実際に届いているかご確認ください。"
                                } else {
                                    "送信に失敗しました。SMS送信権限が許可されているか、または電波状況をご確認ください。"
                                }
                                showDialog = true
                            }
                        }
                    },
                    enabled = selectedContact != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("送信する")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "SMS送信テスト",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // 💡 ストア審査対策 ＆ 誤操作防止の注意書き
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 テスト機能について",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "登録された連絡先へ実際にSMSが届くシステムテストを行えます。ボタンを押すと即座に送信され、ご契約のプランに応じたSMS送信料（通信料）が発生しますのでご注意ください。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Text(text = "送信先を1つ選択してください", style = MaterialTheme.typography.titleMedium)

            if (contacts.isEmpty()) {
                Text(
                    text = "連絡先が登録されていません。先に連絡先一覧から追加してください。",
                    color = MaterialTheme.colorScheme.error
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contacts) { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedContact = contact }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedContact?.id == contact.id,
                            onClick = { selectedContact = contact }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${contact.relation} : ${contact.phoneNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}