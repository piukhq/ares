package com.bink.wallet.scenes.login

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.LoginDataDao
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.auth.FacebookAuthRequest
import com.bink.wallet.model.auth.FacebookAuthResponse
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.model.request.Preference
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.model.request.forgot_password.ForgotPasswordRequest
import com.bink.wallet.model.response.SignUpResponse
import com.bink.wallet.network.ApiService
import com.bink.wallet.utils.CONTENT_TYPE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody

class LoginRepository(
    private val apiService: ApiService,
    private val loginDataDao: LoginDataDao
) {
    companion object {
        const val DEFAULT_LOGIN_ID = "0"
    }

    fun doAuthenticationWork(
        loginResponse: LoginResponse,
        loginData: MutableLiveData<LoginBody>,
        authErrorResponse: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.loginOrRegisterAsync(loginResponse)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    SharedPreferenceManager.isUserLoggedIn = true
                    loginData.value = response.consent
                } catch (e: Throwable) {
                    authErrorResponse.value = e
                }
            }
        }
    }

    fun authWithFacebook(
        facebookAuthRequest: FacebookAuthRequest,
        authResult: MutableLiveData<FacebookAuthResponse>,
        authError: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.authWithFacebookAsync(facebookAuthRequest)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    SharedPreferenceManager.isUserLoggedIn = true
                    authResult.value = response
                } catch (e: Throwable) {
                    authError.value = e
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
                    SharedPreferenceManager.isUserLoggedIn = true
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
                    SharedPreferenceManager.isUserLoggedIn = true
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

    fun forgotPassword(
        email: String,
        forgotPasswordResponse: MutableLiveData<ResponseBody>,
        forgotPasswordError: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.forgotPasswordAsync(ForgotPasswordRequest(email))
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    forgotPasswordResponse.value = response
                } catch (e: Throwable) {
                    forgotPasswordError.value = e
                }
            }
        }
    }

    fun logOut(
        logOutResponse: MutableLiveData<ResponseBody>,
        logOutErrorResponse: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.logOutAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    logOutResponse.value = response
                } catch (e: Throwable) {
                    logOutErrorResponse.value = e
                }
            }
        }
    }

    fun getPreferences(
        preferenceResponse: MutableLiveData<List<Preference>>,
        preferenceErrorResponse: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getPreferencesAsync()
            withContext(Dispatchers.Main) {
                try {
                    preferenceResponse.value = request.await()
                } catch (e: Throwable) {
                    preferenceErrorResponse.value = e
                }
            }
        }
    }

    fun setPreference(
        json: String,
        preferenceResponse: MutableLiveData<ResponseBody>,
        preferenceErrorResponse: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.putPreferencesAsync(
                RequestBody.create(
                    MediaType.parse(CONTENT_TYPE),
                    json
                )
            )
            try {
                val response = request.await()
                preferenceResponse.value = response
            } catch (e: Throwable) {
                preferenceErrorResponse.value = e
            }
        }
    }
}