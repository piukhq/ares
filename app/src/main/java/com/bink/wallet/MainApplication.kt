package com.bink.wallet

import android.app.Application
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.di.dataModule
import com.bink.wallet.di.networkModule
import com.bink.wallet.di.utilsModule
import com.bink.wallet.di.viewModelModules
import com.bink.wallet.network.ApiConstants
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            SharedPreferenceManager.init(this@MainApplication)
            if (SharedPreferenceManager.storedApiUrl.isNullOrEmpty()) {
                SharedPreferenceManager.storedApiUrl = ApiConstants.BASE_URL
            } else {
                ApiConstants.BASE_URL = SharedPreferenceManager.storedApiUrl.toString()
            }
            modules(listOf(viewModelModules, networkModule, dataModule, utilsModule))
        }
    }
}