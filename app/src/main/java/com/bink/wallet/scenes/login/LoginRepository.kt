package com.bink.wallet.scenes.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.bink.wallet.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.await

class LoginRepository(private val apiService: ApiService) {

    private var mutableLiveData = MutableLiveData<LoginBody>()

    fun doAuthenticationWork(): MutableLiveData<LoginBody> {
        CoroutineScope(Dispatchers.IO).launch {
            //TODO Change email when login api is provided
            val request = apiService.loginOrRegister(LoginResponse(LoginBody(System.currentTimeMillis() / 1000, "Bink20iteration1@testbink.com", 0.0, 12.345)))
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