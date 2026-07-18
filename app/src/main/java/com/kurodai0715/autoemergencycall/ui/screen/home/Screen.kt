package com.kurodai0715.autoemergencycall.ui.screen.home

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kurodai0715.autoemergencycall.R

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
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

    // 解説ダイアログ表示制御用の状態
    var showActiveTimeInfo by remember { mutableStateOf(false) }
    var showCheckTimeInfo by remember { mutableStateOf(false) }

    val isMonitoringEnabled by viewModel.isMonitoringEnabled.collectAsState()
    var showStopConfirmDialog by remember { mutableStateOf(false) }
    var isConsentChecked by remember { mutableStateOf(false) }

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
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.SEND_SMS
            )
            if (!showRationale) {
                showSettingsGuideDialog = true
            }
        }
    }

    // Google Play審査対策 「目立つ事前開示」ダイアログ
    if (showProminentDisclosureDialog) {
        SmsPermissionDialog(
            onDismiss = { showProminentDisclosureDialog = false },
            onConfirm = {
                showProminentDisclosureDialog = false
                requestPermissionLauncher.launch(Manifest.permission.SEND_SMS)
            }
        )
    }

    // 永久拒否時のエスコートダイアログ
    if (showSettingsGuideDialog) {
        SettingsGuideDialog(
            onDismiss = { showSettingsGuideDialog = false },
            onConfirm = {
                showSettingsGuideDialog = false
                val intent = viewModel.createApplicationDetailsIntent(context)
                context.startActivity(intent)
            }
        )
    }

    // 「最終生存確認」の定義解説ダイアログ
    if (showActiveTimeInfo) {
        InfoDialog(
            title = stringResource(R.string.active_time_info_title),
            text = stringResource(R.string.active_time_info_text),
            iconColor = MaterialTheme.colorScheme.primary,
            onDismiss = { showActiveTimeInfo = false }
        )
    }

    // 「見守りチェック」の定義解説ダイアログ
    if (showCheckTimeInfo) {
        InfoDialog(
            title = stringResource(R.string.check_time_info_title),
            text = stringResource(R.string.check_time_info_text),
            iconColor = MaterialTheme.colorScheme.secondary,
            onDismiss = { showCheckTimeInfo = false }
        )
    }

    // 停止確認ダイアログ
    if (showStopConfirmDialog) {
        StopConfirmDialog(
            isConsentChecked = isConsentChecked,
            onConsentChange = { isConsentChecked = it },
            onDismiss = { showStopConfirmDialog = false },
            onConfirm = {
                viewModel.toggleMonitoringStatus(context, false) // 💡 停止を実行
                showStopConfirmDialog = false
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
        // タイトル
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 説明文
        Text(
            text = stringResource(R.string.home_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        // 現在の稼働ステータス表示
        Text(
            text = stringResource(R.string.home_section_status),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

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
                val statusText =
                    if (isMonitoringEnabled) stringResource(R.string.home_status_active) else stringResource(
                        R.string.home_status_paused
                    )
                val statusColor =
                    if (isMonitoringEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                val guideText =
                    if (isMonitoringEnabled) stringResource(R.string.home_guide_to_pause) else stringResource(
                        R.string.home_guide_to_resume
                    )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onLongClick = {
                                if (isMonitoringEnabled) {
                                    isConsentChecked = false
                                    showStopConfirmDialog = true
                                } else {
                                    viewModel.toggleMonitoringStatus(context, true)
                                }
                            },
                            onClick = {}
                        )
                        .padding(vertical = 6.dp),
                    // アイテムとアイテムの間に均等なスペースを配置する
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // 左側のステータス（アイコン＋テキスト）
                    Row(
                        // FlowRow 内で各アイテムの高さが異なる場合に、アイテムを垂直方向で中央寄せする
                        modifier = Modifier.align(Alignment.CenterVertically),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!isMonitoringEnabled) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = stringResource(R.string.home_status_paused_desc),
                                tint = Color(0xFFFBC02D)
                            )
                        } else {
                            Text("●", color = statusColor)
                        }

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Text(
                        // FlowRow 内で各アイテムの高さが異なる場合に、アイテムを垂直方向で中央寄せする
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = guideText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(top = 2.dp, bottom = 6.dp))

                // 1. 最終活動検知の行
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val infoIcon = "info_icon"
                    // テキストとインラインアイコンの位置を定義。
                    // Annotated とは、装飾されたという意味。
                    // buildAnnotatedString は StringBuilder の装飾有り版。
                    val annotatedString = buildAnnotatedString {
                        append(stringResource(R.string.home_label_active_time))
                        append(" ") // テキストとアイコンの間にわずかな隙間を入れる
                        appendInlineContent(infoIcon, "[info]")
                    }

                    // アイコンの見た目とサイズを定義
                    val inlineContent = mapOf(
                        infoIcon to InlineTextContent(
                            // 埋め込みたい場所に確保する空間のサイズ（Placeholder）を決める
                            Placeholder(
                                width = 20.sp, // 文字サイズに連動するようspで指定
                                height = 20.sp,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(R.string.home_content_description_info),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), // 💡 active側のカラーを指定
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    )

                    // 左側：テキスト＋文末アイコン
                    Text(
                        text = annotatedString,
                        inlineContent = inlineContent,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { showActiveTimeInfo = true }
                            .align(Alignment.CenterVertically)
                    )

                    // 右側：時刻テキスト
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = lastActiveTimeText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 2. 見守りチェック実施の行
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val infoIcon = "info_icon"

                    val annotatedString = buildAnnotatedString {
                        append(stringResource(R.string.home_label_check_time))
                        append(" ") // テキストとアイコンの間にわずかな隙間を入れる
                        appendInlineContent(infoIcon, "[info]") // アイコンを差し込む位置の目印
                    }

                    // アイコンの見た目とサイズを定義
                    val inlineContent = mapOf(
                        infoIcon to InlineTextContent(
                            // テキストの大きさに合わせてアイコンのサイズ（Placeholder）を決める
                            Placeholder(
                                width = 20.sp, // 文字サイズ（sp）に連動させると最大化時も綺麗です
                                height = 20.sp,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.Center // 垂直中央揃え
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(R.string.home_content_description_info),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxSize() // Placeholderのサイズいっぱいに広げる
                            )
                        }
                    )

                    // 左側：テキスト＋文末アイコン
                    Text(
                        text = annotatedString,
                        inlineContent = inlineContent,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { showCheckTimeInfo = true }
                            .align(Alignment.CenterVertically)
                    )

                    // 右側：時刻テキスト
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = lastCheckTimeText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // SMS状態確認カード
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSmsPermissionGranted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer.copy(
                    alpha = 0.4f
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (!isSmsPermissionGranted && activity != null) {
                        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            Manifest.permission.SEND_SMS
                        )
                        val hasRequestedBefore =
                            context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                                .getBoolean("has_requested_sms", false)

                        if (!showRationale && hasRequestedBefore) {
                            showSettingsGuideDialog = true
                        } else {
                            context.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit()
                                .putBoolean("has_requested_sms", true).apply()
                            showProminentDisclosureDialog = true
                        }
                    }
                }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSmsPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isSmsPermissionGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isSmsPermissionGranted) stringResource(R.string.home_sms_permission_granted) else stringResource(
                            R.string.home_sms_permission_denied
                        ), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isSmsPermissionGranted) stringResource(R.string.home_sms_permission_granted_desc) else stringResource(
                            R.string.home_sms_permission_denied_desc
                        ), style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // アプリ自動停止の警告カード
        if (!isAutoRevokeDisabled) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(
                        alpha = 0.6f
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = viewModel.createUnusedAppRestrictionsIntent(context)
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val fallback = viewModel.createApplicationDetailsIntent(context)
                            context.startActivity(fallback)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.home_auto_revoke_warning_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(R.string.home_auto_revoke_warning_desc),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.home_section_management),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // 文字数が多いボタンの集合のため、 FlowRow は使わず、最初から Column で実装する。
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp) // ボタン同士の縦の隙間
        ) {
            // 利用者名の登録ボタン
            Button(
                onClick = onNavigateToProfile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.home_btn_profile),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // 緊急連絡先の登録ボタン（「連絡先設定」）
            Button(
                onClick = onNavigateToContacts,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.home_btn_contacts),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // 見守り動作設定ボタン（「アラート設定」）
            Button(
                onClick = onNavigateToConfigs,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.home_btn_configs),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // SMS送信テストボタン（「送信テストはこちら」）
            OutlinedButton(
                onClick = onNavigateToTest,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.home_btn_test),
                    maxLines = 2, // これだけは文字が長いため、極大フォント時にも備えて2行まで折り返し可能に
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// ==========================================
// 以下、切り出したダイアログコンポーネント群
// ==========================================

@Composable
private fun SmsPermissionDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val dialogScrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(R.string.sms_permission_dialog_title),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(dialogScrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = stringResource(R.string.sms_permission_dialog_overview))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.sms_permission_dialog_warning),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.sms_permission_dialog_privacy_note),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.sms_permission_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}

@Composable
private fun SettingsGuideDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val dialogScrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_guide_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(dialogScrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.settings_guide_dialog_text))
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text(stringResource(R.string.settings_guide_dialog_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}

@Composable
private fun InfoDialog(title: String, text: String, iconColor: Color, onDismiss: () -> Unit) {
    val dialogScrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = iconColor)
                Text(title, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(dialogScrollState)) {
                Text(text)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close)) }
        }
    )
}

@Composable
private fun StopConfirmDialog(
    isConsentChecked: Boolean,
    onConsentChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val dialogScrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.stop_confirm_dialog_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(dialogScrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.stop_confirm_dialog_text))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onConsentChange(!isConsentChecked) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = isConsentChecked, onCheckedChange = onConsentChange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.stop_confirm_dialog_consent),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isConsentChecked,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text(stringResource(R.string.stop_confirm_dialog_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}