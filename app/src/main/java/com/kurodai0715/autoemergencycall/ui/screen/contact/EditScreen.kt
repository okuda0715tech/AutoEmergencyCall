package com.kurodai0715.autoemergencycall.ui.screen.contact

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kurodai0715.autoemergencycall.ui.screen.contact_list.ContactViewModel

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

    // ダイアログ制御用の状態
    var showSuccessDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    // キーボードのフォーカス（次へ移動、閉じるなど）を制御するためのマネージャー
    val focusManager = LocalFocusManager.current

    // 完了ダイアログ
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* 画面外タップは無視 */ },
            title = { Text("完了") },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack() // ダイアログを閉じたら一覧に戻る
                    }
                ) {
                    Text("OK")
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
                        Text("戻る")
                    }

                    // 2. 削除ボタン（既存編集時のみ有効化）
                    Button(
                        onClick = {
                            if (contactId != null) {
                                viewModel.deleteContact(contactId) {
                                    dialogMessage = "連絡先を削除しました。"
                                    showSuccessDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        enabled = contactId != null, // 新規のときは押せない
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("削除")
                    }

                    // 3. 保存ボタン
                    Button(
                        onClick = {
                            viewModel.saveContact(
                                id = contactId,
                                name = nameInput,
                                phoneNumber = phoneInput,
                                relation = relationInput
                            ) {
                                dialogMessage = if (contactId == null) "連絡先を新規登録しました。" else "連絡先を更新しました。"
                                showSuccessDialog = true
                            }
                        },
                        enabled = nameInput.isNotBlank() && phoneInput.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("保存")
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
                text = if (contactId == null) "緊急連絡先の新規追加" else "緊急連絡先の編集",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("名前") },
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
                label = { Text("電話番号") },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone, // 電話番号用キーボード
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = relationInput,
                onValueChange = { relationInput = it },
                label = { Text("関係性 (例: 長男、妻)") },
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