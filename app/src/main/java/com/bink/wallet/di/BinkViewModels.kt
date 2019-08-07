package com.bink.wallet.di

import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.login.LoginViewModel
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyViewModel
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModules = module {
    single { provideLoginRepository(get()) }
    viewModel { LoginViewModel(get()) }
    single { provideLoyaltyCardRepository(get()) }
    viewModel { LoyaltyViewModel(get()) }
}

fun provideLoginRepository(restApiService: ApiService): LoginRepository = LoginRepository(restApiService)

fun provideLoyaltyCardRepository(restApiService: ApiService): LoyaltyWalletRepository =
    LoyaltyWalletRepository(restApiService)