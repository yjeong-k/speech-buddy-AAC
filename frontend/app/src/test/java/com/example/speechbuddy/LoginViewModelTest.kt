package com.example.speechbuddy

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.speechbuddy.data.remote.requests.AuthLoginRequest
import com.example.speechbuddy.data.remote.requests.AuthSignupRequest
import com.example.speechbuddy.domain.models.AuthToken
import com.example.speechbuddy.repository.AuthRepository
import com.example.speechbuddy.ui.models.LoginErrorType
import com.example.speechbuddy.ui.models.SignupErrorType
import com.example.speechbuddy.utils.Resource
import com.example.speechbuddy.utils.Status
import com.example.speechbuddy.viewmodel.LoginViewModel
import com.example.speechbuddy.viewmodel.SignupViewModel
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

class LoginViewModelTest {
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @MockK
    private val repository: AuthRepository = mockk()
    private lateinit var viewModel: LoginViewModel

    // boundary condition: 8 characters in password field
    private val validPassword = "password"
    private val shortPassword = "pwd"
    private val wrongPassword = "wrong_password" // valid format
    private val validEmail = "valid@test.com"
    private val invalidEmail = "invalid"
    private val unregisteredEmail = "unregistered@test.com" // valid format
    private val emptyString = ""

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(mainThreadSurrogate)
        viewModel = LoginViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun `should set invalid email before login click when setemail is called with invalid email`() {
        viewModel.setEmail(invalidEmail)

        assertEquals(invalidEmail, viewModel.emailInput)
        assertEquals(null, viewModel.uiState.value.error?.type)
        assertEquals(false, viewModel.uiState.value.isValidEmail)
    }

    @Test
    fun `should set valid email before login click when setemail is called with valid email`() {
        viewModel.setEmail(validEmail)

        assertEquals(validEmail, viewModel.emailInput)
        assertEquals(null, viewModel.uiState.value.error?.type)
        assertEquals(false, viewModel.uiState.value.isValidEmail)
    }

    @Test
    fun `should set errortype email after login click when setemail is called with invalid email`() {
        viewModel.setEmail(invalidEmail)

        viewModel.login()

        assertEquals(invalidEmail, viewModel.emailInput)
        assertEquals(LoginErrorType.EMAIL, viewModel.uiState.value.error?.type)
        assertEquals(false, viewModel.uiState.value.isValidEmail)
    }

    @Test
    fun `should set errortype null after login click when invalid email is changed to valid email`() {
        viewModel.setEmail(invalidEmail)

        viewModel.login()

        viewModel.setEmail(validEmail)

        assertEquals(validEmail, viewModel.emailInput)
        assertEquals(null, viewModel.uiState.value.error?.type)
        assertEquals(true, viewModel.uiState.value.isValidEmail)
    }

    @Test
    fun `should set errortype email after login click when setemail is called with unregistered email`() {
        viewModel.setEmail(unregisteredEmail)
        viewModel.setPassword(validPassword)

        coEvery {
            repository.login(AuthLoginRequest(unregisteredEmail, validPassword))
        } returns flowOf(
            Resource.error(
            "{\"code\":400,\"message\":{\"wrong_email\":[\"wrong email address\"]}}",
            null)
        )

        viewModel.login()
        Thread.sleep(10) //viewModel.login() does not immediately produce result

        assertEquals(unregisteredEmail, viewModel.emailInput)
        assertEquals(LoginErrorType.EMAIL, viewModel.uiState.value.error?.type)
        assertEquals(R.string.false_email, viewModel.uiState.value.error?.messageId)
        assertEquals(false, viewModel.uiState.value.isValidEmail)
    }

    @Test
    fun `should set errortype null after login click when unregistered email is changed to valid email`() {
        viewModel.setEmail(unregisteredEmail)
        viewModel.setPassword(validPassword)

        coEvery {
            repository.login(AuthLoginRequest(unregisteredEmail, validPassword))
        } returns flowOf(
            Resource.error(
                "{\"code\":400,\"message\":{\"wrong_email\":[\"wrong email address\"]}}",
                null)
        )

        viewModel.login()
        Thread.sleep(10) //viewModel.login() does not immediately produce result

        viewModel.setEmail(validEmail)

        assertEquals(validEmail, viewModel.emailInput)
        assertEquals(null, viewModel.uiState.value.error?.type)
        assertEquals(null, viewModel.uiState.value.error?.messageId)
        assertEquals(true, viewModel.uiState.value.isValidEmail)
    }

    @Test
    fun `should set errortype not email after login click when setemail is called with valid email`() {
        viewModel.setEmail(validEmail)
        viewModel.setPassword(wrongPassword)

        coEvery {
            repository.login(AuthLoginRequest(validEmail, wrongPassword))
        } returns flowOf(
            Resource.error(
                "{\"code\":400,\"message\":{\"wrong_password\":[\"wrong password\"]}}",
                null)
        )

        viewModel.login()
        Thread.sleep(10) //viewModel.login() does not immediately produce result

        assertEquals(validEmail, viewModel.emailInput)
        assertEquals(LoginErrorType.PASSWORD, viewModel.uiState.value.error?.type)
        assertEquals(R.string.false_password, viewModel.uiState.value.error?.messageId)
        assertEquals(false, viewModel.uiState.value.isValidEmail)
    }

    @Test
    fun `should set errortype not email after login click when valid email is changed to invalid email`() {
        viewModel.setEmail(validEmail)
        viewModel.setPassword(wrongPassword)

        coEvery {
            repository.login(AuthLoginRequest(validEmail, wrongPassword))
        } returns flowOf(
            Resource.error(
                "{\"code\":400,\"message\":{\"wrong_password\":[\"wrong password\"]}}",
                null)
        )

        viewModel.login()
        Thread.sleep(10) //viewModel.login() does not immediately produce result

        viewModel.setEmail(invalidEmail)

        assertEquals(invalidEmail, viewModel.emailInput)
        assertEquals(LoginErrorType.PASSWORD, viewModel.uiState.value.error?.type)
        assertEquals(R.string.false_password, viewModel.uiState.value.error?.messageId)
        assertEquals(false, viewModel.uiState.value.isValidEmail)
    }

    @Test
    fun `should set short password before login click when setpassword is called with short email`() {
        viewModel.setPassword(shortPassword)

        assertEquals(shortPassword, viewModel.passwordInput)
        assertEquals(null, viewModel.uiState.value.error?.type)
        assertEquals(false, viewModel.uiState.value.isValidPassword)
    }

    @Test
    fun `should set valid password before login click when setpassword is called with valid email`(){
        viewModel.setPassword(validPassword)

        assertEquals(validPassword, viewModel.passwordInput)
        assertEquals(null, viewModel.uiState.value.error?.type)
        assertEquals(false, viewModel.uiState.value.isValidPassword)
    }

    @Test
    fun `should set errortype password after login click when setpassword is called with short password`() {
        viewModel.setEmail(validEmail)
        viewModel.setPassword(shortPassword)

        viewModel.login()

        assertEquals(shortPassword, viewModel.passwordInput)
        assertEquals(LoginErrorType.PASSWORD, viewModel.uiState.value.error?.type)
        assertEquals(false, viewModel.uiState.value.isValidPassword)
    }

    @Test
    fun `should set errortype null after login click when short password is changed to valid password`() {
        viewModel.setEmail(validEmail)
        viewModel.setPassword(shortPassword)

        viewModel.login()

        viewModel.setPassword(validPassword)

        assertEquals(validPassword, viewModel.passwordInput)
        assertEquals(null, viewModel.uiState.value.error?.type)
        assertEquals(true, viewModel.uiState.value.isValidPassword)
    }

    @Test
    fun `should set errortype password after login click when setpassword is called with wrong password`(){
        viewModel.setEmail(validEmail)
        viewModel.setPassword(wrongPassword)

        coEvery {
            repository.login(AuthLoginRequest(validEmail, wrongPassword))
        } returns flowOf(
            Resource.error(
                "{\"code\":400,\"message\":{\"wrong_password\":[\"wrong password\"]}}",
                null)
        )

        viewModel.login()
        Thread.sleep(10) //viewModel.login() does not immediately produce result

        assertEquals(wrongPassword, viewModel.passwordInput)
        assertEquals(LoginErrorType.PASSWORD, viewModel.uiState.value.error?.type)
        assertEquals(false, viewModel.uiState.value.isValidPassword)
    }

    @Test
    fun `should set errortype null after login click when wrong password is changed to valid password`(){
        viewModel.setEmail(validEmail)
        viewModel.setPassword(wrongPassword)

        coEvery {
            repository.login(AuthLoginRequest(validEmail, wrongPassword))
        } returns flowOf(
            Resource.error(
                "{\"code\":400,\"message\":{\"wrong_password\":[\"wrong password\"]}}",
                null)
        )

        viewModel.login()
        Thread.sleep(10) //viewModel.login() does not immediately produce result

        viewModel.setPassword(validPassword)

        assertEquals(validPassword, viewModel.passwordInput)
        assertEquals(null, viewModel.uiState.value.error?.type)
        assertEquals(true, viewModel.uiState.value.isValidPassword)
    }

    @Test
    fun `should set errortype email when login is called with invalid email, short password`(){
        viewModel.setEmail(invalidEmail)
        viewModel.setPassword(shortPassword)

        viewModel.login()

        assertEquals(false, viewModel.uiState.value.isValidEmail)
        assertEquals(false, viewModel.uiState.value.isValidPassword)
        assertEquals(LoginErrorType.EMAIL, viewModel.uiState.value.error?.type)
        assertEquals(R.string.false_email, viewModel.uiState.value.error?.messageId)
    }

    @Test
    fun `should set errortype email when login is called with invalid email, valid password`(){
        viewModel.setEmail(invalidEmail)
        viewModel.setPassword(validPassword)

        viewModel.login()

        assertEquals(false, viewModel.uiState.value.isValidEmail)
        assertEquals(false, viewModel.uiState.value.isValidPassword)
        assertEquals(LoginErrorType.EMAIL, viewModel.uiState.value.error?.type)
        assertEquals(R.string.false_email, viewModel.uiState.value.error?.messageId)
    }

    @Test
    fun `should set errortype email when login is called with unregistered email, short password`(){
        viewModel.setEmail(unregisteredEmail)
        viewModel.setPassword(shortPassword)

        viewModel.login()

        assertEquals(false, viewModel.uiState.value.isValidEmail)
        assertEquals(false, viewModel.uiState.value.isValidPassword)
        assertEquals(LoginErrorType.PASSWORD, viewModel.uiState.value.error?.type)
        assertEquals(R.string.false_password, viewModel.uiState.value.error?.messageId)
    }

    @Test
    fun `should set errortype email when login is called with unregistered email, valid password`(){
        viewModel.setEmail(unregisteredEmail)
        viewModel.setPassword(validPassword)

        coEvery {
            repository.login(AuthLoginRequest(unregisteredEmail, validPassword))
        } returns flowOf(
            Resource.error(
                "{\"code\":400,\"message\":{\"wrong_email\":[\"wrong email address\"]}}",
                null)
        )

        viewModel.login()
        Thread.sleep(10) //viewModel.login() does not immediately produce result

        assertEquals(false, viewModel.uiState.value.isValidEmail)
        assertEquals(false, viewModel.uiState.value.isValidPassword)
        assertEquals(LoginErrorType.EMAIL, viewModel.uiState.value.error?.type)
        assertEquals(R.string.false_email, viewModel.uiState.value.error?.messageId)

    }

    @Test
    fun `should set errortype password when login is called with valid email, short password`(){
        viewModel.setEmail(validEmail)
        viewModel.setPassword(shortPassword)

        viewModel.login()

        assertEquals(false, viewModel.uiState.value.isValidEmail)
        assertEquals(false, viewModel.uiState.value.isValidPassword)
        assertEquals(LoginErrorType.PASSWORD, viewModel.uiState.value.error?.type)
        assertEquals(R.string.false_password, viewModel.uiState.value.error?.messageId)

    }

    @Test
    fun `should set errortype password when login is called with valid email, wrong password`(){
        viewModel.setEmail(validEmail)
        viewModel.setPassword(wrongPassword)

        coEvery {
            repository.login(AuthLoginRequest(validEmail, wrongPassword))
        } returns flowOf(
            Resource.error(
                "{\"code\":400,\"message\":{\"wrong_password\":[\"wrong password\"]}}",
                null)
        )

        viewModel.login()
        Thread.sleep(10) //viewModel.login() does not immediately produce result

        assertEquals(false, viewModel.uiState.value.isValidEmail)
        assertEquals(false, viewModel.uiState.value.isValidPassword)
        assertEquals(LoginErrorType.PASSWORD, viewModel.uiState.value.error?.type)
        assertEquals(R.string.false_password, viewModel.uiState.value.error?.messageId)
    }

    @Test
    fun `should login success when login is called with valid email, valid password`(){
        viewModel.setEmail(validEmail)
        viewModel.setPassword(validPassword)

        val authToken = AuthToken("access", "refresh")
        coEvery {
            repository.login(AuthLoginRequest(validEmail, validPassword))
        } returns flowOf(
            Resource.success(authToken)
        )

        viewModel.login()
        Thread.sleep(10) //viewModel.login() does not immediately produce result

        assertEquals(null, viewModel.loginResult.value?.message)
        assertEquals(authToken, viewModel.loginResult.value?.data)
    }

}