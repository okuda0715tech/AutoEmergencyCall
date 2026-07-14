package com.kurodai0715.autoemergencycall.ui.screen.contact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kurodai0715.autoemergencycall.R

@Composable
fun ContactEditScreen(
    contactId: String?, // 上記の一覧画面から渡されてくるID（nullなら新規）
    viewModel: ContactViewModel,
    onNavigateBack: () -> Unit // 前の画面に戻るためのナビゲーション処理
) {
    // IDを元に既存データを検索（新規ならnull）
    val existingContact = remember(contactId) { viewModel.getContactById(contactId) }

    // 入力フォームの状態（既存データがあれば初期値として入れる）
    var nameInput by remember { mutableStateOf(existingContact?.name ?: "") }
    var phoneInput by remember { mutableStateOf(existingContact?.phoneNumber ?: "") }
    var relationInput by remember { mutableStateOf(existingContact?.relation ?: "") }

    // 電話番号が半角数字のみで構成されているかチェック
    // 空文字のときはエラーにしない（未入力は別途保存ボタン側でガード）
    val isPhoneValid = phoneInput.isEmpty() || phoneInput.all { it.isDigit() }

    // ダイアログ制御用の状態
    var showSuccessDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    // 各完了時のメッセージ文字列をコンポーザブル内で解決できるように定義
    val messageAdd = stringResource(R.string.contact_edit_dialog_msg_add)
    val messageUpdate = stringResource(R.string.contact_edit_dialog_msg_update)
    val messageDelete = stringResource(R.string.contact_edit_dialog_msg_delete)

    // キーボードのフォーカス（次へ移動、閉じるなど）を制御するためのマネージャー
    val focusManager = LocalFocusManager.current

    // 完了ダイアログ
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* 画面外タップは無視 */ },
            title = { Text(stringResource(R.string.contact_edit_dialog_title)) },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack() // ダイアログを閉じたら一覧に戻る
                    }
                ) {
                    Text(stringResource(R.string.contact_edit_dialog_btn_ok))
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            // 編集画面の下部に3つのボタンを配置
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. 戻るボタン
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.contact_edit_btn_back))
                    }

                    // 2. 削除ボタン（既存編集時のみ有効化）
                    Button(
                        onClick = {
                            if (contactId != null) {
                                viewModel.deleteContact(contactId) {
                                    dialogMessage = messageDelete
                                    showSuccessDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        enabled = contactId != null, // 新規のときは押せない
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.contact_edit_btn_delete))
                    }

                    // 3. 保存ボタン
                    Button(
                        onClick = {
                            if (nameInput.isNotBlank() && phoneInput.isNotBlank() && phoneInput.all { it.isDigit() }) {
                                viewModel.saveContact(
                                    id = contactId,
                                    name = nameInput,
                                    phoneNumber = phoneInput,
                                    relation = relationInput
                                ) {
                                    dialogMessage =
                                        if (contactId == null) messageAdd else messageUpdate
                                    showSuccessDialog = true
                                }
                            }
                        },
                        enabled = nameInput.isNotBlank() && phoneInput.isNotBlank() && isPhoneValid,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.contact_edit_btn_save))
                    }
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
                text = if (contactId == null) {
                    stringResource(R.string.contact_edit_title_add)
                } else {
                    stringResource(R.string.contact_edit_title_edit)
                },
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text(stringResource(R.string.contact_edit_label_name)) },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next // キーボードの右下を「次へ」ボタンにする
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text(stringResource(R.string.contact_edit_label_phone)) },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone, // 電話番号用キーボード
                    imeAction = ImeAction.Next
                ),
                isError = !isPhoneValid,
                supportingText = {
                    if (!isPhoneValid) {
                        Text(
                            text = stringResource(R.string.contact_edit_error_phone_format),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = relationInput,
                onValueChange = { relationInput = it },
                label = { Text(stringResource(R.string.contact_edit_label_relation)) },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done // キーボードの右下を「完了」ボタンにする
                ),
                keyboardActions = KeyboardActions(
                    // 「完了」が押されたらフォーカスを外してキーボードを閉じる
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}