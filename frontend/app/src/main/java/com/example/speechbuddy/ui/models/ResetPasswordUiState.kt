package com.example.speechbuddy.ui.models

data class ResetPasswordUiState(
    val isValidPassword: Boolean = false,
    val error: ResetPasswordError? = null
)

data class ResetPasswordError(
    val type: ResetPasswordErrorType,
    val messageId: Int
)

enum class ResetPasswordErrorType {
    PASSWORD,
    PASSWORD_CHECK
}