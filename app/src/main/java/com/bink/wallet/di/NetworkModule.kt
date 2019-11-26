package com.bink.wallet.di

import com.bink.wallet.network.ApiConstants.Companion.BASE_URL
import com.bink.wallet.network.ApiService
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.LocalStoreUtils
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { provideDefaultOkHttpClient() }
    single { provideRetrofit(get()) }
    single { provideApiService(get()) }
}

fun provideDefaultOkHttpClient(): OkHttpClient {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY

    val headerAuthorizationInterceptor = Interceptor { chain ->
        val jwtToken = LocalStoreUtils.getAppSharedPref(
            LocalStoreUtils.KEY_TOKEN
        )?.let {
            it
        }

        val request = chain.request().url().newBuilder().build()
        val newRequest = chain.request().newBuilder()
            .header("Content-Type", "application/json;v=1.1")
            .header("Authorization", jwtToken ?: EMPTY_STRING).url(request)
            .build()
        chain.proceed(newRequest)
    }

    val logging = HttpLoggingInterceptor()
    // sets desired log level
    logging.level = HttpLoggingInterceptor.Level.BODY

    return OkHttpClient.Builder()
        .addNetworkInterceptor(interceptor)
        .addInterceptor(logging)
        .addInterceptor(headerAuthorizationInterceptor)
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
}

fun provideRetrofit(client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .client(client)
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()
}

fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)


