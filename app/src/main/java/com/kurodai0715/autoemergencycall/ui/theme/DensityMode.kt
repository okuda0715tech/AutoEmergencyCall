package com.kurodai0715.directdebitmanager.ui.theme

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

enum class DensityMode {
    Dense, // タブレット / Desktop など、情報密度重視の場合
    Normal, // スマホの標準設定の場合
    Spacious // 視力の弱い方が文字サイズを上げた場合（アクセシビリティも同時に確保）
}

fun WindowWidthSizeClass.toDensityMode(): DensityMode =
    when (this) {
        // TODO 後で、文字の拡大倍率も考慮して決めたいため、いったん Normal に落とす。
//        WindowWidthSizeClass.Compact -> DensityMode.Spacious
//        WindowWidthSizeClass.Medium -> DensityMode.Normal
//        WindowWidthSizeClass.Expanded -> DensityMode.Dense
        else -> DensityMode.Normal
    }