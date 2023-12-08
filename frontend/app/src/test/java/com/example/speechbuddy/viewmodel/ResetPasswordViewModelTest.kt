package com.example.speechbuddy.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.speechbuddy.data.remote.requests.AuthResetPasswordRequest
import com.example.speechbuddy.domain.SessionManager
import com.example.speechbuddy.repository.AuthRepository
import com.example.speechbuddy.ui.models.ResetPasswordErrorType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class ResetPasswordViewModelTest {
    @OptIn(DelicateCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @MockK
    private val repository: AuthRepository = mockk()
    private val sessionManager: SessionManager = mockk()
    private lateinit var viewModel: ResetPasswordViewModel

    private val validPassword = "123456789" //length >= 8
    private val invalidPassword = "1234567" //length < 8
    private val emptyPassword = ""
    private val passwordList = arrayListOf(validPassword, invalidPassword, emptyPassword)
    private val invalidPasswordList = arrayListOf(invalidPassword, emptyPassword)

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(mainThreadSurrogate)
        viewModel = ResetPasswordViewModel(repository, sessionManager)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun `should return no error when password is empty before click`() {
        passwordList.forEach {
            viewModel.setPassword(emptyPassword)
            viewModel.setPasswordCheck(it)
            assertEquals(viewModel.passwordInput, emptyPassword)
            assertEquals(viewModel.passwordCheckInput, it)
            assertEquals(viewModel.uiState.value.error?.type, null)
            assertEquals(viewModel.uiState.value.isValidPassword, false)
        }
    }

    @Test
    fun `should return no error when password is valid before click`() {
        passwordList.forEach {
            viewModel.setPassword(validPassword)
            viewModel.setPasswordCheck(it)
            assertEquals(viewModel.passwordInput, validPassword)
            assertEquals(viewModel.passwordCheckInput, it)
            assertEquals(viewModel.uiState.value.error?.type, null)
            assertEquals(viewModel.uiState.value.isValidPassword, false)
        }
    }

    @Test
    fun `should return no error when password is invalid before click`() {
        passwordList.forEach {
            viewModel.setPassword(invalidPassword)
            viewModel.setPasswordCheck(it)
            assertEquals(viewModel.passwordInput, invalidPassword)
            assertEquals(viewModel.passwordCheckInput, it)
            assertEquals(viewModel.uiState.value.error?.type, null)
            assertEquals(viewModel.uiState.value.isValidPassword, false)
        }
    }

    @Test
    fun `should set error type PASSWORD when password is empty`() {
        val onSuccess: () -> Unit = mockk(relaxed = true)
        passwordList.forEach {
            viewModel.setPassword(emptyPassword)
            viewModel.setPasswordCheck(it)
            viewModel.resetPassword(onSuccess)

            assertEquals(viewModel.passwordInput, emptyPassword)
            assertEquals(viewModel.passwordCheckInput, it)
            assertEquals(viewModel.uiState.value.error?.type, ResetPasswordErrorType.PASSWORD)
            assertEquals(viewModel.uiState.value.isValidPassword, false)

            viewModel.setPassword(validPassword)
            assertEquals(viewModel.uiState.value.error?.type, null)
            assertEquals(viewModel.uiState.value.isValidPassword, true)
        }
    } //covers both cases when password equals password check, or not

    @Test
    fun `should set error type PASSWORD when password is invalid`() {
        val onSuccess: () -> Unit = mockk(relaxed = true)
        passwordList.forEach {
            viewModel.setPassword(invalidPassword)
            viewModel.setPasswordCheck(it)
            viewModel.resetPassword(onSuccess)

            assertEquals(viewModel.passwordInput, invalidPassword)
            assertEquals(viewModel.passwordCheckInput, it)
            assertEquals(viewModel.uiState.value.error?.type, ResetPasswordErrorType.PASSWORD)
            assertEquals(viewModel.uiState.value.isValidPassword, false)

            viewModel.setPassword(validPassword)
            assertEquals(viewModel.uiState.value.error?.type, null)
            assertEquals(viewModel.uiState.value.isValidPassword, true)
            //checks whether validatePassword works well when changing the password into a valid one
        }
    } //covers both cases when password equals password check, or not

    @Test
    fun `should set error type PASSWORD_CHECK when password is valid`() {
        val onSuccess: () -> Unit = mockk(relaxed = true)
        invalidPasswordList.forEach {
            viewModel.setPassword(validPassword)
            viewModel.setPasswordCheck(it)
            viewModel.resetPassword(onSuccess)

            assertEquals(viewModel.passwordInput, validPassword)
            assertEquals(viewModel.passwordCheckInput, it)
            assertEquals(viewModel.uiState.value.error?.type, ResetPasswordErrorType.PASSWORD_CHECK)
            assertEquals(viewModel.uiState.value.isValidPassword, false)
            //isValidPassword doesn't turn true when password was valid from the start
        }
    }

    @Test
    fun `should success when password is valid and equals password check`() {
        val onSuccess: () -> Unit = mockk(relaxed = true)
        viewModel.setPassword(validPassword)
        viewModel.setPasswordCheck(validPassword)
        coEvery {
            repository.resetPassword(AuthResetPasswordRequest(validPassword))
        } returns flowOf(Response.success(null))
        viewModel.resetPassword(onSuccess)
        Thread.sleep(10)

        assertEquals(null, viewModel.uiState.value.error?.type)
        coVerify { sessionManager.deleteToken() }
    }
}