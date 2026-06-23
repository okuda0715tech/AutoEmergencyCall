package com.kurodai0715.autoemergencycall.ui.screen.developer_only

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurodai0715.autoemergencycall.domain.SafetyCheckUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 開発者画面の状態を表すシールクラス
 */
sealed interface DeveloperUiState {
    object Idle : DeveloperUiState
    object Running : DeveloperUiState
    data class Success(val message: String) : DeveloperUiState
    data class Error(val exception: Throwable) : DeveloperUiState
}

@HiltViewModel
class DeveloperViewModel @Inject constructor(
    private val safetyCheckUseCase: SafetyCheckUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeveloperUiState>(DeveloperUiState.Idle)
    val uiState: StateFlow<DeveloperUiState> = _uiState.asStateFlow()

    /**
     * 1時間に1回動く安否確認ロジック(executeCheck)を今すぐ手動で実行する
     */
    fun runSafetyCheckImmediately() {
        // 二重実行を防止
        if (_uiState.value is DeveloperUiState.Running) return

        viewModelScope.launch {
            _uiState.value = DeveloperUiState.Running
            try {
                safetyCheckUseCase.executeCheck()
                _uiState.value = DeveloperUiState.Success("executeCheck() の実行に成功しました。")
            } catch (e: Exception) {
                _uiState.value = DeveloperUiState.Error(e)
            }
        }
    }

    /**
     * 状態を Idle にリセットする
     */
    fun resetUiState() {
        _uiState.value = DeveloperUiState.Idle
    }
}