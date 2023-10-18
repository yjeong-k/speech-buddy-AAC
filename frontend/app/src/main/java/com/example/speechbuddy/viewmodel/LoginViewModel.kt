package com.example.speechbuddy.viewmodel

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechbuddy.R
import com.example.speechbuddy.data.remote.requests.AuthLoginRequest
import com.example.speechbuddy.repository.UserRepository
import com.example.speechbuddy.ui.models.LoginError
import com.example.speechbuddy.ui.models.LoginErrorType
import com.example.speechbuddy.ui.models.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject internal constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    var emailInput by mutableStateOf("")
        private set

    var passwordInput by mutableStateOf("")
        private set

    fun setEmail(input: String) {
        emailInput = input
    }

    fun setPassword(input: String) {
        passwordInput = input
    }

    private fun isValidEmail(): Boolean {
        return emailInput.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()
    }

    private fun isValidPassword(): Boolean {
        return passwordInput.length >= MINIMUM_PASSWORD_LENGTH
    }

    fun login() {
        if (!isValidEmail()) {
            _uiState.update { currentState ->
                currentState.copy(
                    isValidEmail = false,
                    error = LoginError(
                        type = LoginErrorType.EMAIL,
                        messageId = R.string.false_email
                    )
                )
            }
        } else if (!isValidPassword()) {
            _uiState.update { currentState ->
                currentState.copy(
                    isValidPassword = false,
                    error = LoginError(
                        type = LoginErrorType.PASSWORD,
                        messageId = R.string.false_password
                    )
                )
            }
        } else
            viewModelScope.launch {
                repository.login(
                    AuthLoginRequest(
                        email = emailInput,
                        password = passwordInput
                    )
                ).collect {
                    /*TODO*/
                }
            }
    }

    companion object {
        private const val MINIMUM_PASSWORD_LENGTH = 8
    }

}