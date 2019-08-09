package com.bink.wallet.di

import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.add.AddViewModel
import com.bink.wallet.scenes.browse_brands.BrowseBrandsRepository
import com.bink.wallet.scenes.browse_brands.BrowseBrandsViewModel
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.login.LoginViewModel
import com.bink.wallet.scenes.loyalty_wallet.BarcodeViewModel
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyViewModel
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModules = module {

    single { provideLoginRepository(get()) }
    viewModel { LoginViewModel(get()) }

    single { provideLoyaltyCardRepository(get()) }
    viewModel { LoyaltyViewModel(get()) }

    single { provideBrowseBrandsRepository(get()) }
    viewModel { BrowseBrandsViewModel(get()) }

    viewModel { BarcodeViewModel() }

    viewModel { AddViewModel() }

}

fun provideLoginRepository(restApiService: ApiService): LoginRepository = LoginRepository(restApiService)

fun provideLoyaltyCardRepository(restApiService: ApiService): LoyaltyWalletRepository =
    LoyaltyWalletRepository(restApiService)

fun provideBrowseBrandsRepository(restApiService: ApiService): BrowseBrandsRepository = BrowseBrandsRepository(restApiService)