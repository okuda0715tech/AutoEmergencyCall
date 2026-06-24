package com.kurodai0715.autoemergencycall.ui.screen.app_info

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

fun getAppVersion(context: Context): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            ).versionName ?: "Unknown"
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"
        }
    } catch (e: PackageManager.NameNotFoundException) {
        "Unknown"
    }
}