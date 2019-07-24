package com.bink.wallet.scenes.login

import com.bink.wallet.network.ApiService
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginWorker(apiService: ApiService, loginInteractor: LoginInteractor) {

    var apiService: ApiService = apiService
    var loginInteractor: LoginInteractor = loginInteractor

    fun doAuthenticationWork(requestBody: LoginBody) {
        GlobalScope.launch(Dispatchers.Main) {
            val registerRequest = apiService.loginOrRegister(requestBody)
            try {
                val response = registerRequest.await()
                if (response.isSuccessful) {
                    val responseBody = response.body()!!.toString()
                    val moshi: Moshi = Moshi.Builder().build()
                    val adapter: JsonAdapter<LoginResponse> = moshi.adapter(LoginResponse::class.java)
                    val loginResponse = adapter.fromJson(responseBody)
                    loginInteractor.successfulResponse(loginResponse!!.consent)
                } else {
                    loginInteractor.showErrorMessage("Some relevant Error message")
                }
            } catch (exception: Exception) {
                loginInteractor.showErrorMessage(exception.message!!)
            }
        }
    }
}