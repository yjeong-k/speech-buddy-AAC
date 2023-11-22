package com.example.speechbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechbuddy.domain.SessionManager
import com.example.speechbuddy.repository.AuthRepository
import com.example.speechbuddy.repository.UserRepository
import com.example.speechbuddy.ui.models.AccountSettingsAlert
import com.example.speechbuddy.ui.models.AccountSettingsUiState
import com.example.speechbuddy.utils.ResponseCode
import com.example.speechbuddy.utils.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSettingsViewModel @Inject internal constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountSettingsUiState())
    val uiState: StateFlow<AccountSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getMyInfo().collect { resource ->
                if (resource.status == Status.SUCCESS) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            user = resource.data!!
                        )
                    }
                }
            }
        }
    }

    fun showAlert(alert: AccountSettingsAlert) {
        _uiState.update { currentState ->
            currentState.copy(
                alert = alert
            )
        }
    }

    fun hideAlert() {
        _uiState.update { currentState ->
            currentState.copy(
                alert = null
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout().collect { result ->
                when (result.code()) {
                    ResponseCode.SUCCESS.value -> {
                        /* TODO: 디바이스에 저장돼 있는 유저 정보 초기화(토큰 말고) */
                        sessionManager.logout()
                    }

                    ResponseCode.NO_INTERNET_CONNECTION.value -> {
                        showAlert(AccountSettingsAlert.CONNECTION)
                    }
                }
            }
        }
    }

    fun withdraw() {
        viewModelScope.launch {
            authRepository.withdraw().collect { result ->
                when (result.code()) {
                    ResponseCode.SUCCESS.value -> {
                        /* TODO: 디바이스에 저장돼 있는 유저 정보 초기화(토큰 말고) */
                        sessionManager.logout()
                    }

                    ResponseCode.NO_INTERNET_CONNECTION.value -> {
                        showAlert(AccountSettingsAlert.CONNECTION)
                    }
                }
            }
        }
    }

    fun exitGuestMode() {
        viewModelScope.launch {
            sessionManager.exitGuestMode()
        }
    }

}