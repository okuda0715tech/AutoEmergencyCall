package com.kurodai0715.autoemergencycall.ui.screen.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun HomeScreen(
    onNavigateToContacts: () -> Unit,
    onNavigateToConfigs: () -> Unit,
    onNavigateToTest: () -> Unit // 👈 送信テスト画面への遷移
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollState = rememberScrollState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // ① SMS権限の状態
    var isSmsPermissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
    }

    // ② 💡【ここで使用】自動削除が無効（安全な状態）かどうかを管理するState
    var isAutoRevokeDisabled by remember { mutableStateOf(true) }

    // 画面の更新タイミングを制御するためのカウンターキー
    var refreshTrigger by remember { mutableStateOf(0) }

    // ユーザーが設定アプリから戻ってきた（ON_RESUME）ら状態チェックを促す
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isSmsPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                refreshTrigger++ // トリガーを引いて LaunchedEffect を動かす
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // バックグラウンドで自動リセットのステータスを安全に取得
    LaunchedEffect(refreshTrigger) {
        val status = checkUnusedAppRestrictionsStatus(context)
        // 1: 機能なし(古いOS), 2: DISABLED(自動削除オフで安全) の場合は true（＝警告不要）
        isAutoRevokeDisabled = (status == 2 || status == 1)
    }

    // 永久拒否時のダイアログ制御
    var showSettingsGuideDialog by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isSmsPermissionGranted = isGranted
        if (!isGranted && activity != null) {
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.SEND_SMS)
            if (!showRationale) {
                showSettingsGuideDialog = true
            }
        }
    }

    // 永久拒否された高齢者を救うための専用案内ダイアログ
    if (showSettingsGuideDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsGuideDialog = false },
            title = { Text("権限の設定が必要です") },
            text = { Text("ボタンがブロックされてしまいました。自動SMS送信を有効にするには、次の画面で【許可】を選んでください。設定画面へ直接移動します。") },
            confirmButton = {
                Button(
                    onClick = {
                        showSettingsGuideDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
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
            text = "本アプリは、一人暮らしの高齢者や持病をお持ちの方の万が一の事態を検知するためのアプリです。端末の操作や活動が一定時間検知できない場合に、事前に登録されたご家族や関係者へ自動的に安否確認の連絡を行います。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        // Google Play審査対応：目立つ事前開示カード（完全復活版）
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "⚠️ 重要：SMS送信権限の利用について", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Text(text = "当アプリの核心機能である「孤独死・遭難時の自動緊急通報」を実現するためには、SMS（ショートメッセージ）の送信権限が必要です。", style = MaterialTheme.typography.bodySmall)

                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "【警告】生存確認のタイマーが超過した場合、ユーザーの追加操作を一切必要とせず、アプリが『バックグラウンドで完全自動』で設定された連絡先へ救助要請のSMSを送信します。これにより通話・通信料が発生する場合があります。",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Text(text = "※お預かりした連絡先情報およびSMS送信機能は、上記の安否確認アラートの送信以外の目的で利用されることは一切ありません。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // SMS状態確認 ＆ タップ処理
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSmsPermissionGranted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (!isSmsPermissionGranted) {
                        if (activity != null) {
                            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.SEND_SMS)
                            val hasRequestedBefore = context.getSharedPreferences("prefs", Context.MODE_PRIVATE).getBoolean("has_requested_sms", false)

                            if (!showRationale && hasRequestedBefore) {
                                showSettingsGuideDialog = true
                            } else {
                                context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putBoolean("has_requested_sms", true).apply()
                                requestPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                            }
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
                    Text(text = if (isSmsPermissionGranted) "自動通報システムが有効です。" else "タップして権限を許可しないと、緊急SMSは送信されません。", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // 💡 【修正点】自動削除が有効（危険状態：false）のときだけ、警告カードをしっかり表示！
        if (!isAutoRevokeDisabled) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // ユーザーを設定画面の適切な場所へエスコート
                        openUnusedAppRestrictionsSettings(context)
                    }
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "⚠️ 警告：アプリの自動停止が有効です", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text(text = "数ヶ月アプリを開かないと、スマホの制限により緊急SMSが送れなくなります。タップして次の画面で【未使用のアプリを一時停止する】を必ず「オフ」にしてください。", style = MaterialTheme.typography.bodySmall)
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

        // 送信テスト画面へのアウトラインボタン
        OutlinedButton(
            onClick = onNavigateToTest,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("送信テストを実行する")
        }
    }
}

/**
 * 権限自動リセットの状態チェック（非同期呼び出し用）
 */
private fun checkUnusedAppRestrictionsStatus(context: Context): Int {
    return try {
        PackageManagerCompat.getUnusedAppRestrictionsStatus(context).get()
    } catch (e: Exception) {
        0
    }
}

/**
 * 自動リセット解除のための設定画面ワープ関数
 */
private fun openUnusedAppRestrictionsSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val intent = Intent("android.settings.UNUSED_APP_RESTRICTIONS").apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(fallbackIntent)
        }
    }
}