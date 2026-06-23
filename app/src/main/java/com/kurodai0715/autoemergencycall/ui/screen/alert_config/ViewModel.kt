package com.kurodai0715.autoemergencycall.ui.screen.alert_config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurodai0715.autoemergencycall.data.AlertConfig
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
class ConfigViewModel @Inject constructor(
    private val configStore: ConfigStore,
    private val contactStore: ContactStore // 連絡先一覧を選択肢として出すためにインジェクト
) : ViewModel() {

    private val _alertConfigs = MutableStateFlow<List<AlertConfig>>(emptyList())
    val alertConfigs: StateFlow<List<AlertConfig>> = _alertConfigs.asStateFlow()

    private val _availableContacts = MutableStateFlow<List<Contact>>(emptyList())
    val availableContacts: StateFlow<List<Contact>> = _availableContacts.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _alertConfigs.value = configStore.loadAlertConfigs()
            _availableContacts.value = contactStore.loadContacts()
        }
    }

    fun getConfigById(id: String?): AlertConfig? {
        if (id == null) return null
        return _alertConfigs.value.find { it.id == id }
    }

    /**
     * 動作設定の保存（新規・編集共通）
     */
    fun saveConfig(id: String?, thresholdHours: Int, targetContactIds: List<String>, onComplete: () -> Unit) {
        viewModelScope.launch {
            val currentList = _alertConfigs.value.toMutableList()

            if (id == null) {
                val newConfig = AlertConfig(thresholdHours = thresholdHours, targetContactIds = targetContactIds)
                currentList.add(newConfig)
            } else {
                val index = currentList.indexOfFirst { it.id == id }
                if (index != -1) {
                    currentList[index] = AlertConfig(id = id, thresholdHours = thresholdHours, targetContactIds = targetContactIds)
                }
            }

            configStore.saveAlertConfigs(currentList)
            loadData()
            onComplete()
        }
    }

    /**
     * 動作設定の削除
     */
    fun deleteConfig(id: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val updatedList = _alertConfigs.value.filter { it.id != id }
            configStore.saveAlertConfigs(updatedList)
            loadData()
            onComplete()
        }
    }
}