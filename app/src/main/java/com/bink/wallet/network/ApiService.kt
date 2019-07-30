package com.bink.wallet.network

import com.bink.wallet.scenes.login.LoginResponse
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/ubiquity/service")
    fun checkRegisteredUser(): Call<LoginResponse>

    @POST("/ubiquity/service")
    fun loginOrRegister(@Body loginResponse: LoginResponse): Deferred<LoginResponse>
}