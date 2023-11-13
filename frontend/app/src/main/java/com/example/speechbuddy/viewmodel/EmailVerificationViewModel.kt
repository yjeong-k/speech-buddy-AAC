package com.example.speechbuddy.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechbuddy.R
import com.example.speechbuddy.data.remote.requests.AuthSendCodeRequest
import com.example.speechbuddy.data.remote.requests.AuthVerifyEmailRequest
import com.example.speechbuddy.domain.SessionManager
import com.example.speechbuddy.domain.models.AuthToken
import com.example.speechbuddy.repository.AuthRepository
import com.example.speechbuddy.ui.models.EmailVerificationError
import com.example.speechbuddy.ui.models.EmailVerificationErrorType
import com.example.speechbuddy.ui.models.EmailVerificationUiState
import com.example.speechbuddy.utils.ResponseCode
import com.example.speechbuddy.utils.ResponseHandler
import com.example.speechbuddy.utils.Status
import com.example.speechbuddy.utils.isValidCode
import com.example.speechbuddy.utils.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailVerificationViewModel @Inject internal constructor(
    private val repository: AuthRepository,
    private val responseHandler: ResponseHandler,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmailVerificationUiState())
    val uiState: StateFlow<EmailVerificationUiState> = _uiState.asStateFlow()

    private val source = mutableStateOf<String?>(null)

    private val loading = mutableStateOf(false)

    var emailInput by mutableStateOf("")
        private set

    var codeInput by mutableStateOf("")
        private set

    fun setSource(value: String?) {
        source.value = value
    }

    fun setEmail(input: String) {
        emailInput = input
        if (_uiState.value.error?.type == EmailVerificationErrorType.EMAIL) validateEmail()
    }

    fun setCode(input: String) {
        codeInput = input
        if (_uiState.value.error?.type == EmailVerificationErrorType.CODE) validateCode()
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

    private fun validateCode() {
        if (isValidCode(codeInput)) {
            _uiState.update { currentState ->
                currentState.copy(
                    isValidCode = true,
                    error = null
                )
            }
        }
    }

    fun sendCode() {
        if (emailInput.isEmpty()) {
            _uiState.update { currentState ->
                currentState.copy(
                    isValidEmail = false,
                    error = EmailVerificationError(
                        type = EmailVerificationErrorType.EMAIL,
                        messageId = R.string.no_email
                    )
                )
            }
        } else if (!isValidEmail(emailInput)) {
            _uiState.update { currentState ->
                currentState.copy(
                    isValidEmail = false,
                    error = EmailVerificationError(
                        type = EmailVerificationErrorType.EMAIL,
                        messageId = R.string.wrong_email
                    )
                )
            }
        } else {
            if (source.value == "signup") sendCodeForSignup()
            if (source.value == "reset_password") sendCodeForResetPassword()
            else _uiState.update { currentState ->
                currentState.copy(
                    isValidEmail = false,
                    error = EmailVerificationError(
                        type = EmailVerificationErrorType.UNKNOWN,
                        messageId = R.string.unknown_error
                    )
                )
            }
        }
    }

    private fun sendCodeForSignup() {
        loading.value = true
        viewModelScope.launch {
            repository.sendCodeForSignup(
                AuthSendCodeRequest(
                    email = emailInput
                )
            ).collect { result ->
                loading.value = false
                when (result.code()) {
                    ResponseCode.SUCCESS.value -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isCodeSuccessfullySent = true,
                                error = null
                            )
                        }
                    }

                    ResponseCode.BAD_REQUEST.value -> {
                        val errorMessageId =
                            when (responseHandler.parseErrorResponse(result.errorBody()!!).key) {
                                "email" -> R.string.wrong_email
                                "already_taken" -> R.string.email_already_taken
                                else -> R.string.unknown_error
                            }
                        _uiState.update { currentState ->
                            currentState.copy(
                                isValidEmail = false,
                                error = EmailVerificationError(
                                    type = EmailVerificationErrorType.EMAIL,
                                    messageId = errorMessageId
                                )
                            )
                        }
                    }

                    ResponseCode.NO_INTERNET_CONNECTION.value -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isValidEmail = false,
                                error = EmailVerificationError(
                                    type = EmailVerificationErrorType.CONNECTION,
                                    messageId = R.string.connection_error
                                )
                            )
                        }
                    }

                    else -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isValidEmail = false,
                                error = EmailVerificationError(
                                    type = EmailVerificationErrorType.UNKNOWN,
                                    messageId = R.string.unknown_error
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun sendCodeForResetPassword() {
        loading.value = true
        viewModelScope.launch {
            repository.sendCodeForResetPassword(
                AuthSendCodeRequest(
                    email = emailInput
                )
            ).collect { result ->
                loading.value = false
                when (result.code()) {
                    ResponseCode.SUCCESS.value -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isCodeSuccessfullySent = true,
                                error = null
                            )
                        }
                    }

                    ResponseCode.BAD_REQUEST.value -> {
                        val errorMessageId =
                            when (responseHandler.parseErrorResponse(result.errorBody()!!).key) {
                                "email" -> R.string.wrong_email
                                "no_user" -> R.string.unregistered_email
                                else -> R.string.unknown_error
                            }
                        _uiState.update { currentState ->
                            currentState.copy(
                                isValidEmail = false,
                                error = EmailVerificationError(
                                    type = EmailVerificationErrorType.EMAIL,
                                    messageId = errorMessageId
                                )
                            )
                        }
                    }

                    ResponseCode.NO_INTERNET_CONNECTION.value -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isValidEmail = false,
                                error = EmailVerificationError(
                                    type = EmailVerificationErrorType.CONNECTION,
                                    messageId = R.string.connection_error
                                )
                            )
                        }
                    }

                    else -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isValidEmail = false,
                                error = EmailVerificationError(
                                    type = EmailVerificationErrorType.UNKNOWN,
                                    messageId = R.string.unknown_error
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun verifyEmail(navigateCallback: (String) -> Unit) {
        if (codeInput.isEmpty()) {
            _uiState.update { currentState ->
                currentState.copy(
                    isValidCode = false,
                    error = EmailVerificationError(
                        type = EmailVerificationErrorType.CODE,
                        messageId = R.string.no_code
                    )
                )
            }
        } else if (!isValidCode(codeInput)) {
            _uiState.update { currentState ->
                currentState.copy(
                    isValidCode = false,
                    error = EmailVerificationError(
                        type = EmailVerificationErrorType.CODE,
                        messageId = R.string.wrong_code
                    )
                )
            }
        } else {
            if (source.value == "signup") verifyEmailForSignup(navigateCallback)
            if (source.value == "reset_password") verifyEmailForResetPassword(navigateCallback)
            else _uiState.update { currentState ->
                currentState.copy(
                    isValidCode = false,
                    error = EmailVerificationError(
                        type = EmailVerificationErrorType.UNKNOWN,
                        messageId = R.string.unknown_error
                    )
                )
            }
        }
    }

    private fun verifyEmailForSignup(navigateCallback: (String) -> Unit) {
        loading.value = true
        viewModelScope.launch {
            repository.verifyEmailForSignup(
                AuthVerifyEmailRequest(
                    email = emailInput,
                    code = codeInput
                )
            ).collect { result ->
                loading.value = false
                when (result.code()) {
                    ResponseCode.SUCCESS.value -> {
                        navigateCallback("signup/$emailInput")
                    }

                    ResponseCode.BAD_REQUEST.value -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isValidCode = false,
                                error = EmailVerificationError(
                                    type = EmailVerificationErrorType.CODE,
                                    messageId = R.string.wrong_code
                                )
                            )
                        }
                    }

                    ResponseCode.NO_INTERNET_CONNECTION.value -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isValidCode = false,
                                error = EmailVerificationError(
                                    type = EmailVerificationErrorType.CONNECTION,
                                    messageId = R.string.connection_error
                                )
                            )
                        }
                    }

                    else -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isValidCode = false,
                                error = EmailVerificationError(
                                    type = EmailVerificationErrorType.UNKNOWN,
                                    messageId = R.string.unknown_error
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun verifyEmailForResetPassword(navigateCallback: (String) -> Unit) {
        loading.value = true
        viewModelScope.launch {
            repository.verifyEmailForResetPassword(
                AuthVerifyEmailRequest(
                    email = emailInput,
                    code = codeInput
                )
            ).collect { resource ->
                loading.value = false
                if (resource.status == Status.SUCCESS) {
                    val temporaryToken = AuthToken(resource.data?.accessToken, null)
                    sessionManager.setAuthToken(temporaryToken)
                    navigateCallback("reset_password")
                } else if (resource.message?.contains("unknown", ignoreCase = true) == true) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isValidCode = false,
                            error = EmailVerificationError(
                                type = EmailVerificationErrorType.CONNECTION,
                                messageId = R.string.connection_error
                            )
                        )
                    }
                } else {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isValidCode = false,
                            error = EmailVerificationError(
                                type = EmailVerificationErrorType.CODE,
                                messageId = R.string.wrong_code
                            )
                        )
                    }
                }
            }
        }
    }
}