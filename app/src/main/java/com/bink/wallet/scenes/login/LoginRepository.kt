package com.bink.wallet.scenes.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.LoginDataDao
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.model.response.SignUpResponse
import com.bink.wallet.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class LoginRepository(
    private val apiService: ApiService,
    private val loginDataDao: LoginDataDao
) {
    companion object {
        const val DEFAULT_LOGIN_ID = "0"
    }

    fun doAuthenticationWork(loginResponse: LoginResponse, loginData: MutableLiveData<LoginBody>) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.loginOrRegisterAsync(loginResponse)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    loginData.value = response.consent
                } catch (e: Throwable) {
                    Log.e(LoginRepository::class.simpleName, e.toString(), e)
                }
            }
        }
    }

    fun signUp(
        signUpRequest: SignUpRequest,
        signUpResponse: MutableLiveData<SignUpResponse>,
        signUpErrorResponse: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.signUpAsync(signUpRequest)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    signUpResponse.value = response
                } catch (e: Throwable) {
                    signUpErrorResponse.value = e
                }
            }
        }
    }

    fun logIn(
        logInRequest: SignUpRequest,
        signUpResponse: MutableLiveData<SignUpResponse>,
        signUpErrorResponse: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.logInAsync(logInRequest)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    signUpResponse.value = response
                } catch (e: Throwable) {
                    signUpErrorResponse.value = e
                }
            }
        }
    }

    fun checkMarketingPref(
        checkedOption: MarketingOption,
        marketingResponse: MutableLiveData<ResponseBody>,
        marketingError: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.checkMarketingPrefAsync(checkedOption)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    marketingResponse.value = response
                } catch (e: Throwable) {
                    marketingError.value = e
                }
            }
        }
    }
}