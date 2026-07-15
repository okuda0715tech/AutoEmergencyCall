package com.kurodai0715.autoemergencycall.ui.screen.alert_config

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kurodai0715.autoemergencycall.R

@OptIn(ExperimentalLayoutApi::class) // FlowRow を使用するために追加
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
    // 入力された時間が24時間以上かどうかを判定
    val inputHours = hoursInput.toIntOrNull()
    val isHoursValid = inputHours != null && inputHours >= 24

    // 選択された連絡先IDのセット（複数選択の管理用）
    var selectedContactIds by remember {
        mutableStateOf(
            existingConfig?.targetContactIds?.toSet() ?: emptySet()
        )
    }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    // コンテンツエリアのスクロール状態
    val scrollState = rememberScrollState()

    // 各完了時のメッセージ文字列をコンポーザブル内で解決できるように定義
    val messageAdd = stringResource(R.string.config_edit_dialog_msg_add)
    val messageUpdate = stringResource(R.string.config_edit_dialog_msg_update)
    val messageDelete = stringResource(R.string.config_edit_dialog_msg_delete)

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.config_edit_dialog_title)) },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false; onNavigateBack() }) {
                    Text(stringResource(R.string.config_edit_dialog_btn_ok))
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3
                ) {
                    // 1. 戻る
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.widthIn(min = 100.dp).weight(1f)
                    ) {
                        Text(text = stringResource(R.string.config_edit_btn_back), maxLines = 1)
                    }

                    // 2. 削除
                    Button(
                        onClick = {
                            if (configId != null) {
                                viewModel.deleteConfig(configId) {
                                    dialogMessage = messageDelete
                                    showSuccessDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        enabled = configId != null,
                        modifier = Modifier.widthIn(min = 100.dp).weight(1f)
                    ) {
                        Text(text = stringResource(R.string.config_edit_btn_delete), maxLines = 1)
                    }

                    // 3. 保存
                    Button(
                        onClick = {
                            val hours = hoursInput.toIntOrNull()
                            if (hours != null && hours >= 24) {
                                viewModel.saveConfig(
                                    id = configId,
                                    thresholdHours = hours,
                                    targetContactIds = selectedContactIds.toList()
                                ) {
                                    dialogMessage =
                                        if (configId == null) messageAdd else messageUpdate
                                    showSuccessDialog = true
                                }
                            }
                        },
                        // 24時間以上 ＆ 1件以上選択必須
                        enabled = isHoursValid && selectedContactIds.isNotEmpty(),
                        modifier = Modifier.widthIn(min = 100.dp).weight(1f)
                    ) {
                        Text(text = stringResource(R.string.config_edit_btn_save), maxLines = 1)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // スクロール可能にする
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (configId == null) {
                    stringResource(R.string.config_edit_title_add)
                } else {
                    stringResource(R.string.config_edit_title_edit)
                },
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = hoursInput,
                onValueChange = { hoursInput = it },
                label = { Text(stringResource(R.string.config_edit_label_hours)) },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = hoursInput.isNotEmpty() && !isHoursValid,
                supportingText =
                    if (hoursInput.isNotEmpty() && !isHoursValid) {
                        { // この中カッコは supportingText にコンポーザブル関数を渡すために必要
                            Text(
                                text = stringResource(R.string.config_edit_error_hours_format),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        // 正常時は supportingText 表示用のスペースを確保しないよう null を渡す
                        null
                    },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.config_edit_label_select_contacts),
                style = MaterialTheme.typography.titleMedium
            )

            if (availableContacts.isEmpty()) {
                Text(
                    text = stringResource(R.string.config_edit_error_no_contacts),
                    color = MaterialTheme.colorScheme.error
                )
            }

            // 一覧の表示に LazyColumn は使わず Column を使う。
            // LazyColumn のスクロールが画面全体のスクロールと衝突して使えないため。
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                availableContacts.forEach { contact ->
                    val isChecked = selectedContactIds.contains(contact.id)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedContactIds = if (isChecked) {
                                    selectedContactIds - contact.id
                                } else {
                                    selectedContactIds + contact.id
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            // Row側のclickableと干渉しないよう、チェックボックス単体のコールバックは null に設定
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        val displayText = if (contact.relation.isEmpty()) {
                            contact.name
                        } else {
                            stringResource(
                                R.string.config_edit_contact_display_with_relation,
                                contact.name,
                                contact.relation
                            )
                        }
                        Text(text = displayText, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}