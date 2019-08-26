package com.bink.wallet.di

import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.MembershipPlanDao
import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.add.AddViewModel
import com.bink.wallet.scenes.add_join.AddJoinViewModel
import com.bink.wallet.scenes.browse_brands.BrowseBrandsViewModel
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.login.LoginViewModel
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsRepository
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel
import com.bink.wallet.scenes.loyalty_wallet.BarcodeViewModel
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyViewModel
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModules = module {

    single { provideLoginRepository(get()) }
    viewModel { LoginViewModel(get()) }

    single { provideLoyaltyCardRepository(get(), get(), get()) }
    viewModel { LoyaltyViewModel(get()) }

    viewModel { BrowseBrandsViewModel() }

    viewModel { BarcodeViewModel() }

    viewModel { AddViewModel() }

    viewModel { AddJoinViewModel() }

    single { provideLoyaltyCardDetailsRepository(get(), get()) }
    viewModel { LoyaltyCardDetailsViewModel(get()) }
}

fun provideLoginRepository(restApiService: ApiService): LoginRepository = LoginRepository(restApiService)

fun provideLoyaltyCardRepository(
    restApiService: ApiService,
    membershipPlanDao: MembershipPlanDao,
    membershipCardDao: MembershipCardDao
): LoyaltyWalletRepository =
    LoyaltyWalletRepository(restApiService, membershipCardDao, membershipPlanDao)

fun provideLoyaltyCardDetailsRepository(restApiService: ApiService, membershipCardDao: MembershipCardDao): LoyaltyCardDetailsRepository = LoyaltyCardDetailsRepository(restApiService, membershipCardDao)
