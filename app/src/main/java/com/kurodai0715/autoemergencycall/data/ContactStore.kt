package com.kurodai0715.autoemergencycall.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.contactDataStoreByJson: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "contact_json_prefs")

class ContactStore(private val context: Context) {

    companion object {
        private val KEY_CONTACT_LIST_JSON = stringPreferencesKey("contact_list_json")

        private val configuredJson = Json {
            // Contact クラス側で定義されたデフォルト値を、パース時（デシリアライズ時）に強制適用する
            coerceInputValues = true

            // 将来的にプロパティを「削除」したり、Json側に未知のキーがあっても無視してクラッシュさせない
            ignoreUnknownKeys = true
        }
    }

    /**
     * 登録されているすべての連絡先リスト（List<Contact>）を取得する
     */
    suspend fun loadContacts(): List<Contact> {
        val preferences = context.contactDataStoreByJson.data.first()
        val jsonString = preferences[KEY_CONTACT_LIST_JSON]

        // 保存データがない（null）か空文字の場合は空のリストを返す
        if (jsonString.isNullOrEmpty()) return emptyList()

        return try {
            // Json文字列を List<Contact> オブジェクトに復元
            configuredJson.decodeFromString<List<Contact>>(jsonString)
        } catch (e: Exception) {
            // パース失敗時のフォールバック
            emptyList()
        }
    }

    /**
     * 連絡先リスト（List<Contact>）を丸ごと保存する
     */
    suspend fun saveContacts(contacts: List<Contact>) {
        // List<Contact> を Json文字列（例: "[{"name":"長男"...}]"）に変換
        val jsonString = configuredJson.encodeToString(contacts)

        context.contactDataStoreByJson.edit { preferences ->
            preferences[KEY_CONTACT_LIST_JSON] = jsonString
        }
    }

    /**
     * 特定の連絡先を1件追加する（利便性のためのラッパー関数）
     */
    suspend fun addContact(newContact: Contact) {
        val currentList = loadContacts().toMutableList()
        currentList.add(newContact)
        saveContacts(currentList)
    }
}