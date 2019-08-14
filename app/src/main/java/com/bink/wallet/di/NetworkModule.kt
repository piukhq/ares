package com.bink.wallet.di

import android.content.Context
import android.util.Base64
import com.bink.wallet.BuildConfig
import com.bink.wallet.network.ApiService
import com.bink.wallet.utils.LocalStoreUtils
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

val networkModule = module {

    single { provideDefaultOkhttpClient(androidContext()) }
    single { provideRetrofit(get()) }
    single { provideApiService(get()) }
}

fun provideDefaultOkhttpClient(context: Context): OkHttpClient {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY

    val headerAuthorizationInterceptor = Interceptor { chain ->

        val header = JSONObject()
        header.put("alg", "HS512")
        header.put("typ", "JWT")

        val payload = JSONObject()
        payload.put("organisation_id", "Loyalty Angels")
        payload.put("bundle_id", "com.bink.bink20dev")
        payload.put("user_id", "Bink20iteration1@testbink.com")
        payload.put("property_id", "not currently used for authentication")
        payload.put("iat", System.currentTimeMillis() / 1000)

        val token = "${Base64.encodeToString(
            header.toString().toByteArray(), Base64.URL_SAFE
        )}.${Base64.encodeToString(payload.toString().toByteArray(), Base64.URL_SAFE)}".replace("=", "")
            .replace("\n", "")

        val hmac = Mac.getInstance("HmacSHA512")

        val secretKey = SecretKeySpec(LocalStoreUtils.getAppSecret(context)?.toByteArray(), "HmacSHA512")

        hmac.init(secretKey)

        val signature =
            Base64.encodeToString(hmac.doFinal(token.toByteArray()), Base64.URL_SAFE).replace("=", "").replace("\n", "")

        val finalToken = "Bearer $token.$signature".replace("=", "").replace("\n", "")
        val request = chain.request().url().newBuilder().build()
        val newRequest = chain.request().newBuilder().header("Authorization", finalToken).url(request).build()
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
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .client(client)
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()
}

fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)


