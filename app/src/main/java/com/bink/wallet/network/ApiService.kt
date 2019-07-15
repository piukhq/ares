package com.bink.wallet.network

import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    //TODO: Add the path
    @GET("/")
    fun getPopularMovieAsync(@Query("page") page: Int): Deferred<Response<Any>>

}