package com.kurodai0715.autoemergencycall.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

fun openUnusedAppRestrictionsSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12 (API 31) 以上
        // 「アプリの詳細設定」を開く。ここに「未使用のアプリを一時停止する」のトグルスイッチがあります。
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Android 11 (API 30) 専用
        // 定数が見つからないエラーを回避するため、文字列でインテントを直接生成します
        val intent = Intent("android.settings.UNUSED_APP_RESTRICTIONS").apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // 万が一システム側で開けなかった場合のフォールバック（詳細画面へ）
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(fallbackIntent)
        }
    } else {
        // Android 10以下はそもそもこの機能（自動剥奪）がないので、何もしない、
        // または通常のアプリ詳細画面を開く
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}