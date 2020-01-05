package com.bink.wallet

import android.app.Application
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.di.dataModule
import com.bink.wallet.di.networkModule
import com.bink.wallet.di.viewModelModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            modules(listOf(viewModelModules, networkModule, dataModule))
            SharedPreferenceManager.init(this@MainApplication)
        }
    }
}