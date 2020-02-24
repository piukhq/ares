package com.bink.wallet.scenes.login

import android.util.Patterns
import androidx.lifecycle.*
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.LoginData
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.model.response.SignUpResponse
import com.bink.wallet.scenes.login.LoginRepository.Companion.DEFAULT_LOGIN_ID
import com.bink.wallet.utils.*
import kotlinx.coroutines.launch

class LoginViewModel constructor(var loginRepository: LoginRepository) : BaseViewModel() {

    val loginBody = MutableLiveData<LoginBody>()
    val loginData = MutableLiveData<LoginData>()
    val logInResponse = MutableLiveData<SignUpResponse>()
    private val _logInErrorResponse = MutableLiveData<Throwable>()
    val logInErrorResponse: LiveData<Throwable>
        get() = _logInErrorResponse
    private val _authErrorResponse = MutableLiveData<Throwable>()
    val authErrorResponse: LiveData<Throwable>
        get() = _authErrorResponse
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val isLoading = MutableLiveData<Boolean>()

    private val passwordValidator = Transformations.map(password) {
        UtilFunctions.isValidField(PASSWORD_REGEX, it)
    }
    private val emailValidator = Transformations.map(email) {
        Patterns.EMAIL_ADDRESS.matcher(it).matches()
    }
    val isLoginEnabled = MediatorLiveData<Boolean>()

    init {
        isLoginEnabled.combineNonNull(
            emailValidator,
            passwordValidator,
            ::validateFields
        )
    }

    private fun validateFields(
        emailValidator: Boolean,
        passwordValidator: Boolean
    ): Boolean = emailValidator && passwordValidator


    fun authenticate() {
        loginRepository.doAuthenticationWork(
            LoginResponse(
                LoginBody(
                    System.currentTimeMillis() / 1000,
                    loginData.value?.email ?: EMPTY_STRING,
                    0.0,
                    0.0
                )
            ), loginBody, _authErrorResponse
        )
    }

    fun logIn(loginRequest: SignUpRequest) {
        loginRepository.logIn(loginRequest, logInResponse, _logInErrorResponse)
    }

    fun retrieveStoredLoginData() = viewModelScope.launch {
        loginData.value = LoginData(
            DEFAULT_LOGIN_ID,
            LocalStoreUtils.getAppSharedPref(
                LocalStoreUtils.KEY_EMAIL
            ) ?: EMPTY_STRING
        )
    }
}