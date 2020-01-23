package com.bink.wallet

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.di.dataModule
import com.bink.wallet.di.networkModule
import com.bink.wallet.di.utilsModule
import com.bink.wallet.di.viewModelModules
import com.livefront.bridge.Bridge
import com.livefront.bridge.SavedStateHandler
import com.tinsuke.icekick.extension.freezeInstanceState
import com.tinsuke.icekick.extension.unfreezeInstanceState
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val startKoin = startKoin {
            androidContext(this@MainApplication)
            modules(listOf(viewModelModules, networkModule, dataModule, utilsModule))
            SharedPreferenceManager.init(this@MainApplication)
        }

        Bridge.initialize(applicationContext, object : SavedStateHandler {
            override fun restoreInstanceState(target: Any, state: Bundle?) {
                when (target) {
                    is Activity -> target.unfreezeInstanceState(state)
                    is Fragment -> target.unfreezeInstanceState(state)
                }
            }

            override fun saveInstanceState(target: Any, state: Bundle) {
                when (target) {
                    is Activity -> target.freezeInstanceState(state)
                    is Fragment -> target.freezeInstanceState(state)
                }
            }
        })    }
}