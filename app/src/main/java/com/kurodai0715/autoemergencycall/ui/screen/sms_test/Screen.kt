package com.kurodai0715.autoemergencycall.ui.screen.sms_test

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kurodai0715.autoemergencycall.R
import com.kurodai0715.autoemergencycall.data.Contact

@OptIn(ExperimentalLayoutApi::class) // FlowRowを使用するために追加
@Composable
fun TestSmsScreen(
    viewModel: TestSmsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val contacts by viewModel.contacts.collectAsState()
    var selectedContact by remember { mutableStateOf<Contact?>(null) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    // コンテンツエリアのスクロール状態
    val scrollState = rememberScrollState()

    // 各結果時のメッセージテンプレートをコンポーザブル内で簡単に呼び出せるよう取得
    val messageSuccess = stringResource(R.string.test_sms_dialog_msg_success, selectedContact?.name ?: "")
    val messageFailure = stringResource(R.string.test_sms_dialog_msg_failure)

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.test_sms_dialog_title)) },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.test_sms_dialog_btn_ok))
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            // 縦に並んだ際の間隔も担保するため Column のパディングを16.dpに
            Column(modifier = Modifier.padding(16.dp)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 2
                ) {
                    // 戻るボタン
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .widthIn(min = 140.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.test_sms_btn_back),
                            maxLines = 1
                        )
                    }

                    // 送信するボタン
                    Button(
                        onClick = {
                            selectedContact?.let { contact ->
                                viewModel.sendTestSms(contact) { success ->
                                    dialogMessage = if (success) messageSuccess else messageFailure
                                    showDialog = true
                                }
                            }
                        },
                        enabled = selectedContact != null,
                        modifier = Modifier
                            .widthIn(min = 140.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.test_sms_btn_send),
                            maxLines = 1
                        )
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
                text = stringResource(R.string.test_sms_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // ストア審査対策 ＆ 誤操作防止の注意書き
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.test_sms_card_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.test_sms_card_desc),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Text(
                text = stringResource(R.string.test_sms_label_select_recipient),
                style = MaterialTheme.typography.titleMedium
            )

            if (contacts.isEmpty()) {
                Text(
                    text = stringResource(R.string.test_sms_error_no_contacts),
                    color = MaterialTheme.colorScheme.error
                )
            }

            // 一覧の表示に LazyColumn は使わず Column を使う。
            // LazyColumn のスクロールが画面全体のスクロールと衝突して使えないため。
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                contacts.forEach { contact ->
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
                            // "関係性 : 電話番号" のフォーマット文字列リソースを適用
                            Text(
                                text = stringResource(
                                    R.string.test_sms_contact_detail_format,
                                    contact.relation,
                                    contact.phoneNumber
                                ),
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