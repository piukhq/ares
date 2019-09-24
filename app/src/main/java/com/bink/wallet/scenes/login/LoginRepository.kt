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
    var loginEmail: String = "mwoodhams@testbink.com"//"Bink20iteration1@testbink.com"

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

    fun retrieveStoredLoginData(localLoginData: MutableLiveData<LoginData>) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    val response = loginDataDao.getLoginData()
                    if (localLoginData.value != null) {
                        localLoginData.value = response
                        loginEmail = response.email!!
                    } else {
                        localLoginData.value = LoginData("0", loginEmail)
                    }
                } catch (e: Throwable) {
                    Log.e(LoginRepository::class.simpleName, e.toString())
                }
            }
        }
    }

    fun storeLoginData(email: String) {
        storeLoginData(LoginData("0", email))
    }
    fun storeLoginData(loginData: LoginData) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                try {
                    runBlocking {
                        loginDataDao.deleteEmails()
                        loginDataDao.store(loginData)

                        loginEmail = loginData.email!!
                    }
                } catch (e: Throwable) {
                    Log.e(LoginDataDao::class.simpleName, e.toString())
                }
            }
        }
    }
}