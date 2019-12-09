package com.bink.wallet.scenes

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.BaseViewModel
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.model.response.SignUpResponse
import com.bink.wallet.scenes.login.LoginRepository
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