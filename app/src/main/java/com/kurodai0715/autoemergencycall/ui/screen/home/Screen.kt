package com.kurodai0715.autoemergencycall.ui.screen.home

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    onNavigateToContacts: () -> Unit,
    onNavigateToConfigs: () -> Unit,
    onNavigateToTest: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollState = rememberScrollState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val isSmsPermissionGranted by viewModel.isSmsPermissionGranted.collectAsState()
    val isAutoRevokeDisabled by viewModel.isAutoRevokeDisabled.collectAsState()
    val lastActiveTimeText by viewModel.lastActiveTimeText.collectAsState()
    val lastCheckTimeText by viewModel.lastCheckTimeText.collectAsState()

    var showProminentDisclosureDialog by remember { mutableStateOf(false) }
    var showSettingsGuideDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshStatuses(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.refreshStatuses(context)
        if (!isGranted && activity != null) {
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.SEND_SMS)
            if (!showRationale) {
                showSettingsGuideDialog = true
            }
        }
    }

    // Google Play審査対策 「目立つ事前開示」ダイアログ
    if (showProminentDisclosureDialog) {
        AlertDialog(
            onDismissRequest = { showProminentDisclosureDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(text = "SMS送信権限の利用について", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "当アプリの核心機能である「孤独死・遭難時の自動緊急通報」を実現するためには、SMS（ショートメッセージ）の送信権限が必要です。")

                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "【警告】生存確認のタイマーが超過した場合、ユーザーの追加操作を一切必要とせず、アプリが『バックグラウンドで完全自動』で登録された連絡先へ救助要請のSMSを送信します。これにより通話・通信料が発生する場合があります。",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Text(text = "※お預かりした連絡先情報およびSMS送信機能は、上記の安否確認アラートの送信以外の目的で利用されることは一切ありません。", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProminentDisclosureDialog = false
                        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("同意して権限を許可する")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProminentDisclosureDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }

    // 永久拒否時のエスコートダイアログ
    if (showSettingsGuideDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsGuideDialog = false },
            title = { Text("権限の再設定が必要です") },
            text = { Text("ボタンがブロックされてしまいました。自動SMS送信を有効にするには、次の画面で SMS 権限を許可してください。設定画面へ直接移動します。") },
            confirmButton = {
                Button(
                    onClick = {
                        showSettingsGuideDialog = false
                        val intent = viewModel.createApplicationDetailsIntent(context)
                        context.startActivity(intent)
                    }
                ) { Text("設定画面を開く") }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsGuideDialog = false }) { Text("キャンセル") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "自動安否確認システム", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Text(
            text = "一人暮らしの高齢者や持病をお持ちの方の万が一の事態を検知し、事前に登録されたご家族へ自動的に安否確認の連絡を行うシステムです。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        // 現在の稼働ステータス表示
        Text(text = "現在の稼働状態", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSmsPermissionGranted && isAutoRevokeDisabled)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📱 最終生存確認（充電など）", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(text = lastActiveTimeText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔄 最終生存チェック実施時刻", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(text = lastCheckTimeText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        // SMS状態確認カード
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSmsPermissionGranted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (!isSmsPermissionGranted && activity != null) {
                        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.SEND_SMS)
                        val hasRequestedBefore = context.getSharedPreferences("prefs", Context.MODE_PRIVATE).getBoolean("has_requested_sms", false)

                        if (!showRationale && hasRequestedBefore) {
                            showSettingsGuideDialog = true
                        } else {
                            context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putBoolean("has_requested_sms", true).apply()
                            showProminentDisclosureDialog = true
                        }
                    }
                }
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSmsPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isSmsPermissionGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = if (isSmsPermissionGranted) "SMS送信権限：許可済み" else "SMS送信権限：未許可（タップして有効化）", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = if (isSmsPermissionGranted) "自動通報システムが有効です。" else "タップして説明を確認し、機能を有効にしてください。", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // アプリ自動停止の警告カード
        if (!isAutoRevokeDisabled) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = viewModel.createUnusedAppRestrictionsIntent(context)
                        try { context.startActivity(intent) } catch (e: Exception) {
                            val fallback = viewModel.createApplicationDetailsIntent(context)
                            context.startActivity(fallback)
                        }
                    }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "警告：アプリの自動停止が有効です", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text(text = "数ヶ月アプリを開かないと、スマホの制限により緊急SMSが送れなくなります。タップして次の画面で【未使用のアプリの権限を削除する】を必ず「オフ」にしてください。", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "各種設定・管理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onNavigateToContacts, modifier = Modifier.weight(1f)) { Text("連絡先一覧") }
            Button(onClick = onNavigateToConfigs, modifier = Modifier.weight(1f)) { Text("動作設定一覧") }
        }

        OutlinedButton(
            onClick = onNavigateToTest,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("送信テストを実行する")
        }
    }
}