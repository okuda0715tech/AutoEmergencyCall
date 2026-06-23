package com.kurodai0715.autoemergencycall.ui.screen.alert_config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ConfigEditScreen(
    configId: String?,
    viewModel: ConfigViewModel,
    onNavigateBack: () -> Unit
) {
    val existingConfig = remember(configId) { viewModel.getConfigById(configId) }
    val availableContacts by viewModel.availableContacts.collectAsState()

    // 入力フォーム状態
    var hoursInput by remember { mutableStateOf(existingConfig?.thresholdHours?.toString() ?: "") }
    // 選択された連絡先IDのセット（複数選択の管理用）
    var selectedContactIds by remember { mutableStateOf(existingConfig?.targetContactIds?.toSet() ?: emptySet()) }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("完了") },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false; onNavigateBack() }) { Text("OK") }
            }
        )
    }

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 1. 戻る
                    OutlinedButton(onClick = onNavigateBack, modifier = Modifier.weight(1f)) { Text("戻る") }

                    // 2. 削除
                    Button(
                        onClick = {
                            if (configId != null) {
                                viewModel.deleteConfig(configId) {
                                    dialogMessage = "動作設定を削除しました。"
                                    showSuccessDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        enabled = configId != null,
                        modifier = Modifier.weight(1f)
                    ) { Text("削除") }

                    // 3. 保存
                    Button(
                        onClick = {
                            val hours = hoursInput.toIntOrNull()
                            if (hours != null) {
                                viewModel.saveConfig(
                                    id = configId,
                                    thresholdHours = hours,
                                    targetContactIds = selectedContactIds.toList()
                                ) {
                                    dialogMessage = if (configId == null) "動作設定を新規登録しました。" else "動作設定を更新しました。"
                                    showSuccessDialog = true
                                }
                            }
                        },
                        enabled = hoursInput.toIntOrNull() != null && selectedContactIds.isNotEmpty(), // 時間正当性 ＆ 1件以上選択必須
                        modifier = Modifier.weight(1f)
                    ) { Text("保存") }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = if (configId == null) "動作設定の新規追加" else "動作設定の編集", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = hoursInput,
                onValueChange = { hoursInput = it },
                label = { Text("何時間後にアラートを出すか（数値）") },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text(text = "送信対象の連絡先を選択（複数選択可）", style = MaterialTheme.typography.titleMedium)

            if (availableContacts.isEmpty()) {
                Text(text = "先に「連絡先一覧」画面から緊急連絡先を登録してください。", color = MaterialTheme.colorScheme.error)
            }

            // 連絡先選択用のチェックボックス一覧
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                items(availableContacts) { contact ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedContactIds.contains(contact.id),
                            onCheckedChange = { isChecked ->
                                selectedContactIds = if (isChecked) {
                                    selectedContactIds + contact.id
                                } else {
                                    selectedContactIds - contact.id
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${contact.name} (${contact.relation})", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}