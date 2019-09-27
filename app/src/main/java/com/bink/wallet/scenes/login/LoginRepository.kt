package com.bink.wallet.scenes.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.LoginDataDao
import com.bink.wallet.model.LoginData
import com.bink.wallet.network.ApiService
import kotlinx.coroutines.*

class LoginRepository(private val apiService: ApiService,
                      private val loginDataDao: LoginDataDao
) {
    var loginEmail: String = "Bink20iteration1@testbink.com"

    fun doAuthenticationWork(loginResponse: LoginResponse, loginData: MutableLiveData<LoginBody>) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.loginOrRegisterAsync(loginResponse)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    loginData.value = response.consent
                } catch (e: Throwable) {
                    Log.e(LoginRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    private fun updateLiveData(liveData: MutableLiveData<LoginData>, loginData: LoginData) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                liveData.value = loginData
            }
        }
    }

    suspend fun retrieveStoredLoginData(loginData: MutableLiveData<LoginData>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Default) {
                try {
                    val response = loginDataDao.getLoginData()
                    // Note: the AS hint says that response should never be null,
                    // but it appears it can be during runtime... go figure!
                    if (response?.email != null) {
                        loginEmail = response.email
                        updateLiveData(loginData, response)
                    } else {
                        updateLiveData(loginData, LoginData("0", loginEmail))
                        storeLoginData(loginEmail, loginData)
                    }
                } catch (e: Throwable) {
                    Log.e(LoginRepository::class.simpleName, e.localizedMessage, e)
                }
            }
        }
    }

    fun storeLoginData(email: String, loginData: MutableLiveData<LoginData>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Default) {
                try {
                    runBlocking {
                        var pos = loginDataDao.store(LoginData("0", email))
                        if (pos >= 0) {
                            updateLiveData(loginData, LoginData("0", email))
                            loginEmail = email
                        }
                    }
                } catch (e: Throwable) {
                    Log.e(LoginDataDao::class.simpleName, e.toString())
                }
            }
        }
    }
}