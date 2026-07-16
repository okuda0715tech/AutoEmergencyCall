package com.kurodai0715.autoemergencycall.ui.screen.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kurodai0715.autoemergencycall.data.ProfileStore

@Composable
fun ProfileScreen(
    onSaveSuccess: () -> Unit, // 保存成功時の画面遷移などのコールバック
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profileStore = remember { ProfileStore(context) }

    // 💡 初期値として、保存されている名前をロード
    var userName by remember { mutableStateOf(profileStore.getUserName()) }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "利用者の名前登録",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "緊急連絡SMSの本文に、ここで登録したお名前（例:「〇〇 太郎」さんの安否確認をお願い致します。）が記載されます。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 💡 名前入力欄
        OutlinedTextField(
            value = userName,
            onValueChange = {
                userName = it
                if (isError) isError = false
            },
            label = { Text("お名前") },
            placeholder = { Text("例: 山田 太郎") },
            singleLine = true,
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                if (isError) {
                    Text("名前を入力してください", color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // 💡 押しやすい縦並びの保存ボタン
        Button(
            onClick = {
                if (userName.isBlank()) {
                    isError = true
                } else {
                    profileStore.saveUserName(userName)
                    onSaveSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("設定を保存する")
        }
    }
}