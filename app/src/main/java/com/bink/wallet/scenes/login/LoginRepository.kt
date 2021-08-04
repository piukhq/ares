package com.bink.wallet.scenes.login

import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.BinkDatabase
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.MagicLinkAccessToken
import com.bink.wallet.model.MagicLinkBody
import com.bink.wallet.model.MagicLinkToken
import com.bink.wallet.model.PostServiceRequest
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.model.request.Preference
import com.bink.wallet.model.request.SignUpRequest
import com.bink.wallet.model.request.forgot_password.ForgotPasswordRequest
import com.bink.wallet.model.response.SignUpResponse
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.settings.DebugMenuViewModel
import com.bink.wallet.utils.CONTENT_TYPE
import com.bink.wallet.utils.logDebug
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody

class LoginRepository(
    private val apiService: ApiService,
    private val binkDatabase: BinkDatabase
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
        requestBody: String,
        preferenceResponse: MutableLiveData<ResponseBody>,
        preferenceErrorResponse: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.putPreferencesAsync(
                RequestBody.create(
                    MediaType.parse(CONTENT_TYPE),
                    requestBody
                )
            )
            try {
                val response = request.await()
                preferenceResponse.value = response
            } catch (e: Exception) {
                preferenceErrorResponse.postValue(e)
            }
        }
    }

    suspend fun sendMagicLink(magicLinkBody: MagicLinkBody) {
        apiService.postMagicLink(magicLinkBody)
    }

    suspend fun sendMagicLinkToken(magicLinkToken: MagicLinkToken): MagicLinkAccessToken{
        return apiService.postMagicLinkToken(magicLinkToken)
    }

    fun clearData(
        clearResponse: MutableLiveData<Unit>,
        clearDataError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                try {
                    val response = binkDatabase.clearAllTables()
                    clearResponse.postValue(response)
                } catch (e: Exception) {
                    clearDataError.postValue(e)
                    logDebug(DebugMenuViewModel::class.simpleName, e.toString())
                }
            }
        }
    }
}