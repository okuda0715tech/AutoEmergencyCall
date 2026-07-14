package com.kurodai0715.autoemergencycall.ui.screen.contact

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kurodai0715.autoemergencycall.R

@Composable
fun ContactListScreen(
    viewModel: ContactViewModel,
    onNavigateToEdit: (String?) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val contactList by viewModel.contacts.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // ViewModel から通知権限の状態を監視
    val isNotificationPermissionGranted by viewModel.isNotificationPermissionGranted.collectAsState()

    // 通知用の設定案内ダイアログの表示管理フラグ
    var showNotificationSettingsGuideDialog by remember { mutableStateOf(false) }

    // 権限リクエスト用のランチャー
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // ダイアログの結果を受けて ViewModel の状態を更新
        viewModel.checkNotificationPermission(context)

        // 拒否された場合の永久拒否判定ロジックを追加
        if (!isGranted && activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )

            // 根拠（Rationale）が表示されない ＝ ユーザーが「今後表示しない」を選択、または完全にブロックしている状態
            if (!showRationale) {
                showNotificationSettingsGuideDialog = true
            }
        }
    }

    // 【修正】LaunchedEffect(Unit) を削除し、ON_RESUME を監視するこちらに差し替えます
    // これにより、設定画面からアプリのこの画面に戻ってきた瞬間（ON_RESUME）に自動で再チェックが走ります
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkNotificationPermission(context)
                viewModel.loadContacts() // ついでに連絡先リストも最新にリフレッシュしておくと安全です
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 通知用の永久拒否エスコートダイアログ
    if (showNotificationSettingsGuideDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationSettingsGuideDialog = false },
            title = { Text(stringResource(R.string.contacts_permission_dialog_title)) },
            text = { Text(stringResource(R.string.contacts_permission_dialog_desc)) },
            confirmButton = {
                Button(
                    onClick = {
                        showNotificationSettingsGuideDialog = false
                        // ホーム画面と同様に、アプリ詳細設定（システムの設定画面）を開くインテントを実行
                        // （※HomeViewModel内の既存メソッド、または直接インテントを生成してもOKです）
                        val intent =
                            android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .apply {
                                    data = android.net.Uri.fromParts(
                                        "package",
                                        context.packageName,
                                        null
                                    )
                                }
                        context.startActivity(intent)
                    }
                ) { Text(stringResource(R.string.contacts_permission_dialog_btn_open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNotificationSettingsGuideDialog = false
                }) { Text(stringResource(R.string.contacts_permission_dialog_btn_cancel)) }
            }
        )
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.contacts_btn_back))
                }

                Button(
                    onClick = { onNavigateToEdit(null) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.contacts_btn_add))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // タイトル
            Text(
                text = stringResource(R.string.contacts_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // スタイルと改行を適用した説明文エリア
            Column(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.contacts_desc_main),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.contacts_desc_sub_sms),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.contacts_desc_sub_config),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 通知権限の案内カード (Android 13以上、かつ未許可の場合のみ表示)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isNotificationPermissionGranted) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.contacts_permission_card_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.contacts_permission_card_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (activity != null) {
                                    val showRationale =
                                        ActivityCompat.shouldShowRequestPermissionRationale(
                                            activity,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    val hasRequestedBefore =
                                        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                                            .getBoolean("has_requested_notification", false)

                                    // 既に永久拒否されている場合は直接エスコートダイアログを出す
                                    if (!showRationale && hasRequestedBefore) {
                                        showNotificationSettingsGuideDialog = true
                                    } else {
                                        // 初回または1回目拒否の通常フロー（SharedPreferencesにリクエスト実績を記録）
                                        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                                            .edit {
                                                putBoolean(
                                                    "has_requested_notification",
                                                    true
                                                )
                                            }
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.contacts_permission_card_btn_allow))
                        }
                    }
                }
            }

            // 連絡先リスト
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (contactList.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.contacts_empty_list),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    items(contactList) { contact ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToEdit(contact.id) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val displayText = if (contact.relation.isNotBlank()) {
                                    stringResource(
                                        R.string.contacts_name_with_relation,
                                        contact.name,
                                        contact.relation
                                    )
                                } else {
                                    contact.name
                                }

                                Text(
                                    text = displayText,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(
                                        R.string.contacts_phone_label,
                                        contact.phoneNumber
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}