package com.kurodai0715.autoemergencycall.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// アプリ全体で共有される、Compose対応のグローバル変数
object DebugManager {
    var isDebugging by mutableStateOf(false)
}