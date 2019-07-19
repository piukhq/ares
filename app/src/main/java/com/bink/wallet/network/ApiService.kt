package com.bink.wallet.network

import com.bink.wallet.scenes.login.LoginBody
import com.bink.wallet.scenes.login.LoginResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @POST("/ubiquity/service")
    @FormUrlEncoded
    fun loginOrRegister(@Field("consent") loginResponse: LoginBody): Call<LoginResponse>
}