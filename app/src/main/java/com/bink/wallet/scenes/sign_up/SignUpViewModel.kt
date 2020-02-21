package com.bink.wallet.scenes.sign_up

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.model.response.SignUpResponse
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.utils.PASSWORD_REGEX
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.combine
import okhttp3.ResponseBody

class SignUpViewModel(var loginRepository: LoginRepository) : BaseViewModel() {
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val confirmPassword = MutableLiveData<String>()
    val termsCondition = MutableLiveData<Boolean>()
    val privacyPolicy = MutableLiveData<Boolean>()
    val marketingMessages = MutableLiveData<Boolean>()
    val isLoading = MutableLiveData<Boolean>()

    val signUpResponse = MutableLiveData<SignUpResponse>()
    val signUpErrorResponse = MutableLiveData<Throwable>()
    val marketingPrefResponse = MutableLiveData<ResponseBody>()
    val marketingPrefErrorResponse = MutableLiveData<Throwable>()

    private val passwordValidator = Transformations.map(password) {
        UtilFunctions.isValidField(PASSWORD_REGEX, it)
    }
    private val emailValidator = Transformations.map(email) {
        Patterns.EMAIL_ADDRESS.matcher(it).matches()
    }

    private val passwordMatcher = MediatorLiveData<Boolean>()
    val isSignUpEnabled = MediatorLiveData<Boolean>()

    init {
        passwordMatcher.combine(
            password,
            confirmPassword,
            ::arePasswordsMatching
        )

        isSignUpEnabled.combine(
            emailValidator,
            passwordValidator,
            passwordMatcher,
            termsCondition,
            privacyPolicy,
            ::isSignUpButtonEnabled
        )
    }

    private fun isSignUpButtonEnabled(
        emailValidator: Boolean,
        passwordValidator: Boolean,
        passwordMatcher: Boolean,
        termsAndConditions: Boolean,
        privacyPolicy: Boolean
    ): Boolean = passwordValidator &&
            emailValidator &&
            passwordMatcher &&
            termsAndConditions &&
            privacyPolicy

    private fun arePasswordsMatching(
        password: String,
        confirmedPassword: String
    ): Boolean =
        password == confirmedPassword


    fun signUp(signUpRequest: SignUpRequest) {
        loginRepository.signUp(
            signUpRequest,
            signUpResponse,
            signUpErrorResponse
        )
    }

    fun marketingPref(marketingOption: MarketingOption) {
        loginRepository.checkMarketingPref(
            marketingOption,
            marketingPrefResponse,
            marketingPrefErrorResponse
        )
    }
}
