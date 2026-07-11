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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kurodai0715.autoemergencycall.R

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
                    Text(text = stringResource(R.string.sms_permission_dialog_title), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    Text(text = stringResource(R.string.sms_permission_dialog_privacy_note), style = MaterialTheme.typography.bodySmall)
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
                    Text(stringResource(R.string.sms_permission_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showProminentDisclosureDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // 永久拒否時のエスコートダイアログ
    if (showSettingsGuideDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsGuideDialog = false },
            title = { Text(stringResource(R.string.settings_guide_dialog_title)) },
            text = { Text(stringResource(R.string.settings_guide_dialog_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        showSettingsGuideDialog = false
                        val intent = viewModel.createApplicationDetailsIntent(context)
                        context.startActivity(intent)
                    }
                ) { Text(stringResource(R.string.settings_guide_dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsGuideDialog = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    // 「最終生存確認」の定義解説ダイアログ
    if (showActiveTimeInfo) {
        AlertDialog(
            onDismissRequest = { showActiveTimeInfo = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.active_time_info_title), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(stringResource(R.string.active_time_info_text))
            },
            confirmButton = {
                TextButton(onClick = { showActiveTimeInfo = false }) { Text(stringResource(R.string.common_close)) }
            }
        )
    }

    // 「最終生存チェック」の定義解説ダイアログ
    if (showCheckTimeInfo) {
        AlertDialog(
            onDismissRequest = { showCheckTimeInfo = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Text(stringResource(R.string.check_time_info_title), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(stringResource(R.string.check_time_info_text))
            },
            confirmButton = {
                TextButton(onClick = { showCheckTimeInfo = false }) { Text(stringResource(R.string.common_close)) }
            }
        )
    }

    if (showStopConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showStopConfirmDialog = false },
            title = { Text(stringResource(R.string.stop_confirm_dialog_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.stop_confirm_dialog_text))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isConsentChecked = !isConsentChecked }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isConsentChecked, onCheckedChange = { isConsentChecked = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.stop_confirm_dialog_consent), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleMonitoringStatus(context, false) // 💡 停止を実行
                        showStopConfirmDialog = false
                    },
                    enabled = isConsentChecked, // 💡 チェックがないと押せない
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.stop_confirm_dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirmDialog = false }) { Text(stringResource(R.string.common_cancel)) }
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
        Text(text = stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Text(
            text = stringResource(R.string.home_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        // 現在の稼働ステータス表示
        Text(text = stringResource(R.string.home_section_status), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

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
                val statusText = if (isMonitoringEnabled) stringResource(R.string.home_status_active) else stringResource(R.string.home_status_paused)
                val statusColor = if (isMonitoringEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                val guideText = if (isMonitoringEnabled) stringResource(R.string.home_guide_to_pause) else stringResource(R.string.home_guide_to_resume)

                Row(
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
                        // 💡 変更：縦のパディングを 4.dp から 6.dp に少し広げて押しやすく＆見やすく
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
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
                        text = guideText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 💡 修正：余計なSpacerを削除し、仕切り線の余白を調整
                HorizontalDivider(modifier = Modifier.padding(top = 2.dp, bottom = 6.dp))

                // 1. 最終活動検知の行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showActiveTimeInfo = true }
                    ) {
                        Text(
                            text = stringResource(R.string.home_label_active_time),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.home_content_description_info),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(text = lastActiveTimeText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                // 2. 見守りチェック実施の行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showCheckTimeInfo = true }
                    ) {
                        Text(
                            text = stringResource(R.string.home_label_check_time),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.home_content_description_info),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
                    Text(text = if (isSmsPermissionGranted) stringResource(R.string.home_sms_permission_granted) else stringResource(R.string.home_sms_permission_denied), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = if (isSmsPermissionGranted) stringResource(R.string.home_sms_permission_granted_desc) else stringResource(R.string.home_sms_permission_denied_desc), style = MaterialTheme.typography.bodySmall)
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
                        Text(text = stringResource(R.string.home_auto_revoke_warning_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text(text = stringResource(R.string.home_auto_revoke_warning_desc), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = stringResource(R.string.home_section_management), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onNavigateToContacts, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.home_btn_contacts)) }
            Button(onClick = onNavigateToConfigs, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.home_btn_configs)) }
        }

        OutlinedButton(
            onClick = onNavigateToTest,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.home_btn_test))
        }
    }
}