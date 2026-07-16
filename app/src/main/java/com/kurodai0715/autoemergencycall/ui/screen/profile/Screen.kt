package com.kurodai0715.autoemergencycall.ui.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold // 💡 追加
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kurodai0715.autoemergencycall.R
import com.kurodai0715.autoemergencycall.data.ProfileStore

@Composable
fun ProfileScreen(
    onSaveSuccess: () -> Unit, // 保存成功時の画面遷移などのコールバック
    onNavigateBack: () -> Unit, // 戻るボタンタップ時のコールバック
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profileStore = remember { ProfileStore(context) }

    // 初期値として、保存されている名前をロード
    var userName by remember { mutableStateOf(profileStore.getUserName()) }
    var isError by remember { mutableStateOf(false) }

    // コンテンツエリアのスクロール状態（キーボード表示時のスクロール対策）
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
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
                    Text(stringResource(R.string.profile_btn_cancel))
                }

                // 右側：保存ボタン
                Button(
                    onClick = {
                        if (userName.isBlank()) {
                            isError = true
                        } else {
                            profileStore.saveUserName(userName)
                            onSaveSuccess()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.profile_btn_save))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // タイトル
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // 説明文
            Text(
                text = stringResource(R.string.profile_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 名前入力欄
            OutlinedTextField(
                value = userName,
                onValueChange = {
                    userName = it
                    if (isError) isError = false
                },
                label = { Text(stringResource(R.string.profile_label_name)) },
                placeholder = { Text(stringResource(R.string.profile_placeholder_name)) },
                singleLine = true,
                isError = isError,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (isError) {
                        Text(
                            text = stringResource(R.string.profile_error_empty),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    }
}