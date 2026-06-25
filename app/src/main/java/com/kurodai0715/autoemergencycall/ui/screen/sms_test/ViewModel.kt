package com.kurodai0715.autoemergencycall.ui.screen.sms_test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurodai0715.autoemergencycall.data.Contact
import com.kurodai0715.autoemergencycall.data.ContactStore
import com.kurodai0715.autoemergencycall.domain.SmsSender
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestSmsViewModel @Inject constructor(
    private val contactStore: ContactStore,
    private val smsSender: SmsSender
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
     * 選択された連絡先に即座にテストSMSを送信する
     */
    fun sendTestSms(contact: Contact, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                smsSender.sendSms(
                    contact.phoneNumber,
                    "【自動安否確認アプリ】【テスト】${contact.name}さんへの安否確認SMS：端末の活動が48時間検知できませんでした。",
                    showNotification = false,
                )
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}