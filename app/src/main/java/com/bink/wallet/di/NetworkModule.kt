package com.bink.wallet.di

import android.content.Context
import android.content.Intent
import com.bink.wallet.MainActivity
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.di.qualifier.network.NetworkQualifiers
import com.bink.wallet.network.ApiService
import com.bink.wallet.network.ApiSpreedly
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.BackendVersion
import com.facebook.login.LoginManager
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

val networkModule = module {
    single(NetworkQualifiers.BinkOkHttp) { provideDefaultOkHttpClient(get()) }
    single(NetworkQualifiers.SpreedlyOkHttp) { provideSpreedlyOkHttpClient() }
    single(NetworkQualifiers.SpreedlyRetrofit) {
        provideRetrofit(
            get(NetworkQualifiers.SpreedlyOkHttp),
            SharedPreferenceManager.storedApiUrl.toString()
        )
    }
    single(NetworkQualifiers.BinkRetrofit) {
        provideRetrofit(
            get(NetworkQualifiers.BinkOkHttp),
            SharedPreferenceManager.storedApiUrl.toString()
        )
    }
    single(NetworkQualifiers.BinkApiInterface) { provideApiService(get(NetworkQualifiers.BinkRetrofit)) }
    single(NetworkQualifiers.SpreedlyApiInterface) { provideSpreedlyApiService(get(NetworkQualifiers.SpreedlyRetrofit)) }
}

fun provideDefaultOkHttpClient(appContext: Context): OkHttpClient {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY

    val headerAuthorizationInterceptor = Interceptor { chain ->
        val jwtToken =
            LocalStoreUtils.getAppSharedPref(
                LocalStoreUtils.KEY_TOKEN
            )?.replace("\n", EMPTY_STRING)?.trim()

        jwtToken?.let {
            logError("NetworkModule", jwtToken)
        }
        val request = chain.request().url().newBuilder().build()

        //todo encryption might need modifying to match this logic..
        val header = if (SharedPreferenceManager.storedBackendVersion != null) {
            SharedPreferenceManager.storedBackendVersion
        } else {
            BackendVersion.VERSION_1.version
        }
        val newRequest = chain.request().newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", header.toString())
            .header("Authorization", jwtToken ?: EMPTY_STRING)
            .url(request)
            .build()
        val response = chain.proceed(newRequest)
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            SharedPreferenceManager.isUserLoggedIn = false
            LoginManager.getInstance().logOut()
            LocalStoreUtils.clearPreferences(appContext)
            appContext.startActivity(
                Intent(appContext, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    .apply {
                        putSessionHandlerNavigationDestination(
                            SESSION_HANDLER_DESTINATION_ONBOARDING
                        )
                    }
            )
            return@Interceptor response
        }
        response
    }

    val logging = HttpLoggingInterceptor()
    // sets desired log level
    logging.level = HttpLoggingInterceptor.Level.BODY

    val builder = CertificatePinner.Builder()
    for (host in CertificatePins.values()) {
//        builder.add(host.domain, "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        builder.add(host.domain, host.hash)
    }
    val certificatePinner = builder.build()

    return OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .addNetworkInterceptor(interceptor)
        .addInterceptor(logging)
        .addInterceptor(headerAuthorizationInterceptor)
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
}

fun provideSpreedlyOkHttpClient(): OkHttpClient {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY

    val headerInterceptor = Interceptor { chain ->
        val request = chain.request().url().newBuilder().build()
        val newRequest = chain.request().newBuilder()
            .header("Content-Type", "application/json").url(request).build()

        chain.proceed(newRequest)
    }

    val logging = HttpLoggingInterceptor()
    // sets desired log level
    logging.level = HttpLoggingInterceptor.Level.BODY
    return OkHttpClient.Builder()
        .addNetworkInterceptor(interceptor)
        .addInterceptor(logging)
        .addInterceptor(headerInterceptor)
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
}

fun provideRetrofit(client: OkHttpClient, baseUrl: String): Retrofit {
    val retrofitBuilder = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(MoshiConverterFactory.create())
        .client(client)
        .addCallAdapterFactory(CoroutineCallAdapterFactory())

    return retrofitBuilder.build()
}

fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

fun provideSpreedlyApiService(retrofit: Retrofit): ApiSpreedly =
    retrofit.create(ApiSpreedly::class.java)


