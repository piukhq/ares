package com.bink.wallet.di

import android.app.Activity
import com.bink.wallet.utils.WindowFullscreenHandler
import org.koin.dsl.module

val utilsModule = module {

    single { (activity: Activity) -> WindowFullscreenHandler(activity)}
}