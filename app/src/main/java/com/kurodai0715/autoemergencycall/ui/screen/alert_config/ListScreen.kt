package com.kurodai0715.autoemergencycall.ui.screen.alert_config

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
fun ConfigListScreen(
    viewModel: ConfigViewModel,
    onNavigateToEdit: (String?) -> Unit
) {
    val configs by viewModel.alertConfigs.collectAsState()
    val contacts by viewModel.availableContacts.collectAsState()

    Scaffold(
        bottomBar = {
            Button(
                onClick = { onNavigateToEdit(null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("新しい動作設定を追加")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)
        ) {
            Text(text = "動作・アラート設定", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 8.dp))

            // 💡 デフォルト状態の明示
            if (configs.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "現在のステータス: デフォルト動作", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "動作設定が未登録のため、万が一の際は「すべての連絡先」に対して一律「48時間後」にSMSが送信されます。", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(configs) { config ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToEdit(config.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "${config.thresholdHours} 時間後 に通知", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))

                            // 紐づいている連絡先名の解決
                            val targetNames = contacts.filter { config.targetContactIds.contains(it.id) }.map { it.name }
                            Text(
                                text = "対象連絡先: ${if (targetNames.isEmpty()) "なし" else targetNames.joinToString(", ")}",
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