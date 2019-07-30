package com.bink.wallet.di

import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.login.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModules = module{
    single { provideLoginRepository(get()) }
    viewModel { LoginViewModel(get()) }
}

fun provideLoginRepository(restApiService: ApiService) : LoginRepository = LoginRepository(restApiService)