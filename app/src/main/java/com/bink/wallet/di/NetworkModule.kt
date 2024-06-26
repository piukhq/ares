package com.bink.wallet.di

import android.content.Context
import android.content.Intent
import com.bink.wallet.BuildConfig
import com.bink.wallet.MainActivity
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.di.qualifier.network.NetworkQualifiers
import com.bink.wallet.model.NetworkActivity
import com.bink.wallet.model.store
import com.bink.wallet.network.ApiService
import com.bink.wallet.network.ApiSpreedly
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.BackendVersion
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
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
        val request = chain.request().url.newBuilder().build()

        val newRequest = chain.request().newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", SharedPreferenceManager.storedBackendVersion ?: BackendVersion.VERSION_3.version)
            .header("Authorization", jwtToken ?: EMPTY_STRING)
            .header("User-Agent", "Bink / Android ${BuildConfig.VERSION_CODE} / ${android.os.Build.VERSION.SDK_INT}")
        //We don't want to add this token in any production request
        if (BuildConfig.BUILD_TYPE != RELEASE_BUILD_TYPE) {
            newRequest.header("Bink-Test-Auth", Keys.binkTestAuthToken()).url(request)
        } else {
            newRequest.url(request)
        }

        val response = chain.proceed(newRequest.build())

        if (BuildConfig.BUILD_TYPE != RELEASE_BUILD_TYPE) {
            val sentAt = response.sentRequestAtMillis
            val receivedAt = response.receivedResponseAtMillis

            var responseBody = ""
            try {
                val copiedBody = response.peekBody(1000000L)
                responseBody = copiedBody.string()
            } catch (e: IllegalStateException) {
            }

            val networkActivity = NetworkActivity(
                baseUrl = SharedPreferenceManager.storedApiUrl.toString(),
                httpStatusCode = response.code.toString(),
                requestBody = chain.request().body.bodyToString(),
                responseBody = responseBody,
                endpoint = chain.request().url.toString(),
                responseTime = "${(receivedAt - sentAt)}ms"
            )

            networkActivity.store()
        }

        response.networkResponse?.request?.url?.let {
            if (it.toString() == ADD_PAYMENT_CARD_URL) {
                if (response.code == 200 || response.code == 201) {
                    SharedPreferenceManager.addPaymentCardSuccessHttpCode = response.code
                }
            }
            if (it.toString() == ADD_LOYALTY_CARD_URL) {
                if (response.code == 200 || response.code == 201) {
                    SharedPreferenceManager.addLoyaltyCardSuccessHttpCode = response.code
                }
            }
        }


        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            SharedPreferenceManager.isUserLoggedIn = false
            LocalStoreUtils.clearPreferences()
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

private fun RequestBody?.bodyToString(): String {
    if (this == null) return ""
    val buffer = okio.Buffer()
    writeTo(buffer)
    return buffer.readUtf8()
}

fun provideSpreedlyOkHttpClient(): OkHttpClient {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY

    val headerInterceptor = Interceptor { chain ->
        val request = chain.request().url.newBuilder().build()
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

    return retrofitBuilder.build()
}

fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

fun provideSpreedlyApiService(retrofit: Retrofit): ApiSpreedly =
    retrofit.create(ApiSpreedly::class.java)

var BASE_URL = SharedPreferenceManager.storedApiUrl.toString()
var ADD_PAYMENT_CARD_URL = "$BASE_URL/ubiquity/payment_cards"
var ADD_LOYALTY_CARD_URL = "$BASE_URL/ubiquity/membership_cards"
