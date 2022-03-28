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
import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.settings.DebugMenuViewModel
import com.bink.wallet.utils.CONTENT_TYPE
import com.bink.wallet.utils.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val requestResult = withContext(Dispatchers.IO) {
                    apiService.loginOrRegisterAsync(loginResponse)
                }
                SharedPreferenceManager.isUserLoggedIn = true
                loginData.value = requestResult.consent
            } catch (e: Exception) {
                authErrorResponse.value = e
            }

        }
    }

    fun signUp(
        signUpRequest: SignUpRequest,
        signUpResponse: MutableLiveData<SignUpResponse>,
        signUpErrorResponse: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val requestResult = withContext(Dispatchers.IO) {
                    apiService.signUpAsync(signUpRequest)
                }
                SharedPreferenceManager.isUserLoggedIn = true
                signUpResponse.value = requestResult
            } catch (e: java.lang.Exception) {
                signUpErrorResponse.value = e
            }
        }
    }

    fun logIn(
        logInRequest: SignUpRequest,
        signUpResponse: MutableLiveData<SignUpResponse>,
        signUpErrorResponse: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val requestResult =
                    withContext(Dispatchers.IO) { apiService.logInAsync(logInRequest) }
                SharedPreferenceManager.isUserLoggedIn = true
                signUpResponse.value = requestResult
            } catch (e: Exception) {
                signUpErrorResponse.value = e
            }
        }
    }

    suspend fun postService(postServiceRequest: PostServiceRequest): ResponseBody {
        return apiService.postServiceAsync(postServiceRequest)
    }

    suspend fun putMarketingPref(marketingOption: MarketingOption): ResponseBody {
        return apiService.checkMarketingPrefAsync(marketingOption)
    }

    fun checkMarketingPref(
        checkedOption: MarketingOption,
        marketingResponse: MutableLiveData<ResponseBody>,
        marketingError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val requestResult = withContext(Dispatchers.IO) {
                    apiService.checkMarketingPrefAsync(checkedOption)
                }
                marketingResponse.value = requestResult
            } catch (e: Exception) {
                marketingError.value = e
            }
        }
    }

    fun forgotPassword(
        email: String,
        forgotPasswordResponse: MutableLiveData<ResponseBody>,
        forgotPasswordError: MutableLiveData<Exception>
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val requestResult = withContext(Dispatchers.IO) {
                    apiService.forgotPasswordAsync(ForgotPasswordRequest(email))
                }
                forgotPasswordResponse.value = requestResult
            } catch (e: Exception) {
                forgotPasswordError.value = e
            }
        }
    }

    suspend fun logOut(): ResponseBody {
        return apiService.logOutAsync()
    }

    suspend fun getPreferences(): List<Preference> {
        return apiService.getPreferencesAsync()
    }

    suspend fun setPreference(requestBody: String) {
        apiService.putPreferencesAsync(requestBody.toRequestBody(CONTENT_TYPE.toMediaTypeOrNull()))
    }

    suspend fun sendMagicLink(magicLinkBody: MagicLinkBody) {
        apiService.postMagicLink(magicLinkBody)
    }

    suspend fun sendMagicLinkToken(magicLinkToken: MagicLinkToken): MagicLinkAccessToken {
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