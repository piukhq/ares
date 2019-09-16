package com.bink.wallet.di

import com.bink.wallet.MainActivity
import com.bink.wallet.utils.WindowFullscreenHandler
import org.koin.dsl.module

val utilsModule = module {
    single { MainActivity() }
    factory<WindowFullscreenHandler> { get() }
}