package com.kurodai0715.autoemergencycall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kurodai0715.autoemergencycall.domain.PowerSignalManager
import com.kurodai0715.autoemergencycall.ui.screen.AppBaseScreen
import com.kurodai0715.autoemergencycall.ui.theme.AutoEmergencyCallTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Hilt が自動でマネージャーを生成して注入してくれます
    @Inject
    lateinit var powerSignalManager: PowerSignalManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Activity のライフサイクルにマネージャーを登録（これだけで onStart / onStop が連動します）
        lifecycle.addObserver(powerSignalManager)

        enableEdgeToEdge()
        setContent {
            AutoEmergencyCallTheme {
                AppBaseScreen()
            }
        }
    }
}
