package com.bink.wallet.scenes.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginRepository(private val apiService: ApiService) {

    private var mutableLiveData = MutableLiveData<LoginBody>()

    fun doAuthenticationWork(loginResponse: LoginResponse): MutableLiveData<LoginBody> {
        CoroutineScope(Dispatchers.IO).launch {
            val request = apiService.loginOrRegisterAsync(loginResponse)
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    mutableLiveData.value = response.consent
                } catch (e: Throwable) {
                    Log.e(LoginRepository::class.simpleName, e.toString())
                }
            }
        }
        return mutableLiveData
    }
}