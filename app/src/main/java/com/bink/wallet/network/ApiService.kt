package com.bink.wallet.network

import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    @GET("/ubiquity/service")
    fun registerCustomer(): Deferred<Response<Any>>

}