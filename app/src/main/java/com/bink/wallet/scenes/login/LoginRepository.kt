package com.bink.wallet.scenes.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.LoginDataDao
import com.bink.wallet.model.LoginData
import com.bink.wallet.model.auth.FacebookAuthRequest
import com.bink.wallet.model.auth.FacebookAuthResponse
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

//    var loginEmail: String = "Bink20iteration1@testbink.com"

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

//    private fun updateLiveData(liveData: MutableLiveData<LoginData>, loginData: LoginData) {
//        CoroutineScope(Dispatchers.IO).launch {
//            withContext(Dispatchers.Main) {
//                liveData.value = loginData
//            }
//        }
//    }
//
//    suspend fun retrieveStoredLoginData(loginData: MutableLiveData<LoginData>) {
//        CoroutineScope(Dispatchers.IO).launch {
//            withContext(Dispatchers.Default) {
//                try {
//                    val response = loginDataDao.getLoginData()
//                    // Note: the AS hint says that response should never be null,
//                    // but it appears it can be during runtime... go figure!
//                    if (response != null && response.email != null) {
//                        loginEmail = response.email
//                        updateLiveData(loginData, response)
//                    } else {
//                        updateLiveData(loginData, LoginData(DEFAULT_LOGIN_ID, loginEmail))
//                        storeLoginData(loginEmail, loginData)
//                    }
//                } catch (e: Throwable) {
//                    Log.e(LoginRepository::class.simpleName, e.localizedMessage, e)
//                }
//            }
//        }
//    }
//
//    fun storeLoginData(email: String, loginData: MutableLiveData<LoginData>) {
//        CoroutineScope(Dispatchers.IO).launch {
//            withContext(Dispatchers.Default) {
//                try {
//                    runBlocking {
//                        val pos = loginDataDao.store(LoginData(DEFAULT_LOGIN_ID, email))
//                        if (pos >= 0) {
//                            updateLiveData(loginData, LoginData(DEFAULT_LOGIN_ID, email))
//                            loginEmail = email
//                        }
//                    }
//                } catch (e: Throwable) {
//                    Log.e(LoginDataDao::class.simpleName, e.toString(), e)
//                }
//            }
//        }
//    }

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
        marketingPrefResponse: MutableLiveData<Throwable>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.checkMarketingPrefAsync(checkedOption)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    marketingResponse.value = response
                } catch (e: Throwable) {
                    marketingPrefResponse.value = e
                }
            }
        }
    }
}