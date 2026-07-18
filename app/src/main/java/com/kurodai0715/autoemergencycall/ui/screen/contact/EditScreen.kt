package com.kurodai0715.autoemergencycall.ui.screen.contact

import androidx.compose.foundation.horizontalScroll // 横スクロール用に追加
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kurodai0715.autoemergencycall.R

@OptIn(ExperimentalLayoutApi::class) // FlowRow を使用するために追加
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
    var relationInput by remember { mutableStateOf(existingContact?.relation ?: "") }

    // 電話番号の入力欄を3つに分けるためのステート
    var phoneInput1 by remember { mutableStateOf("") }
    var phoneInput2 by remember { mutableStateOf("") }
    var phoneInput3 by remember { mutableStateOf("") }

    // 既存データがある場合に3つの入力欄へ分解してセット
    LaunchedEffect(existingContact) {
        val fullPhone = existingContact?.phoneNumber ?: ""
        if (fullPhone.length >= 11) {
            phoneInput1 = fullPhone.substring(0, 3)
            phoneInput2 = fullPhone.substring(3, 7)
            phoneInput3 = fullPhone.substring(7, 11)
        } else {
            phoneInput1 = fullPhone
        }
    }

    // 3つの入力欄を結合した全体の電話番号
    val phoneInput = phoneInput1 + phoneInput2 + phoneInput3

    // コンテンツエリアのスクロール状態を管理するステートを記憶
    val scrollState = rememberScrollState()
    // 電話番号の横スクロール状態を管理するステートを記憶
    val phoneHorizontalScrollState = rememberScrollState()

    // 電話番号が半角数字のみで構成されているかチェック
    // 空文字のときはエラーにしない（未入力は別途保存ボタン側でガード）
    val isPhoneValid =
        phoneInput.isEmpty() || (phoneInput1.all { it.isDigit() } && phoneInput2.all { it.isDigit() } && phoneInput3.all { it.isDigit() })

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
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp), // 縦に並んだときの隙間
                    maxItemsInEachRow = 3 // 1行に最大3つまで（通常時は横並び）
                ) {
                    // 各ボタンの widthIn(min = ...) は小さくなりすぎて押しにくくなるのを防ぐ
                    // ボタン内の Text に maxLines = 1 を指定し、文字が縦に並ぶのを防ぐ

                    // 1. 戻るボタン
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .widthIn(min = 100.dp)
                            .weight(1f)
                    ) {
                        Text(text = stringResource(R.string.contact_edit_btn_back), maxLines = 1)
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
                        modifier = Modifier
                            .widthIn(min = 100.dp)
                            .weight(1f)
                    ) {
                        Text(text = stringResource(R.string.contact_edit_btn_delete), maxLines = 1)
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
                        enabled = nameInput.isNotBlank() && phoneInput1.isNotBlank() && phoneInput2.isNotBlank() && phoneInput3.isNotBlank() && isPhoneValid,
                        modifier = Modifier
                            .widthIn(min = 100.dp)
                            .weight(1f)
                    ) {
                        Text(text = stringResource(R.string.contact_edit_btn_save), maxLines = 1)
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
            // タイトル
            Text(
                text = if (contactId == null) {
                    stringResource(R.string.contact_edit_title_add)
                } else {
                    stringResource(R.string.contact_edit_title_edit)
                },
                style = MaterialTheme.typography.headlineMedium
            )

            // 名前
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

            // 電話番号
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // ラベルを別のUIとして切り出し
                Text(
                    text = stringResource(R.string.contact_edit_label_phone),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (!isPhoneValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 横スクロールできるように Row に horizontalScroll を適用
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(phoneHorizontalScrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = phoneInput1,
                        onValueChange = { if (it.length <= 4) phoneInput1 = it },
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone, // 電話番号用キーボード
                            imeAction = ImeAction.Next
                        ),
                        isError = !isPhoneValid,
                        // 横スクロール内では等倍率での自動縮小を防ぐため、固定幅を確保
                        modifier = Modifier.width(110.dp)
                    )
                    Text("-")
                    OutlinedTextField(
                        value = phoneInput2,
                        onValueChange = { if (it.length <= 4) phoneInput2 = it },
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone, // 電話番号用キーボード
                            imeAction = ImeAction.Next
                        ),
                        isError = !isPhoneValid,
                        modifier = Modifier.width(110.dp)
                    )
                    Text("-")
                    OutlinedTextField(
                        value = phoneInput3,
                        onValueChange = { if (it.length <= 4) phoneInput3 = it },
                        singleLine = true,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone, // 電話番号用キーボード
                            imeAction = ImeAction.Next
                        ),
                        isError = !isPhoneValid,
                        modifier = Modifier.width(110.dp)
                    )
                }

                if (!isPhoneValid) {
                    Text(
                        text = stringResource(R.string.contact_edit_error_phone_format),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // 関係性
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