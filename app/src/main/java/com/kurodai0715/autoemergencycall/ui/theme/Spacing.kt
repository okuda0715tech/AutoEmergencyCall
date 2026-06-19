package com.kurodai0715.directdebitmanager.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Spacing {

    // 同一要素内部（icon-text, label-value）
    val xs: Dp
        @Composable get() = when (LocalDensityMode.current) {
            DensityMode.Dense -> 4.dp
            DensityMode.Normal -> 8.dp
            DensityMode.Spacious -> 12.dp
        }

    // 同一グループ内（ListItem 内、Form 項目内）
    val s: Dp
        @Composable get() = when (LocalDensityMode.current) {
            DensityMode.Dense -> 8.dp
            DensityMode.Normal -> 12.dp
            DensityMode.Spacious -> 16.dp
        }

    // グループ間（Card と Card、Form セクション）
    val m: Dp
        @Composable get() = when (LocalDensityMode.current) {
            DensityMode.Dense -> 12.dp
            DensityMode.Normal -> 16.dp
            DensityMode.Spacious -> 24.dp
        }

    // レイアウト構造（Header / Body、Section 区切り）
    val l: Dp
        @Composable get() = when (LocalDensityMode.current) {
            DensityMode.Dense -> 16.dp
            DensityMode.Normal -> 20.dp
            DensityMode.Spacious -> 28.dp
        }

    // 画面構造（Pane 間、画面端マージン）
    val xl: Dp
        @Composable get() = when (LocalDensityMode.current) {
            DensityMode.Dense -> 24.dp
            DensityMode.Normal -> 28.dp
            DensityMode.Spacious -> 40.dp
        }
}
