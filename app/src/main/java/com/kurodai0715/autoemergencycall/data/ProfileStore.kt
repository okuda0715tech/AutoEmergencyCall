package com.kurodai0715.autoemergencycall.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class ProfileStore(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_NAME = "user_name"
    }

    // ユーザー名の保存
    fun saveUserName(name: String) {
        sharedPreferences.edit { putString(KEY_USER_NAME, name.trim()) }
    }

    // ユーザー名の取得（未設定の場合は空文字）
    fun getUserName(): String {
        return sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
    }
}
