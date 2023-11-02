package com.example.speechbuddy.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.speechbuddy.MainApplication.Companion.token_prefs
import com.example.speechbuddy.R
import com.example.speechbuddy.data.remote.requests.AuthVerifyEmailAcceptRequest
import com.example.speechbuddy.data.remote.requests.AuthVerifyEmailSendRequest
import com.example.speechbuddy.domain.models.AuthToken
import com.example.speechbuddy.repository.AuthRepository
import com.example.speechbuddy.ui.models.EmailVerificationError
import com.example.speechbuddy.ui.models.EmailVerificationErrorType
import com.example.speechbuddy.ui.models.EmailVerificationUiState
import com.example.speechbuddy.utils.Status
import com.example.speechbuddy.utils.isValidEmail
import com.example.speechbuddy.utils.isValidVerifyCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailVerificationViewModel @Inject internal constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

    var emailInput by mutableStateOf("")
        private set

    var verifyCodeInput by mutableStateOf("")
        private set

    fun setEmail(input: String) {
        emailInput = input
        if (_uiState.value.error?.type == EmailVerificationErrorType.EMAIL) validateEmail()
    }

    fun setVerifyCode(input: String) {
        verifyCodeInput = input
        if (_uiState.value.error?.type == EmailVerificationErrorType.VERIFY_CODE) validateVerifyCode()
    }

    private fun clearVerifyCodeInput() {
        verifyCodeInput = ""
    }

    private fun validateEmail() {
        if (isValidEmail(emailInput)) {
            _uiState.update { currentState ->
                currentState.copy(
                    isValidEmail = true,
                    error = null
                )
            }
        }
    }

    private fun validateVerifyCode() {
        if (isValidVerifyCode(verifyCodeInput)) {
            _uiState.update { currentState ->
                currentState.copy(
                    isValidVerifyCode = true,
                    error = null
                )
            }
        }
    }

    fun verifySend(source: String?) {
        // What function(ultimately, API call) to use
        val verifySendFunction = if (source == "signup") {
            repository::verifySendSignup
        } else { // source == "reset_password"
            repository::verifySendPW
        }

        // Prior validation of email input
        if (!isValidEmail(emailInput)) {
            _uiState.update { currentState ->
                currentState.copy(
                    isValidEmail = false,
                    error = EmailVerificationError(
                        type = EmailVerificationErrorType.EMAIL,
                        messageId = R.string.false_email
                    )
                )
            }
        }

        viewModelScope.launch {
            verifySendFunction(
                AuthVerifyEmailSendRequest(
                    email = emailInput
                )
            ).collect { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isSuccessfulSend = true,
                                error = null
                            )
                        }
                    }

                    Status.ERROR -> {
                        val messageId = when (result.message) {
                            "email" -> R.string.false_email
                            "already_taken" -> R.string.email_already_taken
                            "no_user" -> R.string.no_such_user
                            else -> R.string.false_email
                        }
                        _uiState.update { currentState ->
                            currentState.copy(
                                isValidEmail = false,
                                error = EmailVerificationError(
                                    type = EmailVerificationErrorType.EMAIL,
                                    messageId = messageId
                                )
                            )
                        }
                    }

                    Status.LOADING -> {
                    }
                }
            }
        }
    }

    fun verifyAccept(source: String?, navController: NavHostController) {
        val verifyAcceptFunction = if (source == "signup") {
            repository::verifyAcceptSignup
        } else { // source == "reset_password"
            repository::verifyAcceptPW
        }

        if (!isValidVerifyCode(verifyCodeInput)) {
            _uiState.update { currentState ->
                currentState.copy(
                    isValidVerifyCode = false,
                    error = EmailVerificationError(
                        type = EmailVerificationErrorType.VERIFY_CODE,
                        messageId = R.string.false_validation_code
                    )
                )
            }
        }

        viewModelScope.launch {
            verifyAcceptFunction(
                AuthVerifyEmailAcceptRequest(
                    email = emailInput,
                    code = verifyCodeInput
                )
            ).collect { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        if (source == "signup") {
                            navController.navigate("signup/$emailInput")
                        } else {
                            val authToken = result.data as AuthToken
                            token_prefs.setAccessToken(authToken.accessToken!!)
                            navController.navigate("reset_password")
                        }
                    }

                    Status.ERROR -> {
                        // All error cases from this API call can
                        // boil down to 'false_validation_code'
                        _uiState.update { currentState ->
                            currentState.copy(
                                isValidVerifyCode = false,
                                error = EmailVerificationError(
                                    type = EmailVerificationErrorType.VERIFY_CODE,
                                    messageId = R.string.false_validation_code
                                )
                            )
                        }
                    }

                    Status.LOADING -> {
                    }
                }
            }
        }
        clearVerifyCodeInput()
    }
}

