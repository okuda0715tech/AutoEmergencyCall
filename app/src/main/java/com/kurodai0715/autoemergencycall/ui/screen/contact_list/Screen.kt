package com.kurodai0715.autoemergencycall.ui.screen.contact_list

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
    // 実際の遷移処理は外側（MainActivity等）で実装するため、IDを渡すラムダを用意
    // 新規追加時は null、編集時は String の ID が入る仕様
    onNavigateToEdit: (String?) -> Unit
) {
    val contactList by viewModel.contacts.collectAsState()

    Scaffold(
        bottomBar = {
            // 画面最下部に新規追加ボタンを配置
            Button(
                onClick = { onNavigateToEdit(null) }, // 新規なのでnullを渡す
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("新規連絡先を追加")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "緊急連絡先一覧",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (contactList.isEmpty()) {
                    item { Text("登録されている連絡先はありません。") }
                } else {
                    items(contactList) { contact ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToEdit(contact.id) } // タップ時にIDを渡す
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "${contact.name} (${contact.relation})", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "TEL: ${contact.phoneNumber}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}