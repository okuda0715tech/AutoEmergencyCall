/*
 * Copyright (c) 2025 Okuda Tomohiro
 * Licensed under the MIT License.
 */

package com.kurodai0715.directdebitmanager.ui.util

import android.util.Log

private const val TAG = "ClickUtil.kt"

private var lastClickedTime = 0.toLong()

/**
 * ダブルタップを抑止するタップイベントディスパッチャー.
 *
 * @param clickable タップされた際に実際に実施したい処理
 */
fun debouncedClick(clickable: () -> Unit) {

    // ダブルタップを抑止したい時間(単位:ミリ秒)
    val interval = 500

    // 現在時刻(単位:ミリ秒)
    val currentTime = System.currentTimeMillis()

    if (currentTime - lastClickedTime > interval) {
        Log.v(TAG, "click is valid.")
        lastClickedTime = currentTime
        clickable()
    } else {
        Log.v(TAG, "click is ignored.")
    }
}
