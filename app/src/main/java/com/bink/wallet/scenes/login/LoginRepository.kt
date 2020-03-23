package com.bink.wallet.scenes.login

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.LoginDataDao
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.PostServiceRequest
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
        authErrorResponse: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.loginOrRegisterAsync(loginResponse)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    SharedPreferenceManager.isUserLoggedIn = true
                    loginData.value = response.consent
                } catch (e: Exception) {
                    authErrorResponse.value = e
                }
            }
        }
    }

    fun authWithFacebook(
        facebookAuthRequest: FacebookAuthRequest,
        authResult: MutableLiveData<FacebookAuthResponse>,
        authError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.authWithFacebookAsync(facebookAuthRequest)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    SharedPreferenceManager.isUserLoggedIn = true
                    authResult.value = response
                } catch (e: Exception) {
                    authError.value = e
                }
            }
        }
    }

    fun signUp(
        signUpRequest: SignUpRequest,
        signUpResponse: MutableLiveData<SignUpResponse>,
        signUpErrorResponse: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.signUpAsync(signUpRequest)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    SharedPreferenceManager.isUserLoggedIn = true
                    signUpResponse.value = response
                } catch (e: java.lang.Exception) {
                    signUpErrorResponse.value = e
                }
            }
        }
    }

    fun logIn(
        logInRequest: SignUpRequest,
        signUpResponse: MutableLiveData<SignUpResponse>,
        signUpErrorResponse: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.logInAsync(logInRequest)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    SharedPreferenceManager.isUserLoggedIn = true
                    signUpResponse.value = response
                } catch (e: Exception) {
                    signUpErrorResponse.value = e
                }
            }
        }
    }

    fun postService(
        postServiceRequest: PostServiceRequest,
        postServiceResponse: MutableLiveData<ResponseBody>,
        postServiceErrorResponse: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.postServiceAsync(postServiceRequest)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    postServiceResponse.value = response
                } catch (e: Exception) {
                    postServiceErrorResponse.value = e
                }
            }
        }
    }

    fun checkMarketingPref(
        checkedOption: MarketingOption,
        marketingResponse: MutableLiveData<ResponseBody>,
        marketingError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.checkMarketingPrefAsync(checkedOption)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    marketingResponse.value = response
                } catch (e: Exception) {
                    marketingError.value = e
                }
            }
        }
    }

    fun forgotPassword(
        email: String,
        forgotPasswordResponse: MutableLiveData<ResponseBody>,
        forgotPasswordError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.forgotPasswordAsync(ForgotPasswordRequest(email))
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    forgotPasswordResponse.value = response
                } catch (e: Exception) {
                    forgotPasswordError.value = e
                }
            }
        }
    }

    fun logOut(
        logOutResponse: MutableLiveData<ResponseBody>,
        logOutErrorResponse: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.logOutAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    logOutResponse.value = response
                } catch (e: Exception) {
                    logOutErrorResponse.value = e
                }
            }
        }
    }

    fun getPreferences(
        preferenceResponse: MutableLiveData<List<Preference>>,
        preferenceErrorResponse: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.getPreferencesAsync()
            withContext(Dispatchers.Main) {
                try {
                    preferenceResponse.value = request.await()
                } catch (e: java.lang.Exception) {
                    preferenceErrorResponse.value = e
                }
            }
        }
    }

    fun setPreference(
        json: String,
        preferenceResponse: MutableLiveData<ResponseBody>,
        preferenceErrorResponse: MutableLiveData<Exception>
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
            } catch (e: Exception) {
                preferenceErrorResponse.value = e
            }
        }
    }
}