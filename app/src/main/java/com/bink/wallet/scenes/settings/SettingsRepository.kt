package com.bink.wallet.scenes.settings

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.data.LoginDataDao
import com.bink.wallet.model.LoginData
import com.bink.wallet.scenes.login.LoginRepository
import kotlinx.coroutines.*

class SettingsRepository(private val loginDataDao: LoginDataDao) {
    var loginEmail: String = "mwoodhams@testbink.com"//"Bink20iteration1@testbink.com"

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