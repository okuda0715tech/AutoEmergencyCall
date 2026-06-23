package com.kurodai0715.autoemergencycall.ui.screen.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurodai0715.autoemergencycall.data.ConfigStore
import com.kurodai0715.autoemergencycall.data.Contact
import com.kurodai0715.autoemergencycall.data.ContactStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val contactStore: ContactStore,
    private val configStore: ConfigStore,
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            _contacts.value = contactStore.loadContacts()
        }
    }

    /**
     * IDを指定して特定の連絡先を1件取得する（編集画面の初期値用）
     */
    fun getContactById(id: String?): Contact? {
        if (id == null) return null
        return _contacts.value.find { it.id == id }
    }

    /**
     * 保存（新規追加 または 既存編集）
     */
    fun saveContact(
        id: String?,
        name: String,
        phoneNumber: String,
        relation: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val currentList = _contacts.value.toMutableList()

            if (id == null) {
                // 新規追加
                val newContact =
                    Contact(name = name, phoneNumber = phoneNumber, relation = relation)
                currentList.add(newContact)
            } else {
                // 既存編集（IDが一致する要素を置き換える）
                val index = currentList.indexOfFirst { it.id == id }
                if (index != -1) {
                    currentList[index] = Contact(
                        id = id,
                        name = name,
                        phoneNumber = phoneNumber,
                        relation = relation
                    )
                }
            }

            contactStore.saveContacts(currentList)
            loadContacts() // リスト更新
            onComplete()   // 完了通知（ダイアログ表示用）
        }
    }

    /**
     * 削除
     */
    fun deleteContact(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            deleteContact(id)

            // 【連動処理】アラート動作設定からもそのIDを消し去る
            deleteAlertConfig(id)

            loadContacts() // リスト更新
            onComplete()   // 完了通知（ダイアログ表示用）
        }
    }

    private suspend fun deleteAlertConfig(contactId: String) {
        val currentConfigs = configStore.loadAlertConfigs()
        val updatedConfigs = currentConfigs.map { config ->
            config.copy(
                // 除外したいID以外のリストを構築
                targetContactIds = config.targetContactIds.filter { it != contactId }
            )
        }
        configStore.saveAlertConfigs(updatedConfigs)
    }

    private suspend fun deleteContact(id: String) {
        // 除外したいID以外のリストを構築
        val updatedList = _contacts.value.filter { it.id != id }
        contactStore.saveContacts(updatedList)
    }
}