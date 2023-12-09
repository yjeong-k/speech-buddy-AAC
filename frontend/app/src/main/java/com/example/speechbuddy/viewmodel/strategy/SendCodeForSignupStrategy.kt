package com.example.speechbuddy.viewmodel.strategy

import androidx.lifecycle.viewModelScope
import com.example.speechbuddy.R
import com.example.speechbuddy.data.remote.requests.AuthSendCodeRequest
import com.example.speechbuddy.repository.AuthRepository
import com.example.speechbuddy.utils.ResponseCode
import com.example.speechbuddy.utils.ResponseHandler
import com.example.speechbuddy.viewmodel.EmailVerificationViewModel
import kotlinx.coroutines.launch

class SendCodeForSignupStrategy: SendCodeStrategy {
    override fun sendCode(viewModel: EmailVerificationViewModel, repository: AuthRepository) {
        viewModel.changeLoadingState()
        viewModel.viewModelScope.launch {
            repository.sendCodeForSignup(
                AuthSendCodeRequest(
                    email = viewModel.emailInput
                )
            ).collect { result ->
                viewModel.changeLoadingState()
                when (result.code()) {
                    ResponseCode.SUCCESS.value -> {
                        viewModel.handleSendCodeSuccess()
                    }

                    ResponseCode.BAD_REQUEST.value -> {
                        val responseHandler = ResponseHandler()
                        val errorMessageId =
                            when (responseHandler.parseErrorResponse(result.errorBody()!!).key) {
                                "email" -> R.string.wrong_email
                                "already_taken" -> R.string.email_already_taken
                                else -> R.string.unknown_error
                            }
                        viewModel.handleSendCodeBadRequest(errorMessageId)
                    }

                    ResponseCode.NO_INTERNET_CONNECTION.value -> {
                        viewModel.handleSendCodeNoInternetConnection()
                    }

                    else -> {
                        viewModel.handleSendCodeUnknownError()
                    }
                }
            }
        }
    }
}