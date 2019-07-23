package com.bink.wallet.scenes.login

import com.bink.wallet.network.ApiService
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginWorker(apiService: ApiService) {

    var apiService: ApiService = apiService

    fun doAuthenticationWork(request: LoginBody) {
        apiService.loginOrRegister(request).enqueue(object : Callback<LoginResponse> {
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                TODO("not implemented")
            }

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful()) {
                    val responseBody = response.body()!!.toString()
                    val moshi: Moshi = Moshi.Builder().build()
                    val adapter: JsonAdapter<LoginResponse> = moshi.adapter(LoginResponse::class.java)
                    val loginResponse = adapter.fromJson(responseBody)
                    TODO("Send response to interactor ")
                }
            }

        })
        val response = "{\"consent\":{\"email\":\"launchpad@bink.com\",\"timestamp\":1517549941}}"
    }
}